package com.example.scaffolddemo.ui.ability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffolddemo.data.mock.MockControl
import com.example.scaffolddemo.data.probe.ProbeRepository
import com.example.scaffolddemo.data.probe.ProbeResult
import com.example.scaffolddemo.data.probe.ProbeSourceKind
import com.example.scaffolddemo.ui.common.ErrorKind
import com.example.scaffolddemo.ui.common.ErrorMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 网络实测屏的状态。
 *
 * 多一个 [Idle]（首页没有）是有意的：**默认不发请求**。主链路保持零出网，
 * 只有使用者点了顶栏「请求」才真发一次 —— 否则一进这一页就联网，走查时说不清是谁发的。
 */
sealed interface ProbeUiState {
    /** 还没发过请求。 */
    data object Idle : ProbeUiState

    data class Loading(
        val source: ProbeSourceKind,
    ) : ProbeUiState

    /** 带上 [source]：错误态要能说清"是哪条通路失败的"。 */
    data class Error(
        val source: ProbeSourceKind,
        val kind: ErrorKind,
    ) : ProbeUiState

    data class Success(
        val result: ProbeResult,
    ) : ProbeUiState
}

/**
 * 网络实测 ViewModel：**一个方法、两档数据源**。
 *
 * 它只做两件事：记住当前选中档位、把 [ProbeRepository] 的结果翻译成 [ProbeUiState]。
 * 换档不清空已请求的结果没有意义（对照要看得见差别），所以**换档回到 [ProbeUiState.Idle]**：
 * 屏上显示的是"这一档还没请求过"，而不是拿另一档的数据冒充。
 *
 * [mockEnabled] 来自 [MockControl]（容器按 flavor 装配）：`prod` 构建立刻为 false，
 * 界面据此禁用「本地 mock」档。**界面不判 `BuildConfig`** —— 那是构建期的分支，
 * 判了就没法在单测里演两档。
 */
class NetworkProbeViewModel(
    private val repository: ProbeRepository,
    private val mockControl: MockControl,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProbeUiState>(ProbeUiState.Idle)
    val uiState: StateFlow<ProbeUiState> = _uiState.asStateFlow()

    /** 默认档位：有 mock 就用 mock（dev），没有只能走真网络（prod）。 */
    private val _source =
        MutableStateFlow(
            if (mockControl.enabled) ProbeSourceKind.MOCK else ProbeSourceKind.REAL,
        )
    val source: StateFlow<ProbeSourceKind> = _source.asStateFlow()

    val mockEnabled: Boolean = mockControl.enabled

    /** 切档：`prod` 下的 mock 档是禁用的，这里也挡住（不指望界面一定不点）。 */
    fun selectSource(kind: ProbeSourceKind) {
        if (kind == ProbeSourceKind.MOCK && !mockEnabled) return
        if (_source.value == kind) return
        _source.value = kind
        _uiState.value = ProbeUiState.Idle
    }

    /** 发一次请求（顶栏 action 与错误态重试共用）。 */
    fun request() {
        val kind = _source.value
        _uiState.value = ProbeUiState.Loading(kind)
        viewModelScope.launch {
            try {
                _uiState.value = ProbeUiState.Success(repository.load(kind))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // 不吞异常：两档的失败（超时 / 无网 / 404 / 解析）走同一个 ErrorMapper
                Timber.e(e, "网络实测失败 source=%s", kind)
                _uiState.value = ProbeUiState.Error(kind, ErrorMapper.map(e))
            }
        }
    }
}

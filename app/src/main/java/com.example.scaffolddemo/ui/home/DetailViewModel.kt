package com.example.scaffolddemo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffolddemo.data.DemoRepository
import com.example.scaffolddemo.data.DemoUser
import com.example.scaffolddemo.ui.common.ArticleUi
import com.example.scaffolddemo.ui.common.ErrorKind
import com.example.scaffolddemo.ui.common.ErrorMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/** 详情三态：加载 / 失败（含 404 特例文案）/ 成功。 */
sealed interface DetailUiState {
    data object Loading : DetailUiState

    data class Error(
        val kind: ErrorKind,
    ) : DetailUiState

    data class Success(
        val article: ArticleUi,
        val authorEmail: String,
    ) : DetailUiState
}

/**
 * 详情 ViewModel。
 *
 * 参数只有 `id` —— 界面不传整个对象（§4.2）：进程被杀后由导航恢复 `id`，
 * 详情重新拉一次即可，不会出现"参数是一个已经不新鲜的内存对象"这种隐患。
 *
 * 每次进入都重新拉（不缓存）。要"同一篇二次进入不转圈"是 P1（Room），
 * 现在不提前引入缓存 —— 缓存的失效策略比多一次请求贵得多。
 */
class DetailViewModel(
    private val repository: DemoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun load(id: Long) {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            try {
                val post = repository.byId(id)
                val author = authorOf(post.userId)
                _uiState.value =
                    DetailUiState.Success(
                        article =
                            ArticleUi(
                                id = post.id,
                                title = post.title,
                                body = post.body,
                                authorName = author?.name.orEmpty(),
                                authorId = post.userId,
                                date = post.date,
                            ),
                        authorEmail = author?.email.orEmpty(),
                    )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "详情加载失败 id=%d", id)
                _uiState.value = DetailUiState.Error(ErrorMapper.map(e))
            }
        }
    }

    /**
     * 作者信息**降级容忍**：拿不到就当没有（正文照常显示），不让整页进错误态。
     * 这是"次要数据不拖垮主数据"的常见取舍，值得抄。
     */
    private suspend fun authorOf(userId: Long): DemoUser? =
        try {
            repository.users().firstOrNull { it.id == userId }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "作者信息获取失败，详情降级显示 userId=%d", userId)
            null
        }
}

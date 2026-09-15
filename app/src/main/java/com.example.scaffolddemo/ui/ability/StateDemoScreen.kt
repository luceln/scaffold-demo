package com.example.scaffolddemo.ui.ability

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scaffolddemo.R
import com.example.scaffolddemo.data.mock.Scenario
import com.example.scaffolddemo.ui.common.ArticleCard
import com.example.scaffolddemo.ui.common.ArticleUi
import com.example.scaffolddemo.ui.common.EmptyView
import com.example.scaffolddemo.ui.common.ErrorKind
import com.example.scaffolddemo.ui.common.ErrorView
import com.example.scaffolddemo.ui.common.SkeletonArticleList
import com.example.scaffolddemo.ui.shell.SubPageScaffold
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 演示用的四个态。标签走字符串资源，切换时顺手把全局 mock 档位拨过去。 */
private enum class PreviewState(
    @StringRes val labelRes: Int,
    val scenario: Scenario,
) {
    LOADING(R.string.state_label_loading, Scenario.NORMAL),
    SUCCESS(R.string.state_label_success, Scenario.NORMAL),
    EMPTY(R.string.state_label_empty, Scenario.EMPTY),
    ERROR(R.string.state_label_error, Scenario.FAIL),
}

/**
 * 状态管理演示：把**同一个界面的四种输入**摆在一屏里对照。
 *
 * 这一屏想说的只有一句话：**四态不是四个界面，是同一个界面的四种输入**。
 * 所以下面四个态用的是首页那套**同一批组件**（`SkeletonArticleList` / `ArticleCard` /
 * `EmptyView` / `ErrorView`），不是另画一套假界面。
 *
 * 而且它**真的会改数据层**：切到「空」或「失败」会把全局 mock 档位拨过去，
 * 回首页刷新时请求真的返空数组、真的抛超时。演示看得见、也复现得了。
 * `prod` 构建没有 mock 可驱动 ⇒ 不显示控件，只留状态呈现（[StateDemoViewModel.mockEnabled]）。
 *
 * 换态控件形态：M3 的 `FilterChip` 行 → miuix `TabRow`（4 档，索引即 [PreviewState] 的 ordinal）。
 */
@Composable
fun StateDemoScreen(
    onBack: () -> Unit,
    viewModel: StateDemoViewModel = koinViewModel(),
) {
    var preview by rememberSaveable { mutableStateOf(PreviewState.SUCCESS) }

    SubPageScaffold(
        title = stringResource(R.string.title_state_demo),
        onBack = onBack,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                tabs = PreviewState.entries.map { stringResource(it.labelRes) },
                selectedTabIndex = preview.ordinal,
                onTabSelected = { index ->
                    val candidate = PreviewState.entries[index]
                    preview = candidate
                    // 档位是全局的：驱动之后回首页刷新，请求真的按这个档位走
                    viewModel.drive(candidate.scenario)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            Text(
                text =
                    stringResource(
                        R.string.state_current_format,
                        stringResource(preview.labelRes),
                    ),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Text(
                text =
                    stringResource(
                        if (viewModel.mockEnabled) R.string.state_demo_hint_mock else R.string.state_demo_hint_prod,
                    ),
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                StatePreview(state = preview)
            }
        }
    }
}

/** 四个态的呈现 —— 与首页 `when (uiState)` 的四个分支一一对应，只是输入由档位给定。 */
@Composable
private fun StatePreview(state: PreviewState) {
    when (state) {
        // 骨架屏：与真实卡片同尺寸同位置，落数据时页面不跳（dev 下 mock 补了延迟，真看得见）
        PreviewState.LOADING -> SkeletonArticleList(itemCount = 2)

        PreviewState.SUCCESS ->
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PREVIEW_ARTICLES.forEach { article ->
                    // 演示态列表：点按无副作用（真实页面接 onOpenDetail）
                    ArticleCard(article = article, onClick = {})
                }
            }

        // 空态是"请求成功了但没数据"，跟失败态是两回事：文案与出口都不同
        PreviewState.EMPTY -> EmptyView(message = stringResource(R.string.home_empty))

        // 失败态带错误码与「可重试」提示；重试这里没有真实下游（真实页面接 viewModel::load）
        PreviewState.ERROR -> ErrorView(kind = ErrorKind.TIMEOUT, onRetry = {})
    }
}

/** 演示数据：形状与首页列表一致，才能说明"同一套组件换输入"。 */
private val PREVIEW_ARTICLES =
    listOf(
        ArticleUi(
            id = 1L,
            title = "Koin 让 ViewModel 的装配只写一遍",
            body = "构造参数写依赖，容器里写装配，两边都不藏 —— 这就是显式构造注入。",
            authorName = "林清和",
            authorId = 1L,
            date = "2026-03-12",
        ),
        ArticleUi(
            id = 2L,
            title = "四态不是四个界面，是同一个界面的四种输入",
            body = "加载中 / 成功 / 空 / 失败 —— 分支写在 when 里，别写四份 UI。",
            authorName = "周砚",
            authorId = 2L,
            date = "2026-03-15",
        ),
    )

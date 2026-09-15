package com.example.scaffolddemo.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scaffolddemo.R
import com.example.scaffolddemo.ui.common.ArticleCard
import com.example.scaffolddemo.ui.common.EmptyView
import com.example.scaffolddemo.ui.common.ErrorView
import com.example.scaffolddemo.ui.common.SkeletonArticleList
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.basic.SearchCleanup
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 全屏搜索页：**首页顶栏 action 进入，不占 tab**（搜索是"进入某一项内部"的行为，
 * 不是并列的顶层目的地）。
 *
 * 结构 = miuix **`SearchBar` 完整范式**（2026-09-15 按 ailang 拍板改用组件本体）：
 * - `inputField` 槽挂 `InputField`（胶囊输入框；进页即 `expanded = true` ⇒ 自动聚焦拉键盘）；
 * - `outsideEndAction` 槽挂 `TextButton("取消")` —— 展开态才出现，点击退出搜索页；
 * - `content` 槽挂输入框以下的正文字（热门词 / 骨架 / 空态 / 结果），`fillMaxSize()` 吃掉剩余高度。
 *
 * ⚠️ **与 M3 版（TopAppBar 内嵌 OutlinedTextField）的三处结构差异**（真机走查要重点看）：
 * 1. **不再有标题栏** —— HyperOS 搜索页顶部就是搜索框本身，"取消"承担退出；
 * 2. **「返回」变「取消」** —— 系统返回键也走同一路径：`SearchBar` 内建 `NavigationBackHandler`
 *    在展开态拦截返回，转成 `onExpandedChange(false)`，本页即收起退出；
 * 3. 正文从 Scaffold body 移进 `content` 槽（滚动区仍是 `LazyColumn`，行为不变）。
 *
 * 图标用 `MiuixIcons.Basic.*`（`miuix-ui` 自带的 7 枚基础图标，与 `InputField` 内建默认图标
 * 同一套，保证同屏 Search 图标无两种笔画）—— 显式传入**只为换中文无障碍文案**，
 * miuix 默认的 `contentDescription` 是硬编码英文（"Search" / "Search Cleanup"）。
 *
 * 过滤走本地纯函数（[filterByTitle]），不重发请求。
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    // 进页即展开：`InputField` 在 expanded 时主动 requestFocus ⇒ 键盘自动拉起（与原行为一致）
    var expanded by remember { mutableStateOf(true) }

    // 收起即离开搜索页。`outsideEndAction` 的"取消"与系统返回键都汇到这里。
    val handleExpandedChange: (Boolean) -> Unit = { isExpanded ->
        expanded = isExpanded
        if (!isExpanded) onBack()
    }

    Scaffold { padding ->
        SearchBar(
            inputField = {
                InputField(
                    query = query,
                    onQueryChange = { value ->
                        query = value
                        viewModel.onQueryChange(value)
                    },
                    // 输入即过滤，键盘的"搜索"键不需要额外动作（保留槽位以说明这一点）
                    onSearch = { },
                    expanded = expanded,
                    onExpandedChange = handleExpandedChange,
                    label = stringResource(R.string.search_hint),
                    leadingIcon = {
                        Icon(
                            imageVector = MiuixIcons.Basic.Search,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceContainerHigh,
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            Icon(
                                imageVector = MiuixIcons.Basic.SearchCleanup,
                                contentDescription = stringResource(R.string.search_clear),
                                tint = MiuixTheme.colorScheme.onSurfaceContainerHighest,
                                modifier =
                                    Modifier
                                        .padding(start = 8.dp, end = 16.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            query = ""
                                            viewModel.onQueryChange("")
                                        },
                            )
                        }
                    },
                )
            },
            onExpandedChange = handleExpandedChange,
            expanded = expanded,
            outsideEndAction = {
                TextButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = onBack,
                )
            },
            content = {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = uiState) {
                        SearchUiState.Loading -> SkeletonArticleList()

                        is SearchUiState.Error -> ErrorView(kind = state.kind, onRetry = viewModel::loadPool)

                        SearchUiState.Idle ->
                            HotWords(
                                onPick = { word ->
                                    query = word
                                    viewModel.onQueryChange(word)
                                },
                            )

                        is SearchUiState.Empty ->
                            EmptyView(
                                message = stringResource(R.string.search_empty_title, state.query),
                                actionLabel = stringResource(R.string.search_change_keyword),
                                onAction = {
                                    query = ""
                                    viewModel.onQueryChange("")
                                },
                            )

                        is SearchUiState.Results ->
                            SearchResults(
                                state = state,
                                onOpenDetail = onOpenDetail,
                            )
                    }
                }
            },
            modifier = Modifier.fillMaxSize().padding(padding),
        )
    }
}

/**
 * 未输入时只给热门词。
 *
 * 形态从 M3 的 `SuggestionChip` 流式布局换成 **miuix `SmallTitle` 分组标题 + `BasicComponent` 列表行**
 * —— miuix 没有 chip 组件族，而"热门搜索"在 HyperOS 里本来就是列表项（左搜索图标 / 词条 / 右箭头）。
 * **信息密度下降**（一屏约 4–5 行，原来 4 个 chip 一行放得下），这是冻结文档 §4 记录的代价。
 */
@Composable
private fun HotWords(onPick: (String) -> Unit) {
    val hotWords =
        listOf(
            R.string.search_hot_koin,
            R.string.search_hot_skeleton,
            R.string.search_hot_navigation,
            R.string.search_hot_flavor,
        )
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 8.dp),
    ) {
        SmallTitle(text = stringResource(R.string.search_hot_label))
        hotWords.forEach { res ->
            val word = stringResource(res)
            BasicComponent(
                title = word,
                startAction = {
                    Icon(
                        imageVector = MiuixIcons.Basic.Search,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                endActions = {
                    Icon(
                        imageVector = MiuixIcons.ChevronForward,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                onClick = { onPick(word) },
            )
        }
        Text(
            text = stringResource(R.string.search_idle_hint, POOL_SIZE_HINT),
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun SearchResults(
    state: SearchUiState.Results,
    onOpenDetail: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "count") {
            Text(
                text = stringResource(R.string.search_result_count, state.articles.size),
                style = MiuixTheme.textStyles.subtitle,
            )
        }
        items(state.articles, key = { it.id }) { article ->
            // 搜索结果只给标题 + 作者（不给摘要）：结果更密，一屏能看更多
            ArticleCard(
                article = article,
                onClick = { onOpenDetail(article.id) },
                showBody = false,
            )
        }
    }
}

/** 与 `HomeViewModel` / `SearchViewModel` 的 PAGE_SIZE 对齐（示例数据集 24 篇）。 */
private const val POOL_SIZE_HINT = 24

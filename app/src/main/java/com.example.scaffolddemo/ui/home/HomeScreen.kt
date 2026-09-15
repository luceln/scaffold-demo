package com.example.scaffolddemo.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scaffolddemo.R
import com.example.scaffolddemo.ui.common.ArticleCard
import com.example.scaffolddemo.ui.common.EmptyView
import com.example.scaffolddemo.ui.common.ErrorView
import com.example.scaffolddemo.ui.common.SkeletonArticleList
import com.example.scaffolddemo.ui.shell.TopLevelDestination
import com.example.scaffolddemo.ui.shell.TopLevelScaffold
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Search

/**
 * 首页 tab：业务纵切（作者筛选 → 卡片列表 → 详情）。
 *
 * 这一页同时是**样板**：四态怎么写、列表用 `Card(onClick)` 怎么承载点击、
 * 顶栏 action 怎么挂 —— 新业务页照这一页抄。
 *
 * 状态来自 [HomeViewModel]（容器注入），页面自己不持有状态。
 */
@Composable
fun HomeScreen(
    onSelectTab: (TopLevelDestination) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TopLevelScaffold(
        destination = TopLevelDestination.HOME,
        onSelectTab = onSelectTab,
        actions = {
            IconButton(onClick = onOpenSearch) {
                Icon(
                    imageVector = MiuixIcons.Search,
                    contentDescription = stringResource(R.string.action_search),
                )
            }
            IconButton(onClick = viewModel::load) {
                Icon(
                    imageVector = MiuixIcons.Refresh,
                    contentDescription = stringResource(R.string.action_refresh),
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                HomeUiState.Loading -> SkeletonArticleList()

                is HomeUiState.Empty -> {
                    val filtered = state.filteredAuthorName
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 空态**保留筛选行**（当前作者高亮）：换作者是一步动作，不必先清空再重筛。
                        // 冻结原型屏 03 画的就是这个形态 —— 2026-09-14 走查发现实现把它收掉了。
                        AuthorFilterRow(
                            authors = state.authors,
                            selectedAuthorId = state.selectedAuthorId,
                            onSelectAuthor = viewModel::filterBy,
                            modifier = Modifier.padding(16.dp),
                        )
                        EmptyView(
                            message =
                                stringResource(
                                    if (filtered == null) R.string.home_empty else R.string.home_empty_author,
                                ),
                            actionLabel = if (filtered == null) null else stringResource(R.string.action_clear_filter),
                            onAction = if (filtered == null) null else ({ viewModel.filterBy(null) }),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                is HomeUiState.Error -> ErrorView(kind = state.kind, onRetry = viewModel::load)

                is HomeUiState.Success ->
                    HomeList(
                        state = state,
                        onSelectAuthor = viewModel::filterBy,
                        onOpenDetail = onOpenDetail,
                    )
            }
        }
    }
}

@Composable
private fun HomeList(
    state: HomeUiState.Success,
    onSelectAuthor: (Long?) -> Unit,
    onOpenDetail: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "author-filter") {
            AuthorFilterRow(
                authors = state.authors,
                selectedAuthorId = state.selectedAuthorId,
                onSelectAuthor = onSelectAuthor,
            )
        }

        items(state.articles, key = { it.id }) { article ->
            ArticleCard(article = article, onClick = { onOpenDetail(article.id) })
        }
    }
}

/**
 * 作者筛选行：「全部」+ 全部作者。**列表态与空态共用同一个**（提取出来就是为了空态也能留筛选行）。
 *
 * 形态从 M3 的 `FilterChip` 行换成 miuix 的 **`TabRow`**：miuix 没有 chip 组件族，
 * 而筛选行本来就是"单选 + 当前项高亮"，`TabRow` 的语义完全对得上
 * （`tabs: List<String>` + `selectedTabIndex` + `onTabSelected`，横向可滚动，
 * 项宽由文字实测决定、落在 76–98dp 之间）。
 * **交互从"标签"漂向"档位"** —— 这是换取纯 miuix 观感的代价，冻结文档 §4 已记录。
 *
 * 筛选维度是作者（数据源里只有作者可筛），过滤在本地做，不重发请求。
 */
@Composable
private fun AuthorFilterRow(
    authors: List<AuthorFilter>,
    selectedAuthorId: Long?,
    onSelectAuthor: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 索引 0 恒为「全部」，作者从 1 开始 —— 与 onTabSelected 的换算写在一处，不外泄
    val tabs = listOf(stringResource(R.string.author_all)) + authors.map { it.name }
    val selectedIndex = authors.indexOfFirst { it.id == selectedAuthorId } + 1

    TabRow(
        tabs = tabs,
        selectedTabIndex = selectedIndex,
        onTabSelected = { index -> onSelectAuthor(if (index == 0) null else authors[index - 1].id) },
        modifier = modifier,
    )
}

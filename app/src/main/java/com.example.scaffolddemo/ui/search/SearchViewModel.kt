package com.example.scaffolddemo.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffolddemo.data.DemoRepository
import com.example.scaffolddemo.ui.common.ArticleUi
import com.example.scaffolddemo.ui.common.ErrorKind
import com.example.scaffolddemo.ui.common.ErrorMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 搜索页四态 + 未输入态。
 *
 * 「未输入」（[Idle]）与「搜了没命中」（[Empty]）是两回事：前者给热门词引导，
 * 后者告诉用户搜的是什么词、并给一个改词的出口。
 */
sealed interface SearchUiState {
    /** 首次进入在拉文章池。 */
    data object Loading : SearchUiState

    data class Error(
        val kind: ErrorKind,
    ) : SearchUiState

    /** 未输入关键词：只给热门词，**不铺全量列表**（避免首屏一次性怼 24 条）。 */
    data object Idle : SearchUiState

    data class Empty(
        val query: String,
    ) : SearchUiState

    data class Results(
        val query: String,
        val articles: List<ArticleUi>,
    ) : SearchUiState
}

/**
 * 搜索 ViewModel：**先取全量、再本地过滤**。
 *
 * 为什么不做服务端搜索：数据源是 24 条的本地 mock 集，为它加一个搜索接口是过度设计；
 * 真实项目里把 [filterByTitle] 换成远程查询即可，界面一行不用改。
 */
class SearchViewModel(
    private val repository: DemoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var pool: List<ArticleUi> = emptyList()
    private var query: String = ""

    init {
        loadPool()
    }

    fun loadPool() {
        _uiState.value = SearchUiState.Loading
        viewModelScope.launch {
            try {
                val (posts, users) =
                    try {
                        val posts = repository.page(start = 0, limit = PAGE_SIZE)
                        posts to repository.users()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // 作者名拿不到不该让搜索不可用：降级成"只有标题"
                        Timber.w(e, "搜索页作者信息获取失败，降级为仅标题匹配")
                        repository.page(start = 0, limit = PAGE_SIZE) to emptyList()
                    }
                val nameById = users.associate { it.id to it.name }
                pool =
                    posts.map { post ->
                        ArticleUi(
                            id = post.id,
                            title = post.title,
                            body = post.body,
                            authorName = nameById[post.userId].orEmpty(),
                            authorId = post.userId,
                            date = post.date,
                        )
                    }
                applyQuery()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "搜索页加载失败")
                _uiState.value = SearchUiState.Error(ErrorMapper.map(e))
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        query = newQuery
        applyQuery()
    }

    private fun applyQuery() {
        if (_uiState.value is SearchUiState.Error) return
        val trimmed = query.trim()
        _uiState.value =
            when {
                trimmed.isEmpty() -> SearchUiState.Idle
                else -> {
                    val hits = filterByTitle(pool, trimmed)
                    if (hits.isEmpty()) SearchUiState.Empty(trimmed) else SearchUiState.Results(trimmed, hits)
                }
            }
    }

    private companion object {
        const val PAGE_SIZE = 24
    }
}

/**
 * 标题过滤：**纯函数**，所以边界（空查询 / 首尾空格 / 大小写 / 无命中）都能在
 * 纯 JVM 单测里穷举（`SearchFilterTest`）。
 *
 * 只匹配标题不匹配正文：正文命中会带来大量"看起来不相关"的结果（噪点）。
 * 空查询返回**空列表**而不是全量 —— 让调用方显式决定"未输入"该显示什么。
 */
fun filterByTitle(
    posts: List<ArticleUi>,
    query: String,
): List<ArticleUi> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return emptyList()
    return posts.filter { it.title.contains(trimmed, ignoreCase = true) }
}

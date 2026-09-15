package com.example.scaffolddemo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffolddemo.data.DemoRepository
import com.example.scaffolddemo.ui.common.ArticleUi
import com.example.scaffolddemo.ui.common.ErrorKind
import com.example.scaffolddemo.ui.common.ErrorMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/** 作者筛选行的一项：`id` 用来过滤，`name` 用来显示。 */
data class AuthorFilter(
    val id: Long,
    val name: String,
)

/**
 * 首页四态。
 *
 * 空态单独带一个 `filteredAuthorName`：**"筛选结果为空"与"压根没有数据"是两回事**，
 * 文案不同、出口也不同（前者给「清除筛选」，后者不给）。这是模板刻意示范的一点。
 *
 * 空态**同时带着 [authors] 与 [selectedAuthorId]**：2026-09-14 真机走查发现，
 * 原实现一进空态就把筛选行整条收掉，只剩一个「清除筛选」按钮 —— 用户想换个作者
 * 得先清空再重筛，而冻结原型（屏 03）画的是"筛选行留着、当前作者高亮"。
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Empty(
        val filteredAuthorName: String?,
        val authors: List<AuthorFilter> = emptyList(),
        val selectedAuthorId: Long? = null,
    ) : HomeUiState

    data class Error(
        val kind: ErrorKind,
    ) : HomeUiState

    data class Success(
        val articles: List<ArticleUi>,
        val authors: List<AuthorFilter>,
        val selectedAuthorId: Long?,
    ) : HomeUiState
}

/**
 * 首页 ViewModel：并发拉 `/posts` + `/users`，把作者名映射到卡片上。
 *
 * - 状态在 ViewModel ⇒ 旋转屏幕不重拉（Activity 不持有状态）；
 * - 异常不吞：交给 [ErrorMapper] 翻译，界面只认 [ErrorKind]；
 * - 筛选是**本地过滤**，不重发请求（24 条数据量下重发没有意义）。
 */
class HomeViewModel(
    private val repository: DemoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var articles: List<ArticleUi> = emptyList()
    private var authors: List<AuthorFilter> = emptyList()
    private var selectedAuthorId: Long? = null

    init {
        load()
    }

    /** 加载（首次进入 / 点顶栏刷新 / 错误态重试，三处同一个入口）。 */
    fun load() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
                // 两个请求并发：串行会让首屏多等一个往返
                val (posts, users) =
                    coroutineScope {
                        val postsDeferred = async { repository.page(start = 0, limit = PAGE_SIZE) }
                        val usersDeferred = async { repository.users() }
                        postsDeferred.await() to usersDeferred.await()
                    }
                val nameById = users.associate { it.id to it.name }
                articles =
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
                authors = users.map { AuthorFilter(id = it.id, name = it.name) }
                selectedAuthorId = null
                emitFiltered()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "首页加载失败")
                _uiState.value = HomeUiState.Error(ErrorMapper.map(e))
            }
        }
    }

    /** 作者筛选：`null` = 「全部」。 */
    fun filterBy(authorId: Long?) {
        selectedAuthorId = authorId
        emitFiltered()
    }

    private fun emitFiltered() {
        val selected = selectedAuthorId
        val visible = if (selected == null) articles else articles.filter { it.authorId == selected }
        _uiState.value =
            if (visible.isEmpty()) {
                HomeUiState.Empty(
                    filteredAuthorName = authors.firstOrNull { it.id == selected }?.name,
                    authors = authors,
                    selectedAuthorId = selected,
                )
            } else {
                HomeUiState.Success(articles = visible, authors = authors, selectedAuthorId = selected)
            }
    }

    private companion object {
        /** 骨架自带的中文 mock 集共 24 篇；**分页 UI 属 P1**，这里一次取全量做本地筛选。 */
        const val PAGE_SIZE = 24
    }
}

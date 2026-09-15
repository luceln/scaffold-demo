package com.example.scaffolddemo.ui.search

import com.example.scaffolddemo.ui.common.ArticleUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
 * 标题过滤的边界测试。
 *
 * `filterByTitle` 是纯函数，所以"未输入 / 只有空格 / 首尾空格 / 大小写 / 无命中 / 只匹配标题"
 * 这些边界都能直接钉死 —— 这正是把过滤逻辑抽成顶层函数（而不是塞进 ViewModel 的方法）的收益。
 */
class SearchFilterTest {
    private fun article(
        id: Long,
        title: String,
        body: String = "",
    ) = ArticleUi(
        id = id,
        title = title,
        body = body,
        authorName = "林清和",
        authorId = 1L,
        date = "2026-03-12",
    )

    private val pool =
        listOf(
            article(1L, "Koin 让 ViewModel 的装配只写一遍"),
            article(2L, "骨架屏要跟真实内容同尺寸同位置"),
            article(3L, "类型安全导航：路由是对象", body = "正文里提到 Koin 但标题没有"),
        )

    @Test
    fun `空查询 - 返回空列表而不是全量`() {
        assertTrue(filterByTitle(pool, "").isEmpty())
    }

    @Test
    fun `只有空格 - 也算空查询`() {
        assertTrue(filterByTitle(pool, "   ").isEmpty())
    }

    @Test
    fun `首尾空格 - 先裁剪再匹配`() {
        assertEquals(listOf(1L), filterByTitle(pool, "  Koin  ").map { it.id })
    }

    @Test
    fun `大小写不敏感`() {
        assertEquals(listOf(1L), filterByTitle(pool, "koin").map { it.id })
        assertEquals(listOf(1L), filterByTitle(pool, "KOIN").map { it.id })
    }

    @Test
    fun `只匹配标题 - 正文命中不算（否则结果里全是看起来不相关的条目）`() {
        assertTrue(filterByTitle(pool, "正文").isEmpty())
    }

    @Test
    fun `多个命中 - 按输入顺序返回`() {
        val hits = filterByTitle(listOf(article(1L, "导航 A"), article(2L, "导航 B")), "导航")

        assertEquals(listOf(1L, 2L), hits.map { it.id })
    }

    @Test
    fun `无命中 - 返回空列表`() {
        assertTrue(filterByTitle(pool, "这个词一定搜不到").isEmpty())
    }
}

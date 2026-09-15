package com.example.scaffolddemo.data.probe

import com.example.scaffolddemo.data.DemoRepository
import com.example.scaffolddemo.data.remote.RealArticleApi
import com.example.scaffolddemo.data.remote.RemoteArticlePage
import com.example.scaffolddemo.data.remote.decodeHtmlEntities
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * 网络实测屏的数据出口：**同一个方法、两档数据源**，返回同一个 [ProbeResult]。
 *
 * - [ProbeSourceKind.MOCK]：走正常的 `DemoRepository` ⇒ 在 `dev` 下被 `MockInterceptor` 拦下，
 *   **零出网**；用于和真网络档做对照。
 * - [ProbeSourceKind.REAL]：走 [RealArticleApi] 的绝对地址 ⇒ 绕过拦截器，**真出网**。
 *
 * 两档的失败（超时 / 无网 / 非 200 / 解析失败）都抛异常出去，由 ViewModel 交给**同一个**
 * `ErrorMapper` —— 不在这里吞异常，也不在这里造文案。
 */
class ProbeRepository(
    private val demo: DemoRepository,
    private val realApi: RealArticleApi,
    private val json: Json,
    private val realUrl: String = RealArticleApi.DEFAULT_URL,
) {
    suspend fun load(kind: ProbeSourceKind): ProbeResult =
        when (kind) {
            ProbeSourceKind.MOCK -> loadMock()
            ProbeSourceKind.REAL -> loadReal(realUrl)
        }

    private suspend fun loadMock(): ProbeResult {
        val startedAt = System.nanoTime()
        val posts = demo.page(start = 0, limit = MOCK_LIMIT)
        val nameById = demo.users().associate { it.id to it.name }
        val elapsedMs = (System.nanoTime() - startedAt) / NANOS_PER_MS
        val articles =
            posts.map { post ->
                ProbeArticle(
                    title = post.title,
                    subtitle =
                        listOfNotNull(
                            nameById[post.userId]?.takeIf { it.isNotBlank() },
                            post.date.takeIf { it.isNotBlank() },
                        ).joinToString(SUB_TITLE_SEPARATOR),
                )
            }
        return ProbeResult(
            articles = articles,
            meta =
                ProbeMeta(
                    source = ProbeSourceKind.MOCK,
                    url = MOCK_SOURCE_LABEL,
                    code = null, // 没走 HTTP，没有状态码可言
                    elapsedMs = elapsedMs,
                    bytes = articles.sumOf { it.title.length + it.subtitle.length }.toLong(),
                    count = articles.size,
                ),
        )
    }

    private suspend fun loadReal(url: String): ProbeResult {
        val startedAt = System.nanoTime()
        val response = realApi.probe(url)
        val elapsedMs = (System.nanoTime() - startedAt) / NANOS_PER_MS
        val body = response.body() ?: throw IOException("响应体为空：$url")
        val declaredLength = body.contentLength()
        val text = body.string()
        val bytes = if (declaredLength >= 0) declaredLength else text.toByteArray().size.toLong()
        val datas =
            json
                .decodeFromString<RemoteArticlePage>(text)
                .data
                ?.datas
                .orEmpty()
        return ProbeResult(
            articles =
                datas.map { article ->
                    ProbeArticle(
                        // 第三方标题里带 HTML 实体（实测 `&mdash;`），在这里还原成字符 ——
                        // 界面层不该知道"上游是网页"这件事，见 remote/HtmlText.kt
                        title = article.title.decodeHtmlEntities(),
                        subtitle =
                            listOfNotNull(
                                article.author.decodeHtmlEntities().takeIf { it.isNotBlank() },
                                article.niceDate.takeIf { it.isNotBlank() },
                            ).joinToString(SUB_TITLE_SEPARATOR),
                    )
                },
            meta =
                ProbeMeta(
                    source = ProbeSourceKind.REAL,
                    url = url,
                    code = response.code(),
                    elapsedMs = elapsedMs,
                    bytes = bytes,
                    count = datas.size,
                ),
        )
    }

    private companion object {
        /** 示例数据集共 24 篇；分页 UI 属 P1，这里一次取全量做同屏对照。 */
        const val MOCK_LIMIT = 24
        const val SUB_TITLE_SEPARATOR = " · "
        const val NANOS_PER_MS = 1_000_000L
        const val MOCK_SOURCE_LABEL = "assets/mock/posts.json"
    }
}

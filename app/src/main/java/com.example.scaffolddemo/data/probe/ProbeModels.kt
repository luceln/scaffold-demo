package com.example.scaffolddemo.data.probe

/** 网络实测屏的两个数据源档位。 */
enum class ProbeSourceKind {
    /** 本地 mock：走 `MockInterceptor`，零出网。`prod` 构建下不可用。 */
    MOCK,

    /** 真实网络：走绝对地址，真 DNS / TLS / socket。 */
    REAL,
}

/**
 * 列表条目的**统一 UI 模型**：两档数据源映射成同一个形状，
 * 所以屏上是「同一套列表、同一个 `UiState`，只换数据来源」。
 */
data class ProbeArticle(
    val title: String,
    val subtitle: String,
)

/**
 * 「真出网」的活证据。
 *
 * `code` 只有真网络档才有（mock 档压根没走 HTTP，为 `null`）；
 * `elapsedMs` / `bytes` / `count` 两档都量，便于对照。
 * [url] 也由数据层给出 —— 界面不该知道"数据是从哪读的"这种细节。
 */
data class ProbeMeta(
    val source: ProbeSourceKind,
    val url: String,
    val code: Int?,
    val elapsedMs: Long,
    val bytes: Long,
    val count: Int,
)

/** 一次请求的结果：列表 + 元信息。 */
data class ProbeResult(
    val articles: List<ProbeArticle>,
    val meta: ProbeMeta,
)

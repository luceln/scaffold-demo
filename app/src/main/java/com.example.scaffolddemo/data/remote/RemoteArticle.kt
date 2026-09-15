package com.example.scaffolddemo.data.remote

import kotlinx.serialization.Serializable

/**
 * 第三方接口（wanandroid）的响应模型。
 *
 * 第三方字段**多且会变**，所以这里只声明用得上的五个，其余靠 `Json { ignoreUnknownKeys = true }`
 * 忽略 —— 见 `di/NetworkModule.kt` 的 `Json` 单例。少字段则会触发 `MissingFieldException`，
 * 同样汇进统一错误映射路径（不另开分支）。
 */
@Serializable
data class RemoteArticlePage(
    val data: RemoteArticleData? = null,
)

@Serializable
data class RemoteArticleData(
    val datas: List<RemoteArticle> = emptyList(),
)

@Serializable
data class RemoteArticle(
    val title: String = "",
    val link: String = "",
    val author: String = "",
    val niceDate: String = "",
    val chapterName: String = "",
)

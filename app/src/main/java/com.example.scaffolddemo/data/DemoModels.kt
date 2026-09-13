package com.example.scaffolddemo.data

import kotlinx.serialization.Serializable

/**
 * 纵切的演示模型：对齐 jsonplaceholder 的 /posts 资源（公开示例 API）。
 * 这份文件是「给人抄的样板」—— 新实体照这个形状写 DTO。
 */
@Serializable
data class DemoItem(
    val id: Long = 0L,
    val userId: Long = 0L,
    val title: String = "",
    val body: String = "",
)

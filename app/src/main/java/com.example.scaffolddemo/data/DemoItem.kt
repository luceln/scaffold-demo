package com.example.scaffolddemo.data

import kotlinx.serialization.Serializable

/**
 * 纵切的演示模型：对齐 `/posts` 资源。
 * 这份文件是「给人抄的样板」—— 新实体照这个形状写 DTO。
 *
 * `date` 是列表卡片第二行「作者 · 日期」的来源。骨架自带的中文 mock 数据里这一项有值；
 * 接自己的后端时按实际字段改名即可（序列化字段名跟着 `@SerialName` 或属性名走）。
 */
@Serializable
data class DemoItem(
    val id: Long = 0L,
    val userId: Long = 0L,
    val title: String = "",
    val body: String = "",
    val date: String = "",
)

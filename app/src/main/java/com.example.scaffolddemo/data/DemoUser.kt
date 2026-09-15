package com.example.scaffolddemo.data

import kotlinx.serialization.Serializable

/**
 * 作者模型：对齐 `/users` 资源。
 *
 * 列表「作者名」与详情「作者区（姓名 + 邮箱）」都靠它 —— 卡片第二行显示的是**真实作者名**，
 * 由 `HomeViewModel` 按 `userId` 从这份数据映射出来，不是页面里写死的假名。
 */
@Serializable
data class DemoUser(
    val id: Long = 0L,
    val name: String = "",
    val email: String = "",
)

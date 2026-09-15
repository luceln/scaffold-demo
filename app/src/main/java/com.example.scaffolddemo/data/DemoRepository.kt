package com.example.scaffolddemo.data

/**
 * 纵切的数据层出口。
 *
 * 装配（OkHttp / 序列化 / baseUrl）**已经搬进 DI 容器**（`di/NetworkModule.kt`）——
 * 这里没有 `create()` 这类手工装配入口，新增网络资源时只加方法。
 * 这也意味着 baseUrl 由**构建变体**决定（`BuildConfig.BASE_URL`），不在代码里写死。
 *
 * 失败语义：抛 `IOException` / `HttpException`，由调用方（ViewModel）决定降级方式，
 * 统一经 `ui/common/ErrorMapper.kt` 翻译成用户可读文案。
 */
class DemoRepository(
    private val api: DemoApi,
) {
    /** 分页查询：`_start` / `_limit` 由数据源侧真切片（mock 拦截器也实现同一套切片）。 */
    suspend fun page(
        start: Int,
        limit: Int,
    ): List<DemoItem> = api.page(start, limit)

    suspend fun byId(id: Long): DemoItem = api.byId(id)

    suspend fun create(item: DemoItem): DemoItem = api.create(item)

    suspend fun update(
        id: Long,
        item: DemoItem,
    ): DemoItem = api.update(id, item)

    /** 删除：2xx 即成功（204/200 都常见），非 2xx 抛 HttpException */
    suspend fun delete(id: Long): Boolean = api.delete(id).isSuccessful

    /** 作者表：`userId` → 作者名/邮箱 的映射来源。 */
    suspend fun users(): List<DemoUser> = api.users()
}

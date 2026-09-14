package com.example.scaffolddemo.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 纵切的「对外入口」（§7.2 形态 B）：Retrofit 接口。
 *
 * 纵切测试通过 MockWebServer 回环真打这个接口（OkHttp 完整栈），
 * 五条路径与 §7.2 的硬要求一一对应：
 * 分页查询 / 按 id 查询 / 新增 / 修改 / 删除。
 */
interface DemoApi {
    @GET("posts")
    suspend fun page(
        @Query("_start") start: Int,
        @Query("_limit") limit: Int,
    ): List<DemoItem>

    @GET("posts/{id}")
    suspend fun byId(
        @Path("id") id: Long,
    ): DemoItem

    @POST("posts")
    suspend fun create(
        @Body item: DemoItem,
    ): DemoItem

    @PUT("posts/{id}")
    suspend fun update(
        @Path("id") id: Long,
        @Body item: DemoItem,
    ): DemoItem

    @DELETE("posts/{id}")
    suspend fun delete(
        @Path("id") id: Long,
    ): retrofit2.Response<Unit>
}

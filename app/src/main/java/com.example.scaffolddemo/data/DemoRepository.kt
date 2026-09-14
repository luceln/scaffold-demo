package com.example.scaffolddemo.data

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber

/**
 * 纵切的数据层出口：手动构造注入（骨架不引 DI 框架 —— 这是应用规模决策，
 * 引入 Hilt/Koin 的时机见 README「扩展路径」）。
 *
 * Retrofit + OkHttp + kotlinx-serialization 的装配样板：
 * 新增网络资源时照本文件与 DemoApi 的形状扩展。
 */
class DemoRepository(
    private val api: DemoApi,
) {
    /** 分页查询：失败抛 IOException/HttpException，由调用方决定降级方式 */
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

    companion object {
        /** 示例 API（公开稳定）。接自己的后端时替换成真实 baseUrl，见 README */
        const val BASE_URL: String = "https://jsonplaceholder.typicode.com/"

        fun create(baseUrl: String = BASE_URL): DemoRepository {
            val json =
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }
            val http =
                OkHttpClient
                    .Builder()
                    .addInterceptor(
                        HttpLoggingInterceptor { message ->
                            Timber.d(message)
                        }.apply { level = HttpLoggingInterceptor.Level.BASIC },
                    ).build()
            val retrofit =
                Retrofit
                    .Builder()
                    .baseUrl(baseUrl)
                    .client(http)
                    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                    .build()
            return DemoRepository(retrofit.create(DemoApi::class.java))
        }
    }
}

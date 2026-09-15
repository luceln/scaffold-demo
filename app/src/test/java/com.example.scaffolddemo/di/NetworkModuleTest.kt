package com.example.scaffolddemo.di

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

/*
 * 网络层装配的 flavor 差异测试。
 *
 * 构建变体的差异**全部**收敛在 `networkModule` 的一行 `getOrNull<Interceptor>()` 上：
 * 容器里有拦截器就挂、没有就不挂。所以这一条能测，就等于把 dev/prod 的差异机制测住了 ——
 * 而且它不依赖 flavor 源集（两个变体跑的是同一份测试），因为它用的是测试自己提供的拦截器。
 *
 * 真正验证"dev 有、prod 没有"的是 testDev / testProd 下的两个装配测试。
 */
class NetworkModuleTest {
    private class RecordingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
    }

    private fun koinWith(vararg extra: Module): KoinApplication = koinApplication { modules(networkModule(baseUrl = BASE_URL), *extra) }

    @Test
    fun `容器里没有 Interceptor - OkHttp 只挂日志拦截器`() {
        val app = koinWith()
        try {
            assertEquals(
                1,
                app.koin
                    .get<OkHttpClient>()
                    .interceptors.size,
            )
        } finally {
            app.close()
        }
    }

    @Test
    fun `容器里有 Interceptor - 被可选挂载成第二个`() {
        val provided = module { single<Interceptor> { RecordingInterceptor() } }
        val app = koinWith(provided)
        try {
            assertEquals(
                2,
                app.koin
                    .get<OkHttpClient>()
                    .interceptors.size,
            )
        } finally {
            app.close()
        }
    }

    private companion object {
        const val BASE_URL = "https://example.test/"
    }
}

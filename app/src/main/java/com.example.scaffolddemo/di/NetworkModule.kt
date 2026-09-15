package com.example.scaffolddemo.di

import com.example.scaffolddemo.BuildConfig
import com.example.scaffolddemo.data.DemoApi
import com.example.scaffolddemo.data.DemoRepository
import com.example.scaffolddemo.data.probe.ProbeRepository
import com.example.scaffolddemo.data.remote.RealArticleApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber

/**
 * 网络层装配。
 *
 * `baseUrl` 作参数而不是常量：正式链路用构建变体给的 `BuildConfig.BASE_URL`，
 * 单测用 `MockWebServer` 的回环地址注入 —— 同一份装配，两种环境。
 *
 * 【可选挂载】是 flavor 差异的全部实现：本模块**不知道** mock 的存在，只问容器
 * 「有没有 `Interceptor`？」—— `dev` 源集有（`MockInterceptor`），`prod` 源集没有。
 */
fun networkModule(baseUrl: String = BuildConfig.BASE_URL): Module =
    module {
        // 宽松反序列化：第三方接口字段多且会变，忽略未知字段；缺失字段仍会抛错（汇进统一错误路径）。
        // 装配块内一律用行注释：ktlint 的 standard:kdoc 不允许 KDoc 出现在 `module { }` 里。
        single {
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
        }

        single {
            val mockInterceptor: Interceptor? = getOrNull()
            OkHttpClient
                .Builder()
                .addInterceptor(
                    HttpLoggingInterceptor { message -> Timber.d(message) }
                        .apply { level = HttpLoggingInterceptor.Level.BASIC },
                ).apply { if (mockInterceptor != null) addInterceptor(mockInterceptor) }
                .build()
        }

        single {
            Retrofit
                .Builder()
                .baseUrl(baseUrl)
                .client(get())
                .addConverterFactory(
                    get<Json>().asConverterFactory("application/json".toMediaType()),
                ).build()
        }

        single { get<Retrofit>().create(DemoApi::class.java) }

        // 真出网通路：同一条 Retrofit，请求时用 `@Url` 传绝对地址绕过 mock 白名单。
        single { get<Retrofit>().create(RealArticleApi::class.java) }

        single { DemoRepository(get()) }

        single { ProbeRepository(get(), get(), get()) }
    }

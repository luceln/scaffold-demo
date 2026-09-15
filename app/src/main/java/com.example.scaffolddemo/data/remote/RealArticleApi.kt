package com.example.scaffolddemo.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * **真出网通路**：唯一一条不被 mock 拦截器覆盖的请求。
 *
 * 为什么返回 `Response<ResponseBody>` 而不是 `Response<RemoteArticlePage>`：
 * 这一屏的存在意义就是**证明真的出网了**，所以要在界面上给出 状态码 / 耗时 / 响应字节数 ——
 * 拿到原始 `ResponseBody` 才能量出真实字节数；JSON 反序列化改由 `ProbeRepository` 显式做
 * （同一条 `Json` 单例，行为不变）。
 *
 * `@Url` 传的是**绝对地址** ⇒ 绕过 `BASE_URL`（dev 那个假域名），也因此不命中 mock 拦截器的
 * 白名单，会被 `chain.proceed()` 原样放行到真实网络栈。**不需要第二个 Retrofit。**
 *
 * 换源：只改 [DEFAULT_URL] 一处（备选见模板 README「网络实测的外网源」）。第三方接口不是契约，
 * 站点改版/下线只会让这一屏进错误态，**主链路（走 mock 的首页）不受影响**。
 */
interface RealArticleApi {
    @GET
    suspend fun probe(
        @Url url: String,
    ): Response<ResponseBody>

    companion object {
        /** 默认源：玩Android 文章列表（真中文技术文章、免鉴权、国内直连快）。 */
        const val DEFAULT_URL: String = "https://www.wanandroid.com/article/list/0/json"
    }
}

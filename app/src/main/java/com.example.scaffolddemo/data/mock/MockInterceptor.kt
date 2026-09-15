package com.example.scaffolddemo.data.mock

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * `dev` 变体的数据源心脏：把**已声明的接口路径**拦下来，换成模板自带的中文 mock JSON。
 *
 * 两条语义要同时成立，缺一不可：
 *
 * 1. **白名单语义** —— 只有命中 [declaredAsset] 的请求才被拦，其它一律 `chain.proceed()`
 *    原样放行到真实网络栈。于是「网络实测」屏用绝对地址就能绕过它真出网（不需要第二个
 *    Retrofit / 第二份装配），同时**拼错的 mock 路径也不会被静默吞掉**（单测锁住这条）。
 * 2. **故障注入** —— [MockControl] 切到 [Scenario.EMPTY] / [Scenario.FAIL] 时**真的**返空数组 /
 *    真的抛超时，让界面四态由真实数据层驱动，而不是画出来的假态。
 *
 * 注意它替换的只是「最后一跳 socket」：Retrofit 接口、OkHttp 调用链、kotlinx-serialization
 * 反序列化、协程调度、异常传播**全部真跑** —— 这是这个模板网络纵切的价值所在。
 */
class MockInterceptor(
    private val data: MockData,
    private val control: MockControl,
    private val json: Json,
    private val latencyMs: Long = DEFAULT_LATENCY_MS,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.trim('/')
        // 未声明的路径 → 原样放行（真网络通路靠这条成立）
        val assetName = declaredAsset(path) ?: return chain.proceed(request)
        val id = detailId(path, assetName)

        return when (control.scenario.value) {
            Scenario.FAIL -> throw SocketTimeoutException("mock: 模拟请求超时 path=$path")

            // 空档：列表给空数组（落空态）；详情给 404（没有这条数据）
            Scenario.EMPTY -> if (id == null) respond(request, 200, EMPTY_ARRAY) else respond(request, 404)

            Scenario.NORMAL -> {
                // 补一点延迟：真机网速快时骨架屏一闪而过、截图都截不到
                if (latencyMs > 0) Thread.sleep(latencyMs)
                val raw =
                    data.read(assetName)
                        ?: throw IOException("mock 数据缺失：assets/mock/$assetName.json")
                if (id == null) {
                    respond(request, 200, slice(raw, request))
                } else {
                    // 整表按 id 挑一条；找不到给**真 404**，让「详情 404」这条错误路径可达
                    pickById(raw, id)?.let { respond(request, 200, it) } ?: respond(request, 404)
                }
            }
        }
    }

    /** 已声明的路径 → 资源名；未声明返回 `null`（调用方据此放行）。 */
    private fun declaredAsset(path: String): String? =
        when {
            path == POSTS || path.startsWith("$POSTS/") -> POSTS
            path == USERS || path.startsWith("$USERS/") -> USERS
            else -> null
        }

    /** 详情路径（`posts/7`）取 id；列表路径返回 `null`。 */
    private fun detailId(
        path: String,
        assetName: String,
    ): Long? =
        if (assetName == POSTS && path.startsWith("$POSTS/")) {
            path.removePrefix("$POSTS/").toLongOrNull()
        } else {
            null
        }

    /** `_start` / `_limit` 本地切片；分页 UI 属 P1，但请求侧的切片逻辑是真跑的。 */
    private fun slice(
        raw: String,
        request: Request,
    ): String {
        val array =
            json.parseToJsonElement(raw) as? JsonArray
                ?: throw IOException("mock 数据不是 JSON 数组")
        val start =
            request.url
                .queryParameter("_start")
                ?.toIntOrNull()
                ?.coerceAtLeast(0) ?: 0
        val limit = request.url.queryParameter("_limit")?.toIntOrNull()
        val sliced = array.drop(start).let { if (limit == null) it else it.take(limit) }
        return JsonArray(sliced).toString()
    }

    private fun pickById(
        raw: String,
        id: Long,
    ): String? =
        (json.parseToJsonElement(raw) as? JsonArray)
            ?.firstOrNull {
                (it as? JsonObject)
                    ?.get("id")
                    ?.jsonPrimitive
                    ?.content
                    ?.toLongOrNull() == id
            }?.toString()

    private fun respond(
        request: Request,
        code: Int,
        body: String = "",
    ): Response =
        Response
            .Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code == 200) "OK" else "Mock Error")
            .body(body.toResponseBody(JSON))
            .build()

    private companion object {
        const val POSTS = "posts"
        const val USERS = "users"
        const val EMPTY_ARRAY = "[]"

        /**
         * 让骨架屏在真机上真的看得见（走查时不用赌网速）。
         *
         * **2026-09-14 真机实测从 180ms 调到 450ms**：180ms 下用 adb 连拍 5 张都截不到加载态
         * （截图本身要 ~250ms 一次），人眼同样只能看到一闪 —— 骨架屏等于白做。
         * 450ms 仍远快于任何真网络请求，不掩盖真实体感。
         */
        const val DEFAULT_LATENCY_MS = 450L

        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}

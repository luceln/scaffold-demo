package com.example.scaffolddemo.data.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
 * mock 拦截器的行为测试 —— 这个模板最容易被写坏的一块。
 *
 * 两条语义必须同时成立，缺了任何一条都会出问题：
 *
 *  1. 白名单：只有 `posts` / `users` 被拦，其它路径原样放行到真网络栈。
 *     少了这条，"网络实测"屏的真出网档会静默读到本地假数据（最难发现的那种 bug）。
 *  2. 故障注入：空档真的返空数组、失败档真的抛超时。
 *     少了这条，"状态管理"演示就退化成画四个假界面。
 *
 * 放行的判定不用真网络：紧跟着挂一个**必抛哨兵**的拦截器，它一旦被调用就说明
 * "请求确实被放行了"。测试全程零出网、零 DNS，秒级完成。
 */
class MockInterceptorTest {
    private class FakeControl(
        override val enabled: Boolean = true,
    ) : MockControl {
        private val state = MutableStateFlow(Scenario.NORMAL)

        override val scenario: StateFlow<Scenario> = state

        override fun drive(scenario: Scenario) {
            state.value = scenario
        }
    }

    /** 放行哨兵：放在链尾，正常实现永远不会走到它（走到了就是 chain.proceed 了）。 */
    private class PassthroughReached : RuntimeException("请求被放行到了下一棒")

    private fun client(
        control: MockControl,
        data: MockData = DATA,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(MockInterceptor(data = data, control = control, json = JSON, latencyMs = 0))
            .addInterceptor { throw PassthroughReached() }
            .build()

    private fun get(
        path: String,
        control: MockControl = FakeControl(),
        data: MockData = DATA,
    ): Int = client(control, data).newCall(request(path)).execute().code

    private fun body(
        path: String,
        control: MockControl = FakeControl(),
        data: MockData = DATA,
    ): String =
        client(control, data)
            .newCall(request(path))
            .execute()
            .body!!
            .string()

    private fun request(path: String): Request = Request.Builder().url("$BASE$path").build()

    // ---- 白名单语义 ----

    @Test
    fun `列表路径 - 被拦下并按 start limit 真切片`() {
        val raw = body("/posts?_start=1&_limit=1")

        assertEquals(1, JSON.parseToJsonElement(raw).jsonArray.size)
        assertTrue(raw.contains("第二篇"), "切片应当从第 2 条开始")
        assertFalse(raw.contains("第一篇"))
    }

    @Test
    fun `详情路径 - 按 id 挑出单条对象而不是列表`() {
        val raw = body("/posts/2")

        assertTrue(raw.contains("第二篇"))
        assertFalse(raw.contains("第一篇"))
        assertTrue(JSON.parseToJsonElement(raw) is JsonObject)
    }

    @Test
    fun `作者表 - users 路径同样被拦（零出网）`() {
        assertTrue(body("/users").contains("林清和"))
    }

    @Test
    fun `未声明的路径 - 原样放行给下一棒`() {
        assertFailsWith<PassthroughReached> {
            client(FakeControl()).newCall(request("/unknown")).execute()
        }
    }

    // ---- 故障注入 ----

    @Test
    fun `空档 - 列表返空数组 详情返真 404`() {
        val control = FakeControl()
        control.drive(Scenario.EMPTY)

        assertEquals(200, get("/posts", control))
        assertEquals("[]", body("/posts", control))
        // 详情返真 404（不是抛异常）：让"详情 404"这条错误路径真的可达
        assertEquals(404, get("/posts/1", control))
    }

    @Test
    fun `失败档 - 抛 SocketTimeoutException（与真网络失败走同一条错误映射）`() {
        val control = FakeControl()
        control.drive(Scenario.FAIL)

        assertFailsWith<SocketTimeoutException> {
            client(control).newCall(request("/posts")).execute()
        }
    }

    // ---- 边界 ----

    @Test
    fun `详情 id 不存在 - 返 404，不伪造一条数据出来`() {
        assertEquals(404, get("/posts/999"))
    }

    @Test
    fun `mock 数据缺失 - 抛 IOException，不静默返回空`() {
        val missing = MockData { null }

        assertFailsWith<IOException> {
            client(FakeControl(), missing).newCall(request("/posts")).execute()
        }
    }

    private companion object {
        const val BASE = "https://example.test/"

        const val POSTS_JSON = """
            [
              {"id":1,"userId":1,"title":"第一篇","body":"b1","date":"2026-01-01"},
              {"id":2,"userId":2,"title":"第二篇","body":"b2","date":"2026-01-02"},
              {"id":3,"userId":1,"title":"第三篇","body":"b3","date":"2026-01-03"}
            ]
        """

        const val USERS_JSON = """[{"id":1,"name":"林清和","email":"a@example.com"}]"""

        val DATA =
            MockData { name ->
                when (name) {
                    "posts" -> POSTS_JSON
                    "users" -> USERS_JSON
                    else -> null
                }
            }

        val JSON =
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
    }
}

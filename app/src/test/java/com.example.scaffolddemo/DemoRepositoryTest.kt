package com.example.scaffolddemo

import com.example.scaffolddemo.data.DemoItem
import com.example.scaffolddemo.data.DemoRepository
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 纵切测试（§7.2 形态 B）：五条 CRUD 路径**真打 OkHttp/Retrofit 完整 HTTP 栈**，
 * MockWebServer 只替换远端（本机回环），序列化/反序列化、路径、查询参数、
 * 方法全部真跑 —— 不是 mock 内层函数。
 *
 * 纯 JVM、离线、秒级 —— `./gradlew testDebugUnitTest` 一键跑。
 */
class DemoRepositoryTest {

    private fun serverWith(body: String, code: Int = 200): MockWebServer =
        MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(code).setBody(body))
            start()
        }

    private fun repo(server: MockWebServer): DemoRepository =
        DemoRepository.create(baseUrl = server.url("/").toString())

    @Test
    fun `分页查询 - 带上 start 与 limit 查询参数并解析列表`() = runTest {
        val server = serverWith(
            """[{"id":1,"userId":1,"title":"t1","body":"b1"},
               {"id":2,"userId":1,"title":"t2","body":"b2"}]""",
        )
        try {
            val items = repo(server).page(start = 10, limit = 2)

            assertEquals(2, items.size)
            assertEquals("t2", items[1].title)
            val request = server.takeRequest()
            assertEquals("GET", request.method)
            assertEquals("/?_start=10&_limit=2", request.path)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `按 id 查询 - 路径带 id 并反序列化单个对象`() = runTest {
        val server = serverWith("""{"id":7,"userId":3,"title":"t7","body":"b7"}""")
        try {
            val item = repo(server).byId(id = 7)

            assertEquals(7L, item.id)
            assertEquals("t7", item.title)
            val request = server.takeRequest()
            assertEquals("GET", request.method)
            assertEquals("/7", request.path)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `新增 - 请求体为序列化 JSON 并返回创建结果`() = runTest {
        val server = serverWith("""{"id":101,"userId":1,"title":"new","body":"body"}""")
        try {
            val created = repo(server).create(DemoItem(userId = 1, title = "new", body = "body"))

            assertEquals(101L, created.id)
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertTrue(request.body.readUtf8().contains("\"title\":\"new\""))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `修改 - PUT 路径带 id 且请求体可反序列化`() = runTest {
        val server = serverWith("""{"id":7,"userId":1,"title":"updated","body":"b"}""")
        try {
            val updated = repo(server).update(id = 7, item = DemoItem(id = 7, title = "updated"))

            assertEquals("updated", updated.title)
            val request = server.takeRequest()
            assertEquals("PUT", request.method)
            assertEquals("/7", request.path)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `删除 - 2xx 成功且非 2xx 判失败`() = runTest {
        val ok = serverWith("", code = 204)
        try {
            assertTrue(repo(ok).delete(id = 1))
            assertEquals("DELETE", ok.takeRequest().method)
        } finally {
            ok.shutdown()
        }

        val fail = serverWith("{}", code = 404)
        try {
            assertFalse(repo(fail).delete(id = 1))
        } finally {
            fail.shutdown()
        }
    }
}

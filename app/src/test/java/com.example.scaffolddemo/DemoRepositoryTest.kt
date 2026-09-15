package com.example.scaffolddemo

import com.example.scaffolddemo.data.DemoItem
import com.example.scaffolddemo.data.DemoRepository
import com.example.scaffolddemo.di.networkModule
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 纵切测试（§7.2 形态 B）：五条 CRUD 路径**真打 OkHttp/Retrofit 完整 HTTP 栈**，
 * MockWebServer 只替换远端（本机回环），序列化/反序列化、路径、查询参数、
 * 方法全部真跑 —— 不是 mock 内层函数。
 *
 * **装配来自真实的 [networkModule]**（只是把 `baseUrl` 换成回环地址），不再在测试里手工
 * 拼 Retrofit：这样测试同时守住"容器给出的仓库确实能工作"这件事。装配有几处（baseUrl、
 * Json 配置、OkHttp 客户端）就只有一份，改一处两边同时变。
 *
 * 纯 JVM、离线、秒级 —— `./gradlew testDevDebugUnitTest` 一键跑。
 */
class DemoRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var app: KoinApplication

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        app = koinApplication { modules(networkModule(baseUrl = server.url("/").toString())) }
    }

    @After
    fun tearDown() {
        app.close()
        server.shutdown()
    }

    private val repo: DemoRepository get() = app.koin.get()

    private fun enqueue(
        body: String,
        code: Int = 200,
    ) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    @Test
    fun `分页查询 - 带上 start 与 limit 查询参数并解析列表`() =
        runTest {
            enqueue(
                """[{"id":1,"userId":1,"title":"t1","body":"b1"},
               {"id":2,"userId":1,"title":"t2","body":"b2"}]""",
            )

            val items = repo.page(start = 10, limit = 2)

            assertEquals(2, items.size)
            assertEquals("t2", items[1].title)
            val request = server.takeRequest()
            assertEquals("GET", request.method)
            assertEquals("/posts?_start=10&_limit=2", request.path)
        }

    @Test
    fun `按 id 查询 - 路径带 id 并反序列化单个对象`() =
        runTest {
            enqueue("""{"id":7,"userId":3,"title":"t7","body":"b7"}""")

            val item = repo.byId(id = 7)

            assertEquals(7L, item.id)
            assertEquals("t7", item.title)
            val request = server.takeRequest()
            assertEquals("GET", request.method)
            assertEquals("/posts/7", request.path)
        }

    @Test
    fun `新增 - 请求体为序列化 JSON 并返回创建结果`() =
        runTest {
            enqueue("""{"id":101,"userId":1,"title":"new","body":"body"}""")

            val created = repo.create(DemoItem(userId = 1, title = "new", body = "body"))

            assertEquals(101L, created.id)
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertTrue(request.body.readUtf8().contains("\"title\":\"new\""))
        }

    @Test
    fun `修改 - PUT 路径带 id 且请求体可反序列化`() =
        runTest {
            enqueue("""{"id":7,"userId":1,"title":"updated","body":"b"}""")

            val updated = repo.update(id = 7, item = DemoItem(id = 7, title = "updated"))

            assertEquals("updated", updated.title)
            val request = server.takeRequest()
            assertEquals("PUT", request.method)
            assertEquals("/posts/7", request.path)
        }

    @Test
    fun `删除 - 2xx 成功且非 2xx 判失败`() =
        runTest {
            enqueue("", code = 204)
            assertTrue(repo.delete(id = 1))
            assertEquals("DELETE", server.takeRequest().method)

            enqueue("{}", code = 404)
            assertFalse(repo.delete(id = 1))
        }

    @Test
    fun `作者表 - 解析 users 列表并带 email`() =
        runTest {
            enqueue("""[{"id":1,"name":"林清和","email":"a@example.com"}]""")

            val users = repo.users()

            assertEquals(1, users.size)
            assertEquals("林清和", users[0].name)
            assertEquals("a@example.com", users[0].email)
            assertEquals("/users", server.takeRequest().path)
        }
}

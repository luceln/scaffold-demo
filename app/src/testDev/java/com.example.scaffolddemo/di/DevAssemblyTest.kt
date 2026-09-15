package com.example.scaffolddemo.di

import com.example.scaffolddemo.data.mock.MockControl
import com.example.scaffolddemo.data.mock.MockScenario
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
 * `dev` 变体的容器装配测试（只在 `testDev` 源集里，`prod` 变体不编译它）。
 *
 * 它守住的事：dev 的 `flavorModule` 装配的必须**是那具真身**（`MockScenario` + 开关打开），
 * 而不是 prod 那份空实现。两个 flavor 声明的是同名符号 `flavorModule`，
 * 少写一边编译不过 —— 但"写成了空实现"照样编译得过，只有测试能拦住。
 *
 * 为什么这一档**不解析 ViewModel**：dev 的 `Interceptor` 定义里要 `androidContext()`，
 * 而 `networkModule` 是用 `getOrNull<Interceptor>()` 可选挂载它的。这里有个 Koin 的坑 ——
 * **`getOrNull()` 只对"压根没注册"返回 null；注册了但创建过程抛异常，异常会原样抛出**
 * （实测：纯 JVM 里解析任一 ViewModel 都会炸在这一步，MissingAndroidContextException）。
 * 于是"整图可解析"这件事交给 prod 那一档：它的容器里没有任何需要 Context 的定义。
 */
class DevAssemblyTest {
    private fun devKoin(): KoinApplication =
        koinApplication {
            modules(appModule, flavorModule, networkModule(baseUrl = DEV_BASE_URL))
        }

    @Test
    fun `dev 装配 - mock 已启用`() {
        val app = devKoin()
        try {
            assertTrue(app.koin.get<MockControl>().enabled, "dev 变体必须挂 mock，否则零出网不成立")
        } finally {
            app.close()
        }
    }

    @Test
    fun `dev 装配 - 装的是 MockScenario 而不是 prod 的空实现`() {
        val app = devKoin()
        try {
            assertEquals(MockScenario::class, app.koin.get<MockControl>()::class)
        } finally {
            app.close()
        }
    }

    private companion object {
        const val DEV_BASE_URL = "https://dev-api.example.com/"
    }
}

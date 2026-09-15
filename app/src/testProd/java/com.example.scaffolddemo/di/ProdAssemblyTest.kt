package com.example.scaffolddemo.di

import com.example.scaffolddemo.data.mock.MockControl
import com.example.scaffolddemo.ui.ability.NetworkProbeViewModel
import com.example.scaffolddemo.ui.ability.StateDemoViewModel
import com.example.scaffolddemo.ui.home.DetailViewModel
import com.example.scaffolddemo.ui.home.HomeViewModel
import com.example.scaffolddemo.ui.mine.MineViewModel
import com.example.scaffolddemo.ui.search.SearchViewModel
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/*
 * `prod` 变体的容器装配测试（只在 `testProd` 源集里）。
 *
 * 与 `DevAssemblyTest` 成对：两个 flavor 声明的是**同名符号** `flavorModule`，
 * 少写一边编译不过；这一对测试保证两边**语义也对** ——
 * 发布包必须 `enabled = false`（否则界面会出现一个点了没反应的「本地 mock」档）。
 *
 * 这一档顺带承担"整图可解析"：prod 的容器里没有任何需要 Android Context 的定义
 * （不挂拦截器、没有 assets），所以六个 ViewModel 能在纯 JVM 里全部造出来。
 * 界面写 `koinViewModel()` 而 `appModule` 里忘了注册，编译期看不出来、一进那个页面就崩 ——
 * 这个测试把它变成一次构建就能看见的失败。
 */
class ProdAssemblyTest {
    private fun prodKoin(): KoinApplication =
        koinApplication {
            modules(appModule, flavorModule, networkModule(baseUrl = PROD_BASE_URL))
        }

    @Test
    fun `prod 装配 - mock 未启用`() {
        val app = prodKoin()
        try {
            assertFalse(app.koin.get<MockControl>().enabled, "发布包不得启用 mock")
        } finally {
            app.close()
        }
    }

    @Test
    fun `prod 装配 - 全部 ViewModel 可解析`() {
        val app = prodKoin()
        try {
            val koin = app.koin
            assertNotNull(koin.getOrNull<HomeViewModel>(), "HomeViewModel 未注册")
            assertNotNull(koin.getOrNull<DetailViewModel>(), "DetailViewModel 未注册")
            assertNotNull(koin.getOrNull<SearchViewModel>(), "SearchViewModel 未注册")
            assertNotNull(koin.getOrNull<NetworkProbeViewModel>(), "NetworkProbeViewModel 未注册")
            assertNotNull(koin.getOrNull<StateDemoViewModel>(), "StateDemoViewModel 未注册")
            assertNotNull(koin.getOrNull<MineViewModel>(), "MineViewModel 未注册")
        } finally {
            app.close()
        }
    }

    private companion object {
        const val PROD_BASE_URL = "https://api.example.com/"
    }
}

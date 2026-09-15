package com.example.scaffolddemo.di

import com.example.scaffolddemo.data.mock.AssetMockData
import com.example.scaffolddemo.data.mock.MockControl
import com.example.scaffolddemo.data.mock.MockInterceptor
import com.example.scaffolddemo.data.mock.MockScenario
import okhttp3.Interceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * `dev` 源集：**唯一因 flavor 而异的装配**。
 *
 * - `MockControl` → [MockScenario]：界面据此显示「故障注入」控件、启用「本地 mock」档；
 * - `Interceptor` → [MockInterceptor]：`main` 的 `networkModule` 通过 `getOrNull<Interceptor>()`
 *   **可选挂载**它 —— `dev` 有、`prod` 没有，这就是构建变体差异的全部实现。
 *
 * `prod` 源集有一份同名符号的 `flavorModule`（空装配），保证 `DemoApplication` 里的
 * `modules(appModule, flavorModule, networkModule())` 两边都能编译。
 */
val flavorModule =
    module {

        single<MockControl> { MockScenario() }

        // 数据读 `dev` 源集的 `assets/mock/` 目录（不进 `prod` 包）。
        // ⚠️ 注释里千万别写 `mock/*.json` —— Kotlin 的块注释**可以嵌套**，
        // 其中的 `/*` 会开一个嵌套注释，把这个 KDoc 的结尾 `*/` 吃掉，整个文件报
        // "Unclosed comment"（已实测踩过，见 README 坑 #19）。
        single<Interceptor> {
            MockInterceptor(data = AssetMockData(androidContext()), control = get(), json = get())
        }
    }

package com.example.scaffolddemo.di

import com.example.scaffolddemo.data.mock.MockControl
import com.example.scaffolddemo.data.mock.Scenario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.dsl.module

/**
 * `prod` 源集：**空装配**。
 *
 * 它存在的唯一理由是符号对齐 —— `main` 的 `DemoApplication` 引用了 `flavorModule`，
 * `NetworkModule` 会问容器「有没有 `Interceptor`」。`prod` 这边两样都没有：
 * 不挂拦截器、`assets/mock/` 也不进包（R8 会把没人用的 mock 相关类裁掉）。
 *
 * 但 [MockControl] 仍要可解析：界面要知道「本构建没有 mock」，据此禁用「本地 mock」档。
 */
val flavorModule =
    module {
        single<MockControl> { DisabledMockControl }
    }

/** `prod` 的空实现：永不启用、档位恒为正常、驱动无效。 */
private object DisabledMockControl : MockControl {
    override val enabled: Boolean = false

    override val scenario: StateFlow<Scenario> = MutableStateFlow(Scenario.NORMAL)

    override fun drive(scenario: Scenario) {
        // 发布包没有 mock 可驱动，刻意什么都不做。
    }
}

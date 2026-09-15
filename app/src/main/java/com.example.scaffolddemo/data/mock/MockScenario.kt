package com.example.scaffolddemo.data.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [MockControl] 的 `dev` 实现：内存态档位，进程存活期内有效，重启回到 [Scenario.NORMAL]。
 *
 * 刻意不做持久化 —— 这是个**调试开关**，重启回到正常态比「上次切成失败、这次还失败」更好用。
 */
class MockScenario : MockControl {
    private val _scenario = MutableStateFlow(Scenario.NORMAL)

    override val enabled: Boolean = true

    override val scenario: StateFlow<Scenario> = _scenario.asStateFlow()

    override fun drive(scenario: Scenario) {
        _scenario.value = scenario
    }
}

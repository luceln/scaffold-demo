package com.example.scaffolddemo.ui.ability

import androidx.lifecycle.ViewModel
import com.example.scaffolddemo.data.mock.MockControl
import com.example.scaffolddemo.data.mock.Scenario
import kotlinx.coroutines.flow.StateFlow

/**
 * 状态管理演示屏的 ViewModel。
 *
 * 它本身几乎没有状态 —— 存在的理由是**让界面不碰容器**：这一屏要读、要写全局 mock 档位
 * （[MockControl]），而 `MockControl` 在 `prod` 包里是空实现。让 ViewModel 去承担这个差异，
 * 界面就只剩一句话：「能不能驱动」由 [mockEnabled] 决定。
 */
class StateDemoViewModel(
    private val mockControl: MockControl,
) : ViewModel() {
    /** `prod` 下为 false ⇒ 界面不显示故障注入控件。 */
    val mockEnabled: Boolean = mockControl.enabled

    /** 当前生效的档位：切走再回来，选中的还是同一个。 */
    val scenario: StateFlow<Scenario> = mockControl.scenario

    /**
     * 驱动下一次请求的行为。
     *
     * 注意驱动的是**全局数据层**：切到「空」之后回首页刷新，请求真的返空数组 ——
     * 四态演示由真实数据层产生，而不是在这一屏里画四个假界面。
     */
    fun drive(scenario: Scenario) {
        mockControl.drive(scenario)
    }
}

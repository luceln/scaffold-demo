package com.example.scaffolddemo.data.mock

import kotlinx.coroutines.flow.StateFlow

/**
 * mock 的控制面：**唯一因 flavor 而异的一份装配**（`dev` 有实现、`prod` 是空实现）。
 *
 * 为什么要这层抽象，而不是直接判 `BuildConfig`：
 * - `prod` 包里**不存在** mock 数据与拦截器，界面却要能问出「本构建有没有 mock」；
 * - 状态演示屏要驱动「下一次请求真的返空 / 真的超时」，驱动对象必须能被注入与替换。
 *
 * 两个实现：`dev` 源集用 [MockScenario]（真注入），`prod` 源集用空实现（`enabled = false`）。
 */
interface MockControl {
    /** 本构建是否挂了 mock 拦截器。`prod` 恒为 false ⇒ 界面上禁用 mock 档。 */
    val enabled: Boolean

    /** 当前故障注入档位，供拦截器读取。 */
    val scenario: StateFlow<Scenario>

    /** 切换档位。`prod` 的空实现什么都不做。 */
    fun drive(scenario: Scenario)
}

/** 故障注入档位：让四态演示由**真实数据层**驱动，而不是画出来的假态。 */
enum class Scenario {
    /** 正常：返回本地中文 mock 数据。 */
    NORMAL,

    /** 空：返回 `[]` ⇒ 界面落空态（不是错误态）。 */
    EMPTY,

    /** 失败：抛 `SocketTimeoutException` ⇒ 走与真网络失败**同一条**错误映射。 */
    FAIL,
}

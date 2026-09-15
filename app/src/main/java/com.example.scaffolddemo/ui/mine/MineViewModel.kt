package com.example.scaffolddemo.ui.mine

import androidx.lifecycle.ViewModel
import com.example.scaffolddemo.data.mock.MockControl

/**
 * 「我的」页的 ViewModel。
 *
 * 这一页展示的多半是**构建期常量**（`BuildConfig` 里的 applicationId / BASE_URL / 版本），
 * 那些直接从 `BuildConfig` 读即可。唯一需要走容器的是「数据来源」那一项 ——
 * 它取决于本 flavor 有没有挂 mock 拦截器，而 `prod` 包里 mock 相关类压根不存在，
 * 只能问 [MockControl]。
 *
 * 就为这一个布尔值开一个 ViewModel 值不值？值 —— 界面里出现 `get()`/`koinInject()` 这类
 * **服务定位调用**，就再也说不清"这一页依赖了什么"。依赖写在构造参数上，一眼看得见。
 */
class MineViewModel(
    mockControl: MockControl,
) : ViewModel() {
    /** true = 本构建的数据来自本地 mock（零出网）；false = 走真实网络栈。 */
    val mockEnabled: Boolean = mockControl.enabled
}

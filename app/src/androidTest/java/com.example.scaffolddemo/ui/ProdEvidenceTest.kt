package com.example.scaffolddemo.ui

import org.junit.Assume
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * **prod 包的活证据**（3 屏）：装另一个包（`applicationId` 带不带 `.dev` 后缀，两包可共存），
 * 用同一份测试代码跑出 prod 侧的三条差异 —— 不靠"我觉得应该是这样"。
 *
 * 与 [ScreenshotWalkthroughTest] 是**互补而非重复**：那边证 dev 的 mock 通路，
 * 这边证 prod 的真网络通路 + mock 开关整条消失。
 *
 * ```
 * ./gradlew connectedProdDebugAndroidTest
 * ```
 *
 * 本类在 `dev` 变体下会被 `assumeFalse` 整批跳过（报告里如实标记 skipped，不假装跑过）。
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ProdEvidenceTest : ScreenshotTestBase() {
    @Before
    fun requireProdVariant() {
        Assume.assumeFalse("prod 取证只在 prod 变体跑；dev 包请跑 ScreenshotWalkthroughTest", mock.enabled)
    }

    /** 屏 `17`：prod 的四项活证据（应用名 / applicationId / BASE_URL / 数据来源）。*/
    @Test
    fun e17_prodMineEvidence() {
        awaitText("我的")
        click("我的")
        awaitText("应用信息")
        capture("17-prod-mine-evidence", "我的（prod）", "应用信息 = prod 活证据", "装 prod 包 → 底栏「我的」")
        swipeUpOnce()
        capture("17b-prod-mine-bottom", "我的（prod）", "下段（设置项）", "上滑一屏")
    }

    /** 屏 `17c`：prod 网络实测 —— 档位切换器**整条不显示**（`TabRow` 无单档禁用的取舍）。*/
    @Test
    fun e17c_prodNetworkProbe() {
        awaitText("我的")
        click("能力")
        click("网络实测")
        awaitText("当前构建未启用 mock")
        capture("17c-prod-probe", "网络实测（prod）", "无档位切换器", "prod → 能力 → 网络实测")
    }

    /**
     * 屏 `18`：prod 首页失败态。
     *
     * 不需要任何操作：prod 的 `BASE_URL` 是**占位域名**（`https://api.example.com/`），
     * 冷启必然失败 ⇒ 错误映射 NET-002 落到界面上。这正是模板刻意保留的真实失败路径。
     */
    @Test
    fun e18_prodHomeFailure() {
        awaitText("网络连接失败")
        capture("18-prod-home-error", "首页（prod）", "失败（NET-002 网络连接失败）", "prod 冷启（BASE_URL 是占位域名）")
    }
}

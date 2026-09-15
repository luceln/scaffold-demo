package com.example.scaffolddemo.ui

import android.os.Build
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.scaffolddemo.data.mock.Scenario
import org.junit.Assume
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * **闸门二走查的取证机**：一条命令跑完，把 6 组 20+ 屏拍下来。
 *
 * 用法（真机连着、dev 变体）：
 *
 * ```
 * ./gradlew connectedDevDebugAndroidTest
 * adb pull /sdcard/Android/data/<applicationId>/files/screenshots ./screenshots
 * ```
 *
 * ⚠️ 两条真机侧的前提（MIUI / Android 14 实测踩过）：
 *
 * 1. **系统设置里给本包开「后台弹出界面」+「自启动」**（MIUI 特殊权限，adb 给不了，
 *    且**每次彻底卸载重装都会清掉**，要重新开）。不开的话 instrumentation 拉不起
 *    MainActivity（`MIUILOG- Permission Denied Activity`），全部用例卡死在等 Activity 就绪。
 * 2. **`connectedXxxAndroidTest` 跑完会把主 App 卸载**（AGP 默认清场），截图目录随 App
 *    一起被清。要保住截图产物，在 App 已装好的前提下用 `am instrument` 直跑：
 *
 * ```
 * adb shell am instrument -w \
 *   -e class com.example.miu1demo.ui.ScreenshotWalkthroughTest \
 *   com.example.miu1demo.dev.test/androidx.test.runner.AndroidJUnitRunner
 * ```
 *
 * 产物两样：`*.png`（截图）+ `index.tsv`（屏号 / 页面 / 状态 / 复现方式）——
 * 走查页 `ui-patrol/miu1.html` 就是从这两样生成的，不用手工对图。
 *
 * **屏幕编号与 `MIU1-gate2-checklist.md` 一一对应**，方法名前缀就是屏号，
 * 所以 `-e class ...#a03_homeEmpty` 能单独重跑一屏（改完一屏只回归一屏）。
 *
 * 为什么每条用例都重启一次 App 也要这么写：状态干净比省几秒重要 ——
 * 一条用例里串完整流程，前一步的残留（滚动位置 / 筛选 / 导航栈）会污染后一张图，
 * 而走查要的恰恰是"这一屏在这一刻长这样"。
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ScreenshotWalkthroughTest : ScreenshotTestBase() {
    /**
     * 本类全部用例的数据都来自 mock 资产 ⇒ **只在 `dev` 变体跑**。
     * `prod` 变体下 `assumeTrue` 会让它们整批标记为 skipped（而不是假装跑过）——
     * prod 的取证在 [ProdEvidenceTest]。
     */
    @Before
    fun requireMockVariant() {
        Assume.assumeTrue("走查用例只在 dev 变体跑（界面数据全部来自 mock 资产）", mock.enabled)
    }

    // ================== 0 / A 组：外壳 · 首页 · 详情 ==================

    /** 屏 `00` + `02`：同一画面两个视角（外壳骨架 / 列表内容），合拍一张不浪费。 */
    @Test
    fun a00_shellAndHomeLoaded() {
        rule.waitForIdle()
        awaitText(FIRST_TITLE)
        capture("00-02-home-loaded", "首页", "已加载（外壳 + 内容）", "冷启即首页，等数据落地")
    }

    /** 屏 `01`：首页加载中。靠顶栏「刷新」重新进入 Loading，450ms mock 延迟给足截图窗口。 */
    @Test
    fun a01_homeLoading() {
        awaitText(FIRST_TITLE)
        clickIcon("刷新")
        capture("01-home-loading", "首页", "加载中（骨架屏）", "顶栏「刷新」后立刻截图，不等 idle")
    }

    /** 屏 `02b`：筛选作者。*/
    @Test
    fun a02b_homeFiltered() {
        awaitText(FIRST_TITLE)
        click("林清和")
        capture("02b-home-filtered", "首页", "已筛选（林清和）", "点筛选行第 2 档")
    }

    /** 屏 `03`：空态。「郑之南」0 篇，且是第 4 档 ⇒ 先横向滚进视野再点。 */
    @Test
    fun a03_homeEmpty() {
        awaitText(FIRST_TITLE)
        rule
            .onAllNodes(hasText("郑之南"))
            .onFirst()
            .performScrollTo()
            .performClick()
        rule.waitForIdle()
        capture("03-home-empty", "首页", "空态（该作者 0 篇）", "筛选行滚到「郑之南」并点击")
    }

    /** 屏 `04`：失败态。走查时「状态管理」切失败档是同一条路径，这里直接注档更快。 */
    @Test
    fun a04_homeError() {
        awaitText(FIRST_TITLE)
        mock.drive(Scenario.FAIL)
        clickIcon("刷新")
        awaitText("请求超时，请稍后重试")
        capture("04-home-error", "首页", "失败（NET-001 超时）", "注入 Scenario.FAIL 后点「刷新」")
    }

    /** 屏 `05`：详情已加载。*/
    @Test
    fun a05_detailLoaded() {
        awaitText(FIRST_TITLE)
        click(FIRST_TITLE)
        awaitText(DETAIL_MARK)
        capture("05-detail-loaded", "详情", "已加载", "首页点第 1 张卡片")
    }

    /** 屏 `05b`：列表滚到中段后进入详情。*/
    @Test
    fun a05b_detailFromMiddle() {
        awaitText(FIRST_TITLE)
        scrollAndClick(MID_TITLE)
        awaitText(DETAIL_MARK)
        capture("05b-detail-from-middle", "详情", "已加载（列表中部进入）", "首页列表滚到第 12 篇后点入")
    }

    /** 屏 `06`：详情加载中。点完立刻拍，不等 idle。*/
    @Test
    fun a06_detailLoading() {
        awaitText(FIRST_TITLE)
        rule.onAllNodes(hasText(FIRST_TITLE) and hasClickAction()).onFirst().performClick()
        capture("06-detail-loading", "详情", "加载中（骨架屏）", "点卡片后立刻截图")
    }

    // ================== B 组：搜索（SearchBar 全范式） ==================

    /** 屏 `08`：搜索未输入 —— 无标题栏、右侧「取消」、热门词是列表行。聚焦真实保留，截图前收键盘。*/
    @Test
    fun b08_searchIdle() {
        awaitText(FIRST_TITLE)
        clickIcon("搜索")
        awaitText("热门搜索")
        hideIme()
        capture("08-search-idle", "搜索", "未输入（热门词）", "首页顶栏搜索 action（聚焦后收键盘）")
    }

    /** 屏 `08b`：点「取消」后回到首页（新行为：原来是返回箭头）。*/
    @Test
    fun b08b_searchCancelled() {
        awaitText(FIRST_TITLE)
        clickIcon("搜索")
        awaitText("热门搜索")
        click("取消")
        awaitText(FIRST_TITLE)
        capture("08b-search-cancelled", "首页", "已加载（退出搜索后）", "搜索页点「取消」")
    }

    /**
     * 屏 `09`：有结果。关键词用 **Koin**（标题里真有 4 篇）。
     *
     * ⚠️ 不用清单里写的「林」——`filterByTitle` 只匹配**标题**，而「林」只出现在作者名里，
     * 输它必落空态（第 10 屏）。清单一并已更正。
     */
    @Test
    fun b09_searchResults() {
        awaitText(FIRST_TITLE)
        clickIcon("搜索")
        awaitText("热门搜索")
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("Koin")
        awaitText("找到 4 篇")
        hideIme()
        capture("09-search-results", "搜索", "有结果（关键词 Koin）", "搜索页输入 Koin（收键盘）")
    }

    /** 屏 `10`：无结果。*/
    @Test
    fun b10_searchEmpty() {
        awaitText(FIRST_TITLE)
        clickIcon("搜索")
        awaitText("热门搜索")
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("zzz")
        awaitText("没有标题包含")
        hideIme()
        capture("10-search-empty", "搜索", "无结果（关键词 zzz）", "搜索页输入 zzz（收键盘）")
    }

    // ================== C 组：能力 tab 及其下级页 ==================

    /** 屏 `11`：能力主视图（`BasicComponent` 列表行 + 图标）。*/
    @Test
    fun c11_ability() {
        awaitText(FIRST_TITLE)
        click("能力")
        awaitText("照着抄的样板")
        capture("11-ability", "能力", "主视图", "底栏「能力」")
    }

    /** 屏 `12`：组件画廊，长页面分三段拍（miuix 组件是否都可辨认）。*/
    @Test
    fun c12_gallery() {
        awaitText(FIRST_TITLE)
        click("能力")
        click("组件画廊")
        awaitText("档位切换")
        capture("12a-gallery-top", "组件画廊", "上段（档位 / 按钮 / 输入框）", "能力 → 组件画廊")
        swipeUpOnce()
        capture("12b-gallery-mid", "组件画廊", "中段（列表项 / 卡片三档）", "上滑一屏")
        swipeUpOnce()
        capture("12c-gallery-bottom", "组件画廊", "下段（进度 / 徽标 / 分隔线）", "再上滑一屏")
    }

    /** 屏 `13a/13b/13c`：网络实测三态（唯一真出网通路）。「请求」是顶栏图标按钮（contentDescription）。*/
    @Test
    fun c13_networkProbe() {
        awaitText(FIRST_TITLE)
        click("能力")
        click("网络实测")
        awaitText("默认不发请求")
        capture("13a-probe-idle", "网络实测", "初态（未请求）", "能力 → 网络实测")
        clickIcon("请求")
        awaitText("本地文章")
        capture("13b-probe-mock", "网络实测", "本地 mock 档 · 已请求", "第 1 档 + 「请求」")
        click("真实网络")
        clickIcon("请求")
        awaitText("实时文章")
        capture("13c-probe-real", "网络实测", "真实网络档 · 已请求", "第 2 档 + 「请求」（真出网）")
    }

    /** 屏 `14a–d`：状态管理四态（档位从分段按钮换成 `TabRow`）。进屏默认在「成功」档。*/
    @Test
    fun c14_stateDemo() {
        awaitText(FIRST_TITLE)
        click("能力")
        click("状态管理")
        awaitText("当前状态：成功")
        click("加载中")
        awaitText("当前状态：加载中")
        capture("14a-state-loading", "状态管理", "加载中", "能力 → 状态管理 → 点「加载中」档")
        click("空态")
        awaitText("当前状态：空态")
        capture("14b-state-empty", "状态管理", "空态", "点「空态」档")
        click("失败")
        awaitText("当前状态：失败")
        capture("14c-state-error", "状态管理", "失败", "点「失败」档")
        click("成功")
        awaitText("当前状态：成功")
        capture("14d-state-success", "状态管理", "成功", "点「成功」档")
    }

    // ================== D 组：我的 tab 与状态保持 ==================

    /** 屏 `15` / `15b` / `16`：我的 tab 上下两段，下半就是 dev 活证据。*/
    @Test
    fun d15_mine() {
        awaitText(FIRST_TITLE)
        click("我的")
        awaitText("应用信息")
        capture("15-mine", "我的", "配置与设置（上段）", "底栏「我的」")
        swipeUpOnce()
        capture("15b-mine-dev-evidence", "我的", "应用信息 = dev 活证据（下段）", "上滑一屏")
    }

    /** 屏 `19a` / `19b`：滚动位置保持（进详情再返回，位置不动）。*/
    @Test
    fun d19a_scrollKept() {
        awaitText(FIRST_TITLE)
        scrollAndClick(MID_TITLE)
        awaitText(DETAIL_MARK)
        clickIcon("返回")
        awaitText(MID_TITLE)
        capture("19a-19b-scroll-kept", "首页", "滚动位置保持", "滚到中段进详情 → 返回")
    }

    /** 屏 `19c`：筛选状态保持。*/
    @Test
    fun d19c_filterKept() {
        awaitText(FIRST_TITLE)
        click("林清和")
        click(FIRST_TITLE)
        awaitText(DETAIL_MARK)
        clickIcon("返回")
        awaitText(FIRST_TITLE)
        capture("19c-filter-kept", "首页", "筛选状态保持（林清和）", "选作者 → 进详情 → 返回")
    }

    // ================== F 组：深色主题 ==================

    /** 屏 `20a/b/c`：深色下三屏（首页 / 我的 / 搜索）。*/
    @Test
    fun f20_darkTheme() {
        Assume.assumeTrue(
            "深色走查需要 API 31+（setApplicationNightMode）",
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
        )
        awaitText(FIRST_TITLE)
        setAppNightMode(night = true)
        try {
            awaitText(FIRST_TITLE)
            capture("20a-dark-home", "首页（深色）", "已加载", "切本应用夜模式后看图")
            click("我的")
            awaitText("应用信息")
            capture("20b-dark-mine", "我的（深色）", "配置与设置", "深色下切「我的」")
            click("首页")
            clickIcon("搜索")
            awaitText("热门搜索")
            hideIme()
            capture("20c-dark-search", "搜索（深色）", "未输入", "深色下进搜索页（收键盘）")
        } finally {
            // 夜模式是**应用级持久设置**：不还原，这个包在用户手机上会一直是深色。
            setAppNightMode(night = false)
        }
    }

    private companion object {
        /** mock 集第 1 篇标题：列表"已就绪"的判据，也是进详情的入口。 */
        const val FIRST_TITLE = "Koin 让 ViewModel 的装配只写一遍"

        /** 第 12 篇（作者换人）：滚到中段后的点击目标，用来证明"列表中部进入"也正常。 */
        const val MID_TITLE = "拦截器只换最后一跳，调用链还是真的"

        /**
         * 详情页独有内容：作者邮箱（列表页没有它）⇒ 是"详情真的加载完了"的判据。
         * 只断言邮箱**域**而非完整邮箱：第 12 篇作者是周砚不是林清和，
         * 判据写死某个人 ⇒ 换一篇点就永远等不到（首轮真机实测就是这么红的）。
         */
        const val DETAIL_MARK = "@example.com"
    }
}

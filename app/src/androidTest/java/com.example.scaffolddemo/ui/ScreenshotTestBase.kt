package com.example.scaffolddemo.ui

import android.app.UiModeManager
import android.graphics.Bitmap
import android.os.Build
import android.view.WindowInsets
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.scaffolddemo.MainActivity
import com.example.scaffolddemo.data.mock.MockControl
import com.example.scaffolddemo.data.mock.Scenario
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.koin.mp.KoinPlatform
import java.io.File
import java.io.FileOutputStream

/**
 * 真机 UI 走查截图的共用底座：**一个启动真 App 的 rule + 四个动作原语**。
 *
 * 三个刻意的选择，各自的理由：
 *
 * 1. **`createAndroidComposeRule<MainActivity>()` 而不是 `createComposeRule()`** ——
 *    走查要的是「**应用真的跑起来**」：真 Application、真 Koin 装配、真导航栈、真网络栈。
 *    单屏渲染出的图回答的是"这个组件长这样"，不是"这个 App 长这样"。
 * 2. **截图用 `UiAutomation.takeScreenshot()` 而不是 `captureToImage()`** ——
 *    前者是**系统级抓屏**，不参与 Compose 的空闲同步，于是**抓得到加载中这类短命状态**
 *    （`performClick` 返回时请求还没回来；`MockInterceptor` 那 450ms 延迟就是留给它的窗口）。
 *    后者只截 Compose 画布且必须先等 idle，骨架屏必然抓空。
 *    代价：图里带状态栏 / 导航栏 / 软键盘 —— 与 adb 截图一致，走查看的本来就是真机观感。
 * 3. **故障注入直接 drive 内存档位** —— instrumentation 与 app 同进程，
 *    容器里的 [MockControl] 就是界面正在读的那个实例，不需要额外开测试后门。
 *
 * ⚠️ `MockControl` 在 `prod` 变体是空实现（`drive` 什么都不做）⇒ 依赖 mock 数据的用例
 * 必须自己 `Assume.assumeTrue(mock.enabled)` 跳过，别假装跑过。
 */
abstract class ScreenshotTestBase {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    /** 被测 App 的 Context（**不是**测试 apk 的）—— 外置目录与系统服务都从它取。 */
    protected val appContext = instrumentation.targetContext

    /**
     * 当前构建的 mock 控制面。`dev` 是真实现，`prod` 是空实现 ——
     * 所以 `prod` 下 `mock.enabled == false`，`drive()` 是空操作。
     */
    protected val mock: MockControl get() = KoinPlatform.getKoin().get()

    private val outDir: File by lazy {
        File(appContext.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
    }

    // 每个用例前后都把档位复位：档位是**进程级单例**，不复位会串味到下一条用例。
    @Before
    fun restoreScenarioBefore() {
        mock.drive(Scenario.NORMAL)
    }

    @After
    fun restoreScenarioAfter() {
        mock.drive(Scenario.NORMAL)
    }

    /**
     * 抓一张屏并落盘。同时往 `index.tsv` 追一行，让走查页能把
     * 「文件 ↔ 屏号 ↔ 页面 · 状态 ↔ 复现方式」对上 —— 走查页就是从它生成的。
     *
     * 落盘位置：`/sdcard/Android/data/<applicationId>/files/screenshots/`，
     * 用 `adb pull` 取回（debuggable 包可取）。
     */
    protected fun capture(
        name: String,
        page: String,
        state: String,
        how: String,
    ) {
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        requireNotNull(bitmap) { "takeScreenshot 返回 null，抓不到 $name" }
        FileOutputStream(File(outDir, "$name.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
        File(outDir, "index.tsv").appendText("$name\t$page\t$state\t$how\n")
    }

    /**
     * 点「文本 + 真的可点」的节点。
     *
     * 比 `onNodeWithText` 稳：顶栏标题与底栏 tab 常常**同名**（切到「能力」tab 后，
     * 顶栏标题与底栏标签都是「能力」）—— 只按文本找会撞上"匹配到多个节点"。
     * 加上 `hasClickAction` 后只剩底栏那个可点项。
     */
    protected fun click(text: String) {
        rule.onAllNodes(hasText(text) and hasClickAction()).onFirst().performClick()
        rule.waitForIdle()
    }

    /** 点只有 `contentDescription` 的图标按钮（顶栏 action / 返回箭头）。 */
    protected fun clickIcon(contentDescription: String) {
        rule.onNodeWithContentDescription(contentDescription).performClick()
        rule.waitForIdle()
    }

    /**
     * 先把列表滚到含这段文字的条目、再点它。
     *
     * 给"长列表深处的条目"用：`swipeUp()` 的滑动距离是固定的，赌距离必翻车
     * （目标没进视野就是 "There are no existing nodes for that selector"）。
     *
     * 为什么不"先定位装着目标的滚动容器再滚"：**LazyList 未滚到的条目不在语义树上**，
     * "含目标后代"的容器选择器在滚动前必然落空（首轮真机实测就是这么红的）。
     * 同屏又常有多个可滚动容器（横向筛选 TabRow + 竖向数据列表），不能只取一个。
     * 所以逐个尝试：滚一个容器 → 查目标出现没有 → 没有就换下一个。
     */
    protected fun scrollAndClick(text: String) {
        val target = hasText(text, substring = true)
        val scrollables = rule.onAllNodes(hasScrollAction())
        val total = scrollables.fetchSemanticsNodes().size
        val visible: () -> Boolean = {
            rule.onAllNodes(target and hasClickAction()).fetchSemanticsNodes().isNotEmpty()
        }
        var index = 0
        while (!visible() && index < total) {
            runCatching { scrollables[index].performScrollToNode(target) }
            rule.waitForIdle()
            index++
        }
        check(visible()) { "滚遍了 $total 个可滚动容器也没看到：$text" }
        click(text)
    }

    /**
     * 收起软键盘再截图 —— 键盘一挡半屏，走查页上只剩键盘看不见内容。
     * 自动聚焦的真实行为保留（输入框焦点态还在），只是把挡板的帘子拉开。
     *
     * 三条真机实测换来的教训：
     *
     * 1. Compose 的 `performCloseSoftKeyboard` 已在新版 compose-ui-test 移除；
     *    `InputMethodManager.hideSoftInputFromWindow` 在 MIUI 上会**静默失败**
     *    （返回 false、键盘纹丝不动）。API 30+ 走 `WindowInsetsController.hide(ime())`。
     * 2. **收起是动画，不确认就等于没收**：`waitForIdle` 只同步 Compose，不同步 IME。
     * 3. **hide 会被排队的 show 吃掉**：进搜索页自动聚焦的 show 请求还在队列里时调
     *    `hide()`，insets 会先短暂报告"已收起"、几百 ms 后键盘又补弹出来
     *    （首轮真机 08 屏就是这么翻车的：测试全绿，截图里键盘完整在）。
     *    所以收起后要**持续探测一段时间确认稳定**，再弹就再收；
     *    最终仍收不住就 fail —— 宁可测试红，也不把键盘截图交给走查。
     */
    protected fun hideIme() {
        val activity = rule.activity
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val imm = appContext.getSystemService(android.view.inputmethod.InputMethodManager::class.java)
            instrumentation.runOnMainSync {
                imm.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
            }
            // API < 30 拿不到 ime() 的可见性，只能给动画留时间；现代真机走不到这分支。
            Thread.sleep(IME_FALLBACK_SETTLE_MS)
            rule.waitForIdle()
            return
        }
        // 深色用例切日夜模式会重建 Activity：重建窗口期 decorView 还没挂上新窗口，
        // rootWindowInsets 会瞬时为 null ⇒ 判"未见键盘"，窗口过去自然恢复（否则 NPE 干掉整条用例）。
        val imeVisible: () -> Boolean = {
            rule.activity.window.decorView.rootWindowInsets
                ?.isVisible(WindowInsets.Type.ime()) == true
        }

        fun hideNow() {
            instrumentation.runOnMainSync {
                val a = rule.activity
                val controller = a.window.insetsController
                if (controller != null) {
                    controller.hide(WindowInsets.Type.ime())
                } else {
                    // insetsController 缺位（罕见）时退回 IMM 通道，尽力而为。
                    appContext
                        .getSystemService(android.view.inputmethod.InputMethodManager::class.java)
                        .hideSoftInputFromWindow(a.window.decorView.windowToken, 0)
                }
            }
            rule.waitUntil(IME_CLOSE_TIMEOUT_MS) { !imeVisible() }
        }
        hideNow()
        // 收起后键盘可能被排队的 show 补弹：探测窗口内再弹就再收。
        repeat(IME_RESHOW_RETRIES) {
            Thread.sleep(IME_STABLE_PROBE_MS)
            if (imeVisible()) hideNow()
        }
        check(!imeVisible()) { "软键盘收不住（InsetsController.hide 被补弹压制），截图只会拍到键盘" }
        rule.waitForIdle()
    }

    /** 上滑一屏 —— 长页面分段拍照用。 */
    protected fun swipeUpOnce() {
        rule.onRoot().performTouchInput { swipeUp() }
        rule.waitForIdle()
    }

    /**
     * 等**含**这段文字的节点出现（子串匹配）—— 真网络请求这类耗时不定的场合用它，别 sleep 赌。
     *
     * 用子串而非全等：界面文案多是模板串（"默认不发请求 —— dev 构建下…"），
     * 全等匹配逼着把整句一字不差抄进测试，正文改一个字测试就红。
     */
    protected fun awaitText(
        text: String,
        timeoutMillis: Long = 20_000,
    ) {
        rule.waitUntil(timeoutMillis) {
            rule.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * 切**本应用**的日夜模式（`setApplicationNightMode`，API 31+）。
     *
     * 为什么不用 `adb shell cmd uimode night yes`：那是**全局**开关，走查完忘了切回来
     * 会改掉用户整机设置。应用级开关只影响本包，`@After` 里恢复即可。
     *
     * @return 是否真的切了（API < 31 返回 false，调用方据此跳过深色用例）。
     */
    protected fun setAppNightMode(night: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        appContext.getSystemService(UiModeManager::class.java).setApplicationNightMode(
            if (night) UiModeManager.MODE_NIGHT_YES else UiModeManager.MODE_NIGHT_NO,
        )
        // 日夜模式变更会让系统重建 Activity：等新 configuration 走完再截，否则拍到旧主题
        Thread.sleep(NIGHT_MODE_SETTLE_MS)
        rule.waitForIdle()
        return true
    }

    private companion object {
        /** 日夜模式切换后等系统重建 Activity 的时长；实测 800ms 不稳，给足到 1.5s。 */
        const val NIGHT_MODE_SETTLE_MS = 1_500L

        /** 等键盘收起动画走完的上限；正常几百 ms，超时说明收起请求被系统吞了 ⇒ 红给测试看。 */
        const val IME_CLOSE_TIMEOUT_MS = 3_000L

        /** 收起后的补弹探测：单次探测间隔与重试次数（合计约 1.2s 的稳定窗口）。 */
        const val IME_RESHOW_RETRIES = 3
        const val IME_STABLE_PROBE_MS = 400L

        /** API < 30 的兜底等待（无可见性可查，纯给动画留时间）。 */
        const val IME_FALLBACK_SETTLE_MS = 600L
    }
}

package com.example.scaffolddemo.ui.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.scaffolddemo.R
import com.example.scaffolddemo.ui.AppNavHost
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

/**
 * 应用外壳：**三段式**（顶栏 action + 内容 + 底部 tab）。这是任何 App 的默认骨架，
 * 脚手架必须给出来 —— 不给，使用者就得从零写一遍。
 *
 * 设计取舍（与原型 `lowfi-p0-v5.html` 一一对应）：
 * - **三个顶层目的地**（首页 / 能力 / 我的），每个 tab 有自己的回退栈；
 * - **切 tab 保状态**：`saveState` + `restoreState` + `launchSingleTop` ⇒
 *   切走再切回，滚动位置与数据原样保留，连点也不叠栈；
 * - **搜索不占 tab**：首页顶栏 action 打开全屏页；
 * - **下级页整条底栏消失**：底栏属于"顶层目的地之间切换"，不属于"进入某一项的内部"
 *   （所以下级页用 [SubPageScaffold]，它压根没有 bottomBar）。
 *
 * 为什么把外壳拆成三个 composable 而不是一个大 `Scaffold`：让**每个界面自带自己的顶栏**，
 * 页与页之间没有隐式耦合，抄一页走就能用 —— 这是脚手架的使用方式决定的。
 */
@Composable
fun AppShell(navController: NavHostController = rememberNavController()) {
    AppNavHost(
        navController = navController,
        onSelectTab = { destination -> navController.switchTab(destination) },
    )
}

/**
 * 切 tab 的标准姿势（导航组件的通用形态，NIA 的 `rememberNavigationState` 同理）：
 * - `popUpTo(起始目的地) { saveState = true }` —— 离开时把当前 tab 的栈存起来；
 * - `restoreState = true` —— 回来时把它原样恢复；
 * - `launchSingleTop = true` —— 连点同一个 tab 不叠栈。
 */
private fun NavHostController.switchTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * 顶层目的地的脚手架：顶栏（标题 + 可选 action）+ 底栏（3 个 tab）。
 *
 * 与 M3 版的三处差异（都是 miuix 的取向）：
 * - `TopAppBar` 收 `String` 而不是 `@Composable`（标题不是槽位，是文本），副标题走 `subtitle` 参数；
 * - `NavigationBarItem` 的 `label` 同样是 `String`，图标收 [androidx.compose.ui.graphics.vector.ImageVector]；
 * - 底栏选中态**由库负责**（胶囊指示器 + 语义色），这里只传 `selected=`；
 *   我们额外做的一件事是**换字重**（选中 `Medium` / 未选中 `Regular`），这是 HyperOS 的取向。
 */
@Composable
fun TopLevelScaffold(
    destination: TopLevelDestination,
    onSelectTab: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = stringResource(destination.labelRes),
                actions = actions,
            )
        },
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = item == destination,
                        onClick = { onSelectTab(item) },
                        icon = if (item == destination) item.selectedIcon else item.icon,
                        label = stringResource(item.labelRes),
                    )
                }
            }
        },
        content = content,
    )
}

/**
 * 下级页的脚手架：顶栏带返回箭头，**没有底栏**。
 *
 * 返回箭头与系统返回键走同一条路（`popBackStack()`），不会出现"按箭头回去了、按返回键没回"。
 */
@Composable
fun SubPageScaffold(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = actions,
            )
        },
        content = content,
    )
}

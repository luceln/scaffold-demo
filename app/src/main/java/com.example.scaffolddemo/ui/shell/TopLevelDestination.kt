package com.example.scaffolddemo.ui.shell

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.scaffolddemo.R
import com.example.scaffolddemo.ui.route.AbilityRoute
import com.example.scaffolddemo.ui.route.HomeRoute
import com.example.scaffolddemo.ui.route.MineRoute
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.ListView

/**
 * 三个顶层目的地（底部 tab）。
 *
 * 形态照旗舰参考工程 Now in Android 的 `TopLevelNavItem`：路由 + 标题资源 + 图标三件套集中一处，
 * 底栏与标题栏都从这里派生 —— 加一个 tab 只改这一个枚举 + 一张界面。
 *
 * 图标来自 miuix 图标集（`miuix-icons`，156 枚 HyperOS 语义图标），**不含任何 drawable 里的 XML 图标**。
 * 集里缺的语义补在 `ui/icon/AppIcons.kt`（骨架里带了一枚可抄的示范）。
 *
 * **为什么每项存两枚图标**：miuix 的每枚图标有 6 档字重（Light/Normal/Regular/Medium/Demibold），
 * HyperOS 用**字重**区分底栏选中态 —— 选中的一档更粗，而不是换实心/空心。
 * 这是 Material 图标集没有的一层，用上了才有 HyperOS 的观感。
 */
enum class TopLevelDestination(
    val route: Any,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    HOME(HomeRoute, R.string.tab_home, MiuixIcons.Regular.Home, MiuixIcons.Medium.Home),
    ABILITY(AbilityRoute, R.string.tab_ability, MiuixIcons.Regular.ListView, MiuixIcons.Medium.ListView),
    MINE(MineRoute, R.string.tab_mine, MiuixIcons.Regular.Contacts, MiuixIcons.Medium.Contacts),
}

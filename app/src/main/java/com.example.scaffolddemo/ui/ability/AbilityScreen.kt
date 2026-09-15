package com.example.scaffolddemo.ui.ability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scaffolddemo.R
import com.example.scaffolddemo.ui.shell.TopLevelDestination
import com.example.scaffolddemo.ui.shell.TopLevelScaffold
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Report
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 能力 tab 主视图：**脚手架给了什么能力，一屏看全**。
 *
 * 定位是"给人抄的样板间"—— 每一项都标清**去哪看**：「见首页」「见我的」「本页可进」「P1 未做」。
 * 不标归位的话，使用者知道有 DI/导航，却不知道样板在哪一页。
 *
 * 图标全部来自 miuix 图标集（`miuix-icons` 的 HyperOS 语义图标）。两处**近似而非同名**，
 * 是"换语义位"而不是"凑一个图标"：
 * - 「网络实测」用 `Refresh`（HyperOS 里刷新/重试的语义位；图标集中没有 Cloud）；
 * - 「构建变体」用 `Contacts`（"我的/个人"语义位，与底栏第三个 tab 一致）。
 */
@Composable
fun AbilityScreen(
    onSelectTab: (TopLevelDestination) -> Unit,
    onOpenGallery: () -> Unit,
    onOpenNetworkProbe: () -> Unit,
    onOpenStateDemo: () -> Unit,
) {
    TopLevelScaffold(
        destination = TopLevelDestination.ABILITY,
        onSelectTab = onSelectTab,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            GroupLabel(text = stringResource(R.string.ability_group_samples))

            AbilityRow(
                icon = MiuixIcons.ListView,
                title = stringResource(R.string.ability_gallery),
                description = stringResource(R.string.ability_gallery_desc),
                onClick = onOpenGallery,
            )
            AbilityRow(
                icon = MiuixIcons.Refresh,
                title = stringResource(R.string.ability_probe),
                description = stringResource(R.string.ability_probe_desc),
                onClick = onOpenNetworkProbe,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            GroupLabel(text = stringResource(R.string.ability_group_where))

            AbilityRow(
                icon = MiuixIcons.Home,
                title = stringResource(R.string.ability_network),
                description = stringResource(R.string.ability_network_desc),
                tag = stringResource(R.string.ability_tag_home),
            )
            AbilityRow(
                icon = MiuixIcons.Back,
                title = stringResource(R.string.ability_nav),
                description = stringResource(R.string.ability_nav_desc),
                tag = stringResource(R.string.ability_tag_home),
            )
            AbilityRow(
                icon = MiuixIcons.Info,
                title = stringResource(R.string.ability_state),
                description = stringResource(R.string.ability_state_desc),
                onClick = onOpenStateDemo,
            )
            AbilityRow(
                icon = MiuixIcons.Contacts,
                title = stringResource(R.string.ability_flavor),
                description = stringResource(R.string.ability_flavor_desc),
                tag = stringResource(R.string.ability_tag_mine),
            )
            AbilityRow(
                icon = MiuixIcons.Report,
                title = stringResource(R.string.ability_p1),
                description = stringResource(R.string.ability_p1_desc),
                tag = stringResource(R.string.ability_tag_p1),
            )
        }
    }
}

/** 分组标题：用 miuix 的 `SmallTitle`（HyperOS 的小节标题形态），不自造文字样式。 */
@Composable
private fun GroupLabel(text: String) {
    SmallTitle(text = text)
}

/**
 * 能力入口行：`onClick != null` 时后置箭头（可点），否则后置一个灰标签（**不可点，也不点透**）。
 *
 * 未实现项摆一个明确的 `P1` 标签，比"点了没反应"或者"干脆不写"都更有用 ——
 * 使用者一眼知道这不是他配置错了。
 *
 * 形态从 M3 的 `ListItem`（槽位是 `headlineContent` / `supportingContent` / `leadingContent`）
 * 换成 miuix 的 **`BasicComponent`**（`title` / `summary` / `startAction` / `endActions`，
 * 且 `title`/`summary` 收 `String` 而不是 `@Composable`），**可点击性内建**（`onClick` 参数）
 * ⇒ 不再需要外面套 `Modifier.clickable`。
 */
@Composable
private fun AbilityRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null,
    tag: String? = null,
) {
    BasicComponent(
        title = title,
        summary = description,
        startAction = {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        },
        endActions = {
            when {
                onClick != null ->
                    Icon(
                        imageVector = MiuixIcons.ChevronForward,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )

                // 归位提示用纯文本（**不是 chip / 按钮**）：它本来就不该被点，用可点组件是假语义
                tag != null ->
                    Text(
                        text = tag,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )

                else -> Unit
            }
        },
        onClick = onClick,
    )
}

package com.example.scaffolddemo.ui.ability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scaffolddemo.R
import com.example.scaffolddemo.ui.shell.SubPageScaffold
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.BadgedBox
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Alarm
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 组件画廊：**miuix 组件按族摆一遍，直接抄**。
 *
 * 为什么要这一屏：AI 生成 Compose 界面时最常见的偏差不是"写不出来"，而是**拿错组件** ——
 * 该用 `TabRow` 的档位切换用 `Button`、该用 `BasicComponent` 的列表项手搓 `Row`、
 * 卡片只认识 `Card` 却不知道变体靠容器色。摆一屏看得到，比在文档里翻半天快。
 *
 * ⚠️ **这一屏的清单与 M3 版不同，是重列的**（miuix 的组件粒度与 M3 不是一一对应）：
 * - miuix **没有 chip 组件族** ⇒ 原来的 FilterChip / AssistChip / SuggestionChip 三个演示
 *   合并成一档 `TabRow`（档位/单选，骨架里作者筛选与状态切换用的都是它）；
 * - miuix 的 `Card` / `Button` **各只有一个函数**，M3 的"三变体"在这里退化成
 *   "一个组件 + 换容器色 / 换 colors" ⇒ 摆的是**容器色三档**与**按钮四种配色**，不是三个组件；
 * - `ListItem` → `BasicComponent`，`OutlinedTextField` → `TextField`（无 supportingText 槽位）。
 *
 * 只摆**骨架真正用到或明确推荐**的组件，不做全量罗列（那没有参考价值）。
 */
@Composable
fun GalleryScreen(onBack: () -> Unit) {
    SubPageScaffold(
        title = stringResource(R.string.title_gallery),
        onBack = onBack,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TabRowSection()
            ButtonsSection()
            FieldsSection()
            BasicComponentSection()
            CardsSection()
            ProgressSection()
            BadgeSection()
        }
    }
}

@Composable
private fun TabRowSection() =
    Section(stringResource(R.string.gallery_group_tabs)) {
        var selected by rememberSaveable { mutableStateOf(0) }
        TabRow(
            tabs =
                listOf(
                    stringResource(R.string.gallery_tab_all),
                    stringResource(R.string.gallery_tab_follow),
                    stringResource(R.string.gallery_tab_recommend),
                ),
            selectedTabIndex = selected,
            onTabSelected = { selected = it },
        )
        Text(
            text = stringResource(R.string.gallery_tabs_desc),
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }

@Composable
private fun ButtonsSection() =
    Section(stringResource(R.string.gallery_group_button)) {
        // 一屏里主按钮只该有一个：primary 最高，默认（secondaryVariant）次之，TextButton 最低
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColorsPrimary(),
            ) {
                Text(text = stringResource(R.string.gallery_btn_save))
            }
            Button(onClick = {}) {
                Text(text = stringResource(R.string.gallery_btn_cancel))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                text = stringResource(R.string.gallery_btn_view_all),
                onClick = {},
            )
            // 禁用态：颜色由 ButtonColors 的 disabled* 一组给出，不用自己压 alpha
            Button(
                onClick = {},
                enabled = false,
                colors = ButtonDefaults.buttonColorsPrimary(),
            ) {
                Text(text = stringResource(R.string.gallery_btn_save))
            }
        }
    }

@Composable
private fun FieldsSection() =
    Section(stringResource(R.string.gallery_group_field)) {
        var empty by rememberSaveable { mutableStateOf("") }
        var filled by rememberSaveable { mutableStateOf("已经填好的内容") }
        // 空 + useLabelAsPlaceholder：label 当占位提示用（聚焦后自动收起，不挡输入）
        TextField(
            value = empty,
            onValueChange = { empty = it },
            label = stringResource(R.string.gallery_field_hint),
            useLabelAsPlaceholder = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        // 已填：label 作为浮动标签贴在框内顶部，与内容同时可见
        TextField(
            value = filled,
            onValueChange = { filled = it },
            label = stringResource(R.string.gallery_field_filled),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        // miuix 的 TextField **没有 supportingText 槽位**：需要辅助说明/错误提示时，
        // 在下方自己摆一条 Text（不要为它自造组件）
        Text(
            text = stringResource(R.string.gallery_field_support),
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }

@Composable
private fun BasicComponentSection() =
    Section(stringResource(R.string.gallery_group_list)) {
        // 列表项一律用它：内建三段布局、行高、语义与按压反馈，别手搓 Row
        BasicComponent(
            title = stringResource(R.string.gallery_list_title),
            summary = stringResource(R.string.gallery_list_desc),
            startAction = {
                Icon(imageVector = MiuixIcons.Home, contentDescription = null, modifier = Modifier.size(24.dp))
            },
            endActions = {
                Icon(
                    imageVector = MiuixIcons.ChevronForward,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            onClick = {},
        )
    }

@Composable
private fun CardsSection() =
    Section(stringResource(R.string.gallery_group_card)) {
        // 只有一个 Card 函数：层次靠**容器色**表达（这里是 surfaceContainer 的三档）
        Card(modifier = Modifier.fillMaxWidth()) {
            CardBody(
                title = stringResource(R.string.gallery_card_default),
                desc = stringResource(R.string.gallery_card_default_desc),
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainerHigh),
        ) {
            CardBody(
                title = stringResource(R.string.gallery_card_high),
                desc = stringResource(R.string.gallery_card_high_desc),
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainerHighest),
        ) {
            CardBody(
                title = stringResource(R.string.gallery_card_highest),
                desc = stringResource(R.string.gallery_card_highest_desc),
            )
        }
    }

@Composable
private fun CardBody(
    title: String,
    desc: String,
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MiuixTheme.textStyles.headline2)
        Text(
            text = desc,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun ProgressSection() =
    Section(stringResource(R.string.gallery_group_progress)) {
        // 不定长（不知道进度）用这两个；知道百分比才传 progress，不要假装有进度
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
        }
    }

@Composable
private fun BadgeSection() =
    Section(stringResource(R.string.gallery_group_badge)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BadgedBox(
                badge = { Badge { Text(text = BADGE_COUNT) } },
            ) {
                Icon(
                    imageVector = MiuixIcons.Alarm,
                    contentDescription = stringResource(R.string.gallery_badge_desc),
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = stringResource(R.string.gallery_badge_desc),
                style = MiuixTheme.textStyles.body2,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }

/** 分组：小节标题 + 内容竖排。分组存在的意义是"这一坨是同一族组件"。 */
@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SmallTitle(text = title, insideMargin = PaddingValues(0.dp))
        content()
    }
}

private const val BADGE_COUNT = "3"

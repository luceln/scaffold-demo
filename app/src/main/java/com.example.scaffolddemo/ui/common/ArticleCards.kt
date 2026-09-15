package com.example.scaffolddemo.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 列表条目的 UI 模型：首页列表与搜索结果**共用同一个形状**，所以两处渲染同一张卡片。
 */
data class ArticleUi(
    val id: Long,
    val title: String,
    val body: String,
    val authorName: String,
    val authorId: Long,
    val date: String,
) {
    /** 卡片第二行：作者名 + 日期（缺哪个跳哪个，不产出「 · 」这种空拼接）。 */
    val metaLine: String
        get() = listOf(authorName, date).filter { it.isNotBlank() }.joinToString(META_SEPARATOR)

    private companion object {
        const val META_SEPARATOR = " · "
    }
}

/**
 * 列表行 = miuix `Card(onClick = …)`。
 *
 * 用 `Card` 而不是自建 `Modifier.clickable` 的 `Column`：直接拿到 miuix 的按压反馈
 * （`pressFeedbackType`，HyperOS 的缩放/高亮）与语义角色，不重复造基础组件。
 *
 * miuix 的 `Card` 只有**一个**函数（M3 的 `Card`/`ElevatedCard`/`OutlinedCard` 三变体
 * 在这里不存在），"填充感"的区别靠 `CardDefaults.defaultColors(color = …)` 换容器色 ——
 * 组件画廊里摆了三种容器色，不是三个组件。
 *
 * 标题与正文**各限 2 行 + 省略号**：不截断的话条目高度参差，列表看起来是散的。
 */
@Composable
fun ArticleCard(
    article: ArticleUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBody: Boolean = true,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = article.title,
                    style = MiuixTheme.textStyles.headline2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (article.metaLine.isNotBlank()) {
                    Text(
                        text = article.metaLine,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (showBody && article.body.isNotBlank()) {
                    Text(
                        text = article.body,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            // 尾随箭头：静态截图里也能看出"可点"（miuix 图标由库负责 RTL 镜像）
            Icon(
                imageVector = MiuixIcons.ChevronForward,
                contentDescription = null,
            )
        }
    }
}

package com.example.scaffolddemo.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.scaffolddemo.R
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/*
 * 三个"安静态"的共享实现：**骨架屏 / 空态 / 错误态**。
 *
 * 骨架屏手写约 50 行，不引依赖 —— miuix 没有 Skeleton 组件，属"没有合适依赖"的自建范围
 * （宪法允许：自建只出现在"没有合适依赖"或"业务组合层"，基础原子组件仍必须来自 UI 库）。
 * 关键约束是**与真实内容同尺寸同位置**，落数据时页面不跳动。
 *
 * 用普通块注释而不是 KDoc：文件级说明后面又跟了一个 KDoc，ktlint 会把前者判成
 * "dangling toplevel KDoc"（standard:kdoc）。块注释不参与 KDoc 归属规则。
 */

/** 骨架块（按父容器宽度取比例）：用一个带脉冲透明度的圆角矩形表达"内容还没到"。 */
@Composable
private fun SkeletonBlock(
    widthFraction: Float,
    height: Dp,
    alpha: Float,
    corner: Dp = 4.dp,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth(widthFraction)
                .height(height)
                .clip(RoundedCornerShape(corner))
                .background(MiuixTheme.colorScheme.onSurface.copy(alpha = alpha)),
    )
}

/** 骨架块（固定尺寸）：给宽度由库决定、算不出比例的坑位用（如 tab 行）。 */
@Composable
private fun SkeletonBlock(
    width: Dp,
    height: Dp,
    alpha: Float,
    corner: Dp = 4.dp,
) {
    Box(
        modifier =
            Modifier
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(corner))
                .background(MiuixTheme.colorScheme.onSurface.copy(alpha = alpha)),
    )
}

/** 脉冲透明度：呼吸范围 0.06–0.16，周期 ~900ms。 */
@Composable
private fun pulseAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 900), repeatMode = RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    return alpha
}

/**
 * 单张骨架卡片：**与 [ArticleCard] 逐行对齐** —— 同一份 `padding(16.dp)`、
 * 同样的 `spacedBy(4.dp)`、标题 1 行 / 作者行 / 正文 2 行、右侧还留出尾随箭头的坑位。
 *
 * 行高按真实文字的行盒取（标题 24dp 一行、作者行 14dp、正文 16dp ×2）——
 * 2026-09-14 真机走查实测：原实现只有 3 条 8dp 间距的矮块（卡片高 228px），
 * 真卡片高 310px（一行标题）/ 376px（两行标题），落数据时整列会往下跳一截。
 * 改后卡片高 ≈319px，与常见的 310px 卡片差 ~3%（重建后按真机截图量过）。
 */
@Composable
private fun SkeletonArticleCard() {
    val alpha = pulseAlpha()
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SkeletonBlock(widthFraction = 0.88f, height = 24.dp, alpha = alpha)
                SkeletonBlock(widthFraction = 0.44f, height = 14.dp, alpha = alpha)
                SkeletonBlock(widthFraction = 0.96f, height = 16.dp, alpha = alpha)
                SkeletonBlock(widthFraction = 0.72f, height = 16.dp, alpha = alpha)
            }
            // 尾随箭头占位：不留这个坑的话，骨架卡比真卡窄一截（少 24dp + 行高差）
            SkeletonBlock(width = 24.dp, height = 24.dp, alpha = alpha)
        }
    }
}

/**
 * 首页加载态：档位行也占位（否则列表会整体上跳），下面 3 张骨架卡。
 *
 * 档位行占位照 miuix `TabRow` 的结构常量取：行高 42dp、圆角 12dp、项间距 9dp、
 * 单块宽取下限 76dp（`TabRowDefaults.TabRowMinWidth`）。**真实宽度由文字实测决定、
 * 落在 76–98dp 之间**，所以这里是最小值近似 —— 差值在真机走查里量过再回写。
 *
 * 在 `dev` 下 mock 拦截器会补一段延迟（`MockInterceptor.DEFAULT_LATENCY_MS`），
 * 所以这块在真机上**真的看得见**，不是"一闪而过、截图都截不到"。
 */
@Composable
fun SkeletonArticleList(
    modifier: Modifier = Modifier,
    itemCount: Int = 3,
) {
    val alpha = pulseAlpha()
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            repeat(TAB_ROW_PLACEHOLDER_COUNT) {
                SkeletonBlock(width = 76.dp, height = 42.dp, alpha = alpha, corner = 12.dp)
            }
        }
        repeat(itemCount) { SkeletonArticleCard() }
    }
}

/** 档位行占位块个数：全部 + 4 位作者。 */
private const val TAB_ROW_PLACEHOLDER_COUNT = 5

/** 详情加载态：两行标题 + 作者行 + 正文 6 行，与真实排版对齐。 */
@Composable
fun SkeletonDetail(modifier: Modifier = Modifier) {
    val alpha = pulseAlpha()
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SkeletonBlock(widthFraction = 0.92f, height = 20.dp, alpha = alpha)
        SkeletonBlock(widthFraction = 0.58f, height = 20.dp, alpha = alpha)
        Row(
            modifier = Modifier.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MiuixTheme.colorScheme.onSurface.copy(alpha = alpha)),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonBlock(widthFraction = 0.38f, height = 12.dp, alpha = alpha)
                SkeletonBlock(widthFraction = 0.52f, height = 10.dp, alpha = alpha)
            }
        }
        repeat(6) { index ->
            SkeletonBlock(widthFraction = if (index % 3 == 2) 0.46f else 0.98f, height = 12.dp, alpha = alpha)
        }
    }
}

/**
 * 空态：安静留白 + 一句**说明原因**的文案 + 可选出口按钮。**不放假数据卡片。**
 *
 * 文案带上筛选维度（「该作者下暂无文章」）比笼统的「暂无数据」更能说明发生了什么。
 */
@Composable
fun EmptyView(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            // miuix 只有 Button / TextButton 两个函数，变体靠 colors —— 次级按钮 = 默认色（secondaryVariant）
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(),
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text(text = actionLabel)
            }
        }
    }
}

/**
 * 错误态：文案与错误码都由 [ErrorKind] 给出（UI 层不解释异常）。
 *
 * `canRetry = false`（如 404）时不给重试按钮 —— 只给"返回"这类真出口。
 */
@Composable
fun ErrorView(
    kind: ErrorKind,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    fallbackActionLabel: String? = null,
    onFallbackAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(kind.messageRes),
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.error,
                )
                Text(
                    text =
                        stringResource(
                            R.string.error_meta_format,
                            stringResource(kind.codeRes),
                            stringResource(
                                if (kind.canRetry) R.string.error_meta_retryable else R.string.error_meta_no_retry,
                            ),
                        ),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (kind.canRetry && onRetry != null) {
                // 主按钮 = 品牌色填充
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(text = stringResource(R.string.action_retry))
                }
            }
            if (fallbackActionLabel != null && onFallbackAction != null) {
                Button(
                    onClick = onFallbackAction,
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(text = fallbackActionLabel)
                }
            }
        }
    }
}

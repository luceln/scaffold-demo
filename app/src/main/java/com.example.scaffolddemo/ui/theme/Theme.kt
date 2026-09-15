package com.example.scaffolddemo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

// 只覆盖品牌用到的角色，其余 40 多个走 miuix 默认（签名共 53 个 Color 参数）。
private val LightColors =
    lightColorScheme(
        primary = Blue600,
        onPrimary = White,
        primaryContainer = Gray100,
        onPrimaryContainer = Gray900,
    )

private val DarkColors =
    darkColorScheme(
        primary = BlueDark,
        onPrimary = White,
        primaryContainer = Gray900,
        onPrimaryContainer = Gray100,
    )

/**
 * miuix 主题：品牌定制的唯一入口。
 * - 改色：`Color.kt` 的 token + 这里的两套 ColorScheme
 * - 改字：`Type.kt` 的 `AppTextStyles`（miuix 的 14 个字阶）
 *
 * **刻意不跟随系统动态取色**。miuix 具备这个能力（`ThemeController(keyColor = …)` +
 * `DynamicColors` + `ColorSchemeMode.MonetSystem`），但作为模板默认值有两个问题：
 * 同一份代码在不同手机上呈现不同颜色，且品牌色会失效 —— 模板要的是"换个 App 先看到自己的颜色"。
 * 确有需要时再显式包一层 `ThemeController`。
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MiuixTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        textStyles = AppTextStyles,
        content = content,
    )
}

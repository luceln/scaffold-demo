package com.example.scaffolddemo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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
 * Material 3 主题：品牌定制的唯一入口。
 * - 改色：Color.kt 的 token + 这里的两套 ColorScheme
 * - 改字：Type.kt
 * - Android 12+ 默认跟随系统动态取色（Material You）；不要动态色就删掉
 *   dynamicColorScheme 分支，全局固定品牌色。
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> {
                DarkColors
            }

            else -> {
                LightColors
            }
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}

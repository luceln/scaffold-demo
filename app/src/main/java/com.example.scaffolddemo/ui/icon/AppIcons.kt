package com.example.scaffolddemo.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 项目自有图标。
 *
 * **什么时候该往这里加**：miuix 的图标库（`miuix-icons`）提供了一百多枚 HyperOS 语义图标，
 * 但它并不与 Material 图标一一对应 —— 个别语义位（例如「点赞」）在里面没有对应项。
 * 惯例做法不是为这几枚图标再引入一整套图标依赖，而是把缺失那几枚的矢量几何**直接定义在项目里**。
 *
 * **新增一枚的步骤**：
 * 1. 取矢量时来源要许可明确（官方设计体系，或自家设计稿），并确认是 24dp 网格；
 * 2. 照 [ThumbUp] 的形态写 `ImageVector.Builder(...).apply { path { … } }.build()`；
 * 3. 在对象里追加一个 `val`，几何写进同名的私有构造函数。
 *
 * **用法与 miuix 原生图标完全一致** —— `Icon` 组件接受任意 [ImageVector]：
 * ```
 * Icon(imageVector = AppIcons.ThumbUp, contentDescription = null)
 * ```
 */
object AppIcons {
    /**
     * 点赞。
     *
     * 几何取自 Material Symbols 的 `thumb_up`（Apache License 2.0，24dp 网格）。
     * 之所以自绘而不换用 miuix 里的近似图标：HyperOS 图标集内没有语义对得上的点赞形态，
     * 而点赞在详情页是独立动作，不该被并入收藏。
     */
    val ThumbUp: ImageVector by lazy { buildThumbUp() }
}

private fun buildThumbUp(): ImageVector =
    ImageVector
        .Builder(
            name = "ThumbUp",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(1f, 21f)
                horizontalLineToRelative(4f)
                lineTo(5f, 9f)
                lineTo(1f, 9f)
                verticalLineToRelative(12f)
                close()
                moveTo(23f, 10f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineToRelative(-6.31f)
                lineToRelative(0.95f, -4.57f)
                lineToRelative(0.03f, -0.32f)
                curveToRelative(0f, -0.41f, -0.17f, -0.79f, -0.44f, -1.06f)
                lineTo(14.17f, 1f)
                lineTo(7.59f, 7.59f)
                curveTo(7.22f, 7.95f, 7f, 8.45f, 7f, 9f)
                verticalLineToRelative(10f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(9f)
                curveToRelative(0.83f, 0f, 1.54f, -0.5f, 1.84f, -1.22f)
                lineToRelative(3.02f, -7.05f)
                curveToRelative(0.09f, -0.23f, 0.14f, -0.47f, 0.14f, -0.73f)
                verticalLineToRelative(-2f)
                close()
            }
        }.build()

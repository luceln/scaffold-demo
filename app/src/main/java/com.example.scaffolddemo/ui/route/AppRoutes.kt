package com.example.scaffolddemo.ui.route

import kotlinx.serialization.Serializable

/*
 * 类型安全路由：`@Serializable` 对象/数据类直接当目的地用，`composable<HomeRoute> { }`。
 *
 * 好处是**编译期**就能查错（拼错路由名不可能通过编译）、传参不再手拼字符串。
 * 依赖 `kotlin-serialization` 插件 —— 骨架本来就装了（纵切拿它做 DTO），零新增插件。
 *
 * 命名约定：`XxxRoute` = 目的地；参数只放**真正需要**的东西 ——
 * 详情只传 `id` 不传整个对象，避免"进程重建后参数过期"这类假方便。
 *
 * 为什么是普通块注释而不是 KDoc：下面紧跟着 `//` 分节注释，而 ktlint 的
 * standard:no-consecutive-comments 不允许"EOL 注释紧跟在 KDoc 之后"（反序才允许）。
 *
 * ⚠️ ktlint `standard:filename` 只约束"文件内只有一个顶层类"的情形，本文件是多声明文件，
 * 不受文件名规则限制（README 坑 #13 的边界）。
 */

// ---- 顶层目的地（底部 tab，各有独立回退栈）----
@Serializable
data object HomeRoute

@Serializable
data object AbilityRoute

@Serializable
data object MineRoute

// ---- 下级页（进入时隐藏底栏）----

/** 组件画廊：能力 tab 的下级页。 */
@Serializable
data object GalleryRoute

/** 网络实测（真出网对照组）：能力 tab 的下级页。 */
@Serializable
data object NetworkProbeRoute

/** 状态管理演示：能力 tab 的下级页，同时是 mock 故障注入的驱动台。 */
@Serializable
data object StateDemoRoute

/** 全屏搜索：首页顶栏 action 进入，**不占 tab**。 */
@Serializable
data object SearchRoute

/** 详情：只传 id。 */
@Serializable
data class DetailRoute(
    val id: Long,
)

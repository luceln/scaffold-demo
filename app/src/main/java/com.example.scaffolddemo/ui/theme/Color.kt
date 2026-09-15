package com.example.scaffolddemo.ui.theme

import androidx.compose.ui.graphics.Color

// 品牌色 token：换 App 的第一件事就是把这几个值换成你的品牌色。
// 亮暗两套各一对（primary / on-primary）+ 一对容器色。
//
// miuix 的 ColorScheme 有 53 个颜色角色（primaryContainer / surface / dividerLine …），
// 用到哪个再回这里补一个 token —— 没被覆盖的角色由 miuix 的默认方案兜底，
// 不会被置空。想一次看全 53 个角色名，见 Theme.kt 里 lightColorScheme 的调用点。
val Blue600 = Color(0xFF1A73E8)
val BlueDark = Color(0xFF0B479C)
val White = Color(0xFFFFFFFF)
val Gray900 = Color(0xFF202124)
val Gray100 = Color(0xFFF1F3F4)

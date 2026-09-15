package com.example.scaffolddemo.ui.theme

import top.yukonga.miuix.kmp.theme.TextStyles
import top.yukonga.miuix.kmp.theme.defaultTextStyles

// 字体 token：接品牌字体时在这里换 fontFamily；要改字号/字重也统一在这里改。
//
// ⚠️ **不要按 Material 的名字位置去猜 miuix 的字阶** —— 两套体系的"大小顺序"是反的：
// Material 的 `headline*` 是大标题、`title*` 是次级标题；miuix 正好相反，
// `Title1..4` 才是**展示级**大标题，`Headline1/2` 是**正文级**强调。
// miuix 14 个字阶的出厂值（0.9.3 实测，别再凭印象）：
//
//   Title1 32sp · Title2 24sp · Title3 20sp · Title4 18sp      ← 展示级（页面大标题）
//   Headline1 17sp · Headline2 16sp                            ← 正文级（列表主文案、卡片标题）
//   Main 17sp · Paragraph 17sp · Body1 16sp · Body2 14sp       ← 正文（Body2 更密）
//   Subtitle 14sp Bold · Button 17sp                            ← 小节标题 / 按钮
//   Footnote1 13sp · Footnote2 11sp                             ← 次要说明、元信息
//
// 库内约定可当参照：`BasicComponent` 用 `headline1` + `body2`，`SmallTitle` 用 `subtitle`。
//
// 本模板**默认不覆盖任何字阶** —— 出厂值已经是 HyperOS 的排版节奏，动它之前先想清楚
// 是"品牌需要"还是"我觉得"。
val AppTextStyles: TextStyles = defaultTextStyles()

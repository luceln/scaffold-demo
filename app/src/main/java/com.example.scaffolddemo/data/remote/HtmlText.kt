package com.example.scaffolddemo.data.remote

/*
 * 第三方网页接口返回的标题是**网页正文**，里面带 HTML 实体。
 * 真机走查实测（wanandroid）：标题会出现「Rust&mdash;,&mdash;黄金时代结束了吗？」——
 * 不还原实体，用户看到的就是 `&mdash;` 这种源码。
 *
 * 刻意不引 Jsoup 那类 HTML 解析库（本模板的依赖纪律：几十行能解决的就不引依赖），
 * 只做「实体 → 字符」的定点还原：
 * - 命名实体查表；
 * - 数字实体（`&#8212;` / `&#x2014;`）按码点还原，支持增补平面（emoji）；
 * - **未命中的实体原样保留** —— 宁可留着 `&copy` 也不猜，避免把内容改错。
 */

private val ENTITY_PATTERN =
    Regex(
        pattern = "&(#x?[0-9a-fA-F]{1,6}|[a-zA-Z][a-zA-Z0-9]{1,31});",
        // 实体大小写不敏感（`&Mdash;` / `&#X2014;` 都算）
        option = RegexOption.IGNORE_CASE,
    )

private val NAMED_ENTITIES =
    mapOf(
        "amp" to "&",
        "lt" to "<",
        "gt" to ">",
        "quot" to "\"",
        "apos" to "'",
        "nbsp" to "\u00A0",
        "mdash" to "\u2014",
        "ndash" to "\u2013",
        "hellip" to "\u2026",
        "middot" to "\u00B7",
        "bull" to "\u2022",
        "lsquo" to "\u2018",
        "rsquo" to "\u2019",
        "ldquo" to "\u201C",
        "rdquo" to "\u201D",
        "laquo" to "\u00AB",
        "raquo" to "\u00BB",
        "copy" to "\u00A9",
        "reg" to "\u00AE",
        "trade" to "\u2122",
        "times" to "\u00D7",
        "deg" to "\u00B0",
    )

/** 把 HTML 实体还原成字符；**未识别的实体保持原样**（不猜、不丢内容）。 */
fun String.decodeHtmlEntities(): String =
    ENTITY_PATTERN.replace(this) { match ->
        decodeEntityBody(match.groupValues[1]) ?: match.value
    }

private fun decodeEntityBody(body: String): String? =
    when {
        body.startsWith("#x", ignoreCase = true) -> codePointToString(body.drop(2).toIntOrNull(radix = 16))
        body.startsWith("#") -> codePointToString(body.drop(1).toIntOrNull())
        else -> NAMED_ENTITIES[body.lowercase()]
    }

private fun codePointToString(codePoint: Int?): String? {
    if (codePoint == null || !Character.isValidCodePoint(codePoint)) return null
    return String(Character.toChars(codePoint))
}

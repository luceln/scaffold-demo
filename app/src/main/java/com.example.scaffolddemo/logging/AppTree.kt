package com.example.scaffolddemo.logging

import android.util.Log
import timber.log.Timber

/**
 * 统一日志门面（九件套 #2）：Timber Tree 的骨架实现。
 *
 * 约定：
 * - 输出走 LogCat，格式 `[TAG] message`（设备调试可读；接日志收集系统时
 *   在这里换成 JSON 结构化输出，改动只落在这一个文件）。
 * - 敏感字段脱敏：形如 `password=xxx` / `token: xxx` 的键值对整体遮蔽 ——
 *   宁可多遮，不可漏遮。
 * - 级别约定与宪法一致：ERROR 人工介入 / WARN 降级重试 / INFO 关键节点 /
 *   DEBUG 调试信息；循环体内不打日志。
 */
class AppTree : Timber.DebugTree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        val safeMessage = sanitize(message)
        val safeThrowable = t?.let { "${it.javaClass.simpleName}: ${sanitize(it.message ?: "")}" }
        val full = listOfNotNull(safeMessage, safeThrowable).joinToString(" | ")
        Log.println(priority, tag ?: "APP", full)
    }

    private fun sanitize(message: String): String =
        SENSITIVE_PAIR.replace(message) { match ->
            "${match.groupValues[KEY_GROUP]}=<MASKED>"
        }

    private companion object {
        // 形如 "password=xxx" / "Token: xxx" 的键值对；值含空格视为提前结束
        val SENSITIVE_PAIR =
            Regex("""(?i)(password|token|secret|authorization)\s*[=:]\s*[^,;&\s]+""")
        const val KEY_GROUP = 1
    }
}

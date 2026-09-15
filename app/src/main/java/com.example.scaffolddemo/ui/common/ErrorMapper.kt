package com.example.scaffolddemo.ui.common

import androidx.annotation.StringRes
import com.example.scaffolddemo.R
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 用户可读的错误分类。
 *
 * 界面**只认这个枚举**，不写 `when (e)` —— 异常到文案的映射全仓只有 [ErrorMapper] 一处。
 * `canRetry = false` 的（如 404）界面上不给「重试」按钮：重试也没用，给出口比给按钮诚实。
 *
 * 文案与错误码都走字符串资源（可翻译、可统一改）；错误码进结构化日志，便于线上对账。
 */
enum class ErrorKind(
    @StringRes val messageRes: Int,
    @StringRes val codeRes: Int,
    val canRetry: Boolean,
) {
    TIMEOUT(R.string.error_timeout, R.string.error_code_timeout, canRetry = true),
    NO_NETWORK(R.string.error_no_network, R.string.error_code_no_network, canRetry = true),
    NOT_FOUND(R.string.error_not_found, R.string.error_code_not_found, canRetry = false),
    SERVER(R.string.error_server, R.string.error_code_server, canRetry = true),
    PARSE(R.string.error_parse, R.string.error_code_parse, canRetry = false),
    UNKNOWN(R.string.error_unknown, R.string.error_code_unknown, canRetry = true),
}

/**
 * 统一错误映射层：异常 → [ErrorKind]。
 *
 * 纯函数、无 Android 依赖（只用到 `R` 的整型常量）⇒ 可以在纯 JVM 单测里穷举各分支
 * （见 `ErrorMapperTest`）。**这是模板的样板之一：新页面照抄，别再各写一份 `when(e)`。**
 */
object ErrorMapper {
    fun map(throwable: Throwable): ErrorKind =
        when (throwable) {
            is SocketTimeoutException -> ErrorKind.TIMEOUT
            is UnknownHostException -> ErrorKind.NO_NETWORK
            is HttpException ->
                when (throwable.code()) {
                    404 -> ErrorKind.NOT_FOUND
                    in 500..599 -> ErrorKind.SERVER
                    else -> ErrorKind.UNKNOWN
                }
            // 反序列化失败（字段缺失 / 类型不符）单列一类：这是"数据问题"，不该提示"检查网络"
            is SerializationException -> ErrorKind.PARSE
            is IOException -> ErrorKind.NO_NETWORK // 其余 IO 类（连接被拒 / 流中断）都归"网络不通"
            else -> ErrorKind.UNKNOWN
        }
}

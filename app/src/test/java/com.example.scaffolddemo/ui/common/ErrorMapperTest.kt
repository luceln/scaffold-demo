package com.example.scaffolddemo.ui.common

import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
 * 统一错误映射的穷举测试。
 *
 * 这一层是纯函数（只用到 R 的整型常量、无 Android 依赖），所以每条分支都能在这里钉死。
 * 价值不在"覆盖率"，而在**顺序** —— 比如 SocketTimeoutException 本身是 IOException 的子类，
 * 分支写反了会把它错判成"网络不通"，提示用户去查网络，而真正的问题是超时。
 */
class ErrorMapperTest {
    private fun httpError(code: Int): HttpException = HttpException(Response.error<Unit>(code, "".toResponseBody()))

    @Test
    fun `超时 - 映射为 TIMEOUT 而不是被 IOException 分支抢走`() {
        val kind = ErrorMapper.map(SocketTimeoutException("read timed out"))

        assertEquals(ErrorKind.TIMEOUT, kind)
        assertTrue(kind.canRetry)
    }

    @Test
    fun `域名解析失败 - 映射为 NO_NETWORK`() {
        assertEquals(ErrorKind.NO_NETWORK, ErrorMapper.map(UnknownHostException("api.example.com")))
    }

    @Test
    fun `其它 IO 异常 - 也归到 NO_NETWORK（连接被拒 流中断）`() {
        assertEquals(ErrorKind.NO_NETWORK, ErrorMapper.map(IOException("connection reset by peer")))
    }

    @Test
    fun `404 - 映射为 NOT_FOUND 且不给重试按钮`() {
        val kind = ErrorMapper.map(httpError(404))

        assertEquals(ErrorKind.NOT_FOUND, kind)
        assertFalse(kind.canRetry, "404 重试没有意义，界面不该给这个按钮")
    }

    @Test
    fun `5xx - 映射为 SERVER 且可重试`() {
        assertEquals(ErrorKind.SERVER, ErrorMapper.map(httpError(500)))
        assertEquals(ErrorKind.SERVER, ErrorMapper.map(httpError(503)))
    }

    @Test
    fun `其它 4xx - 落到 UNKNOWN（不假装知道原因）`() {
        assertEquals(ErrorKind.UNKNOWN, ErrorMapper.map(httpError(400)))
        assertEquals(ErrorKind.UNKNOWN, ErrorMapper.map(httpError(418)))
    }

    @Test
    fun `反序列化失败 - 映射为 PARSE（这是数据问题，不该提示检查网络）`() {
        val kind = ErrorMapper.map(SerializationException("Unexpected JSON token"))

        assertEquals(ErrorKind.PARSE, kind)
        assertFalse(kind.canRetry)
    }

    @Test
    fun `未预期的异常 - 兜底 UNKNOWN，不让异常穿透到界面`() {
        assertEquals(ErrorKind.UNKNOWN, ErrorMapper.map(IllegalStateException("意料之外")))
    }
}

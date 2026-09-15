package com.example.scaffolddemo.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * HTML 实体的定点还原。
 *
 * 这组用例的由来：真机走查时「网络实测」的真网络档出现了
 * 「Rust&mdash;,&mdash;黄金时代结束了吗？」—— 实体没还原，用户看到的是源码。
 */
class HtmlTextTest {
    @Test
    fun `还原命名实体`() {
        assertEquals("Rust—,—黄金时代结束了吗？", "Rust&mdash;,&mdash;黄金时代结束了吗？".decodeHtmlEntities())
        assertEquals("a & b < c > d", "a &amp; b &lt; c &gt; d".decodeHtmlEntities())
        assertEquals("引号\"与'单引号'", "引号&quot;与&apos;单引号&apos;".decodeHtmlEntities())
    }

    @Test
    fun `还原数字实体（十进制与十六进制）`() {
        assertEquals("破折号—完毕", "破折号&#8212;完毕".decodeHtmlEntities())
        assertEquals("破折号—完毕", "破折号&#x2014;完毕".decodeHtmlEntities())
        assertEquals("emoji 😀 保留", "emoji &#x1F600; 保留".decodeHtmlEntities())
    }

    @Test
    fun `未识别的实体原样保留`() {
        assertEquals("&copy 与 &unknown; 不动", "&copy 与 &unknown; 不动".decodeHtmlEntities())
        // 码点非法（超出 Unicode 范围）也不该把内容改坏
        assertEquals("&#x110000;", "&#x110000;".decodeHtmlEntities())
    }

    @Test
    fun `没有实体时原样返回`() {
        val plain = "写过 4000 行 ViewModel 后，我开始这样拆 Compose 页面"
        assertEquals(plain, plain.decodeHtmlEntities())
        assertEquals("", "".decodeHtmlEntities())
    }

    @Test
    fun `一格里多个实体逐次还原`() {
        assertEquals(
            "“A—B”…C",
            "&ldquo;A&mdash;B&rdquo;&hellip;C".decodeHtmlEntities(),
        )
    }

    @Test
    fun `大小写混写的实体也能识别`() {
        assertEquals("A—B", "A&Mdash;B".decodeHtmlEntities())
        assertEquals("A—B", "A&#X2014;B".decodeHtmlEntities())
    }
}

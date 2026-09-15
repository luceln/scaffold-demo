package com.example.scaffolddemo.data.mock

/**
 * mock 数据的读取口子：把「从哪读 JSON」与「怎么用 JSON 应答」拆开。
 *
 * 拆开的好处是**拦截器逻辑可以在纯 JVM 单测里跑完** —— 测试塞一个内存实现即可，
 * 不需要 Android `Context`、不需要 Robolectric。真实实现见 [AssetMockData]（读 `assets/mock/`）。
 */
fun interface MockData {
    /**
     * 读一份 mock 数据。
     *
     * @param assetName 不带目录与扩展名的资源名（如 `posts` / `users`）。
     * @return JSON 文本；读不到返回 `null`（由调用方转成 `IOException` 汇进统一错误路径）。
     */
    fun read(assetName: String): String?
}

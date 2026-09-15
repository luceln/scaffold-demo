package com.example.scaffolddemo.data.mock

import android.content.Context

/**
 * [MockData] 的真实实现：读 `dev` 源集里的 `assets/mock/<name>.json`。
 *
 * 数据放在 `app/src/dev/assets/` ⇒ **不进 `prod` 包**（`prod` 里连文件都没有，
 * 拦截器也不挂，所以 mock 在发布包里既不存在也不会被加载）。
 */
class AssetMockData(
    private val context: Context,
) : MockData {
    override fun read(assetName: String): String? =
        runCatching {
            context.assets
                .open("mock/$assetName.json")
                .bufferedReader()
                .use { it.readText() }
        }.getOrNull()
}

package com.example.scaffolddemo

import com.example.scaffolddemo.logging.AppTree
import android.app.Application
import timber.log.Timber

/**
 * 应用入口：统一日志门面的装配点（九件套 #2）。
 *
 * 全工程禁止裸 android.util.Log / println —— 一律走 Timber，
 * 输出格式与脱敏规则集中在 AppTree。
 */
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // BuildConfig.DEBUG 只在 debug 变体种树：release 默认不打印任何日志，
        // 接崩溃上报/日志收集 SDK 后在这里换成对应 Tree（见 README「日志」）。
        if (BuildConfig.DEBUG) {
            Timber.plant(AppTree())
        }
    }
}

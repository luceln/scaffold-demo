package com.example.scaffolddemo

import android.app.Application
import com.example.scaffolddemo.di.appModule
import com.example.scaffolddemo.di.flavorModule
import com.example.scaffolddemo.di.networkModule
import com.example.scaffolddemo.logging.AppTree
import okhttp3.Interceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * 应用入口：**容器装配点**（一）与统一日志门面的装配点（九件套 #2）。
 *
 * 装配顺序有意为之：先 `startKoin`，再从容器取 `AppTree` 种树 —— 不再是
 * `Timber.plant(AppTree())` 那种"自己 new 一个"的写法。
 *
 * 启动日志带 `applicationId` / `BASE_URL` / `mock` 三项，**切 flavor 装两个包，
 * 这一行就是差异证据**（真机验收直接 `adb logcat | grep mock=`）。
 *
 * 全工程禁止裸 `android.util.Log` / `println` —— 一律走 Timber，输出格式与脱敏规则集中在 [AppTree]。
 */
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 持有 startKoin 返回的容器实例，下面一律走它取依赖。
        // 这里**没有**实现 KoinComponent：Koin 4.2.2 只为 KoinComponent 提供 get / inject，
        // 并没有 getOrNull（已核对 koin-core-jvm 4.2.2 的 KoinComponentKt，只有这两个方法）。
        // `Koin` 实例上 get / getOrNull 都齐，少绕一层，依赖也更显式。
        val koin =
            startKoin {
                androidContext(this@DemoApplication) // module 里的 androidContext() 靠它解析
                modules(appModule, flavorModule, networkModule())
            }.koin

        // debug 判定改用 BuildConfig.DEBUG：变体差异本来就要开 buildConfig，不必再绕 FLAG_DEBUGGABLE
        if (BuildConfig.DEBUG) {
            Timber.plant(koin.get<AppTree>())
        }

        Timber.i(
            "应用启动 applicationId=%s baseUrl=%s mock=%s",
            BuildConfig.APPLICATION_ID,
            BuildConfig.BASE_URL,
            koin.getOrNull<Interceptor>() != null,
        )
    }
}

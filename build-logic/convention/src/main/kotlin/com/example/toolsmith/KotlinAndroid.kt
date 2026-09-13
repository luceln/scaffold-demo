// 精简自 Now in Android 的 KotlinAndroid.kt（Apache 2.0）。
// 约定集中配置（§11.4）：compileSdk / bytecode target / desugaring 全工程一处。
package com.example.toolsmith

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

// version catalog 访问器：included build 里用显式扩展属性，不依赖生成类
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        compileSdk = 36

        defaultConfig.apply {
            // 由模板变量渲染：skeleton 里是 23，生成后是字面量
            minSdk = 23
        }

        compileOptions.apply {
            // 产物 bytecode 11 + coreLibraryDesugaring：java.time 等 API 在 minSdk 23 上可用
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
            isCoreLibraryDesugaringEnabled = true
        }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
            allWarningsAsErrors = false
            freeCompilerArgs.add(
                // 启用 kotlinx.coroutines 实验 API（如 Flow 的实验操作符）
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            )
        }
    }

    dependencies {
        "coreLibraryDesugaring"(libs.findLibrary("android-desugarJdkLibs").get())
    }
}

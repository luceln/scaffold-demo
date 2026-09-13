import com.android.build.api.dsl.ApplicationExtension
import com.example.toolsmith.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

// 精简自 Now in Android 的 AndroidApplicationConventionPlugin（Apache 2.0）。
// 注意：AGP 9 内置 Kotlin，这里**不** apply org.jetbrains.kotlin.android ——
// 应用它现在是构建错误，不是冗余。
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Gradle 9 移除了 PluginAware.apply(map/plugin) 重载，只认 pluginManager
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 36
            }
        }
    }
}

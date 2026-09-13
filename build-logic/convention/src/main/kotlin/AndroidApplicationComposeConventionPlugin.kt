import com.android.build.api.dsl.ApplicationExtension
import com.example.toolsmith.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

// 精简自 Now in Android 的 AndroidApplicationComposeConventionPlugin（Apache 2.0）。
// Compose 编译器插件（org.jetbrains.kotlin.plugin.compose）版本随 Kotlin 走，
// 其 classpath 已由 convention build.gradle.kts 的 compose-gradlePlugin 提供。
class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")

            extensions.configure<ApplicationExtension> {
                configureAndroidCompose(this)
            }
        }
    }
}

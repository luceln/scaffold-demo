// 仓库选择：CI（海外 runner）直连官方源；本地（国内）镜像前置、官方兜底。
// 为什么 CI 不走镜像：Gradle 对 404 会落到下一个仓库，但对 5xx（如阿里云偶发 502）
// 会直接禁用该仓库并整体失败 —— 「官方兜底」只防 404，防不住 502。
// 注意：pluginManagement 块先于脚本主体求值，顶层 val 在块内不可见，
// 所以这里必须逐块内联 System.getenv(...)，不能抽公共变量（CI run #9 实锤）。

pluginManagement {
    // build-logic 是 composite build：convention 插件的 classpath 从这里进主构建。
    // 这也是 AGP 9（内置 Kotlin）+ Kotlin 2.3.0 共存的正解 —— KGP 版本由
    // build-logic 的 classpath 决定（见 build-logic/convention/build.gradle.kts），
    // 而不是在模块里 apply org.jetbrains.kotlin.android（AGP 9 已移除该插件）。
    includeBuild("build-logic")
    repositories {
        if (System.getenv("GITHUB_ACTIONS") != "true") {
            maven("https://maven.aliyun.com/repository/gradle-plugin")
            maven("https://maven.aliyun.com/repository/google")
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        if (System.getenv("GITHUB_ACTIONS") != "true") {
            maven("https://maven.aliyun.com/repository/google")
            maven("https://maven.aliyun.com/repository/public")
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "scaffold-demo"

include(":app")

// AGP 9 要求 JDK 17+。在这里 fail-fast，错误信息直指原因，
// 而不是让用户在几分钟后读一个不相干的编译错误。
check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    本工程要求 JDK 17+，当前使用的是 JDK ${JavaVersion.current()}。
    请在 Android Studio 的 Gradle JDK 设置或 JAVA_HOME 中配置 JDK 17。
    参考：https://developer.android.com/build/jdks#jdk-config-in-studio
    """.trimIndent()
}

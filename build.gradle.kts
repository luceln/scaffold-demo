// 根构建脚本：只负责全仓插件版本声明与 spotless（ktlint 格式检查）。
//
// convention 插件（toolsmith.android.application / …compose）的 classpath
// 由 build-logic composite build 提供，这里不需要也不应该重复声明 AGP/KGP。
plugins {
    alias(libs.plugins.spotless)
}

spotless {
    // ktlint 规则的例外统一放 .editorconfig（NIA 同款做法），不散在这里。
    kotlinGradle {
        target("*.gradle.kts", "build-logic/**/*.gradle.kts", "app/*.gradle.kts")
        ktlint()
    }
    kotlin {
        target(
            "build-logic/convention/src/**/*.kt",
            "app/src/main/**/*.kt",
            "app/src/test/**/*.kt",
        )
        ktlint()
    }
}

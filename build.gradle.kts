// 根构建脚本：只负责全仓插件版本声明与 spotless（ktlint 格式检查）。
//
// convention 插件（toolsmith.android.application / …compose）的 classpath
// 由 build-logic composite build 提供，这里不需要也不应该重复声明 AGP/KGP。
plugins {
    alias(libs.plugins.spotless)
}

spotless {
    // ktlint 规则的例外统一放 .editorconfig（NIA 同款做法），不散在这里。
    //
    // ⚠️ 版本必须显式传：`ktlint()` 不带参数时用的是 **Spotless 自带**的 ktlint，
    // 而版本目录里那行 `ktlint = "1.4.0"` 就成了没人读的死条目 —— 升级 Spotless
    // 会悄悄换掉规则集，红了都不知道是谁改的。把声明接上，升级才是显式动作。
    val ktlintVersion = libs.versions.ktlint.get()
    kotlinGradle {
        target("*.gradle.kts", "build-logic/**/*.gradle.kts", "app/*.gradle.kts")
        ktlint(ktlintVersion)
    }
    kotlin {
        target(
            "build-logic/convention/src/**/*.kt",
            // 用 app/src/** 而不是 app/src/main + app/src/test：
            // dev/ prod 两个 flavor 源集（FlavorModule.kt）也是产品代码，
            // 漏在检查范围外就会出现"两份装配一份没被格式化"的漂移。
            "app/src/**/*.kt",
        )
        ktlint(ktlintVersion)
    }
}

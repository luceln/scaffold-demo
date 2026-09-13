// convention 插件的构建脚本。
//
// 关键机制：compileOnly 的 kotlin-gradle-plugin 就是 AGP 9 "内置 Kotlin" 的
// 版本来源 —— AGP 9 不再从模块 apply org.jetbrains.kotlin.android，而是用
// classpath 上存在的 KGP 编译 Kotlin。把 KGP 2.3.0 放进 convention 的
// classpath，主工程的 Kotlin 就升到 2.3.0（Now in Android 同款做法）。
plugins {
    `kotlin-dsl`
}

group = "com.example.toolsmith.buildlogic"

// convention 的目标 JDK 必须与主工程构建用 JDK 一致（17）；
// 这与产物 bytecode 版本（JVM 11 + desugaring，见 KotlinAndroid.kt）是两回事。
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        // id 用字面量（NIA 同款）：convention 构建脚本里 catalog 的 plugins
        // 访问器链不可解析（dependencies 访问器正常）。
        register("androidApplication") {
            id = "toolsmith.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "toolsmith.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
    }
}

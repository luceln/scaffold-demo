// app 模块。工程级约定（compileSdk/minSdk/targetSdk/bytecode/compose）
// 全部在 build-logic 的 convention 里，这里只写模块特有信息。
plugins {
    alias(libs.plugins.toolsmith.android.application)
    alias(libs.plugins.toolsmith.android.application.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.scaffolddemo"

    defaultConfig {
        applicationId = "com.example.scaffolddemo"
        versionCode = 1
        versionName = "0.1.0" // 语义化版本；发布节奏见 CHANGELOG.md
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // 骨架用 debug 签名让 release 产物可直接安装体验；
            // 正式发布前换掉（见 README「发布」一节）。别把正式 keystore 提交进仓库。
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.compose.material3)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // 纵切（§7.2 形态 B）：Retrofit + OkHttp + kotlinx-serialization
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinxSerializationConverter)
    implementation(libs.okhttp.loggingInterceptor)

    implementation(libs.timber)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    // 纵切测试的回环 HTTP 服务端：五条 CRUD 路径真打 OkHttp 栈
    testImplementation(libs.okhttp.mockwebserver)
}

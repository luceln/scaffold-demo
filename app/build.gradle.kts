// app 模块。工程级约定（compileSdk/minSdk/targetSdk/bytecode/compose）
// 全部在 build-logic 的 convention 里，这里只写模块特有信息。
plugins {
    // 真实插件在前：把 AGP / compose 编译器插件的实现类拉上主工程插件 classpath
    // （convention 里对它们是 compileOnly，运行期可见性来自这里）
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // 工程级约定
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
        // 仪器测试入口。AGP 默认值本就是它，显式写出来是为了「真机走查截图」这条链路
        // 一眼可查 —— 换 runner（如 orchestrator 分片）只改这一处。
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // dev / prod 两个构建变体：差异不只是字符串，还包括「数据从哪来、是否出网」
    // （dev 挂 MockInterceptor 读本地中文 mock；prod 走真网络栈），见 README「构建变体」。
    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev" // 与 prod 同机共存，互不覆盖数据
            versionNameSuffix = "-dev"
            // 假域名：请求在 OkHttp 层被 MockInterceptor 拦下，dev 零出网
            buildConfigField("String", "BASE_URL", "\"https://dev-api.example.com/\"")
            resValue("string", "app_name", "scaffold-demo dev") // 桌面上可区分
        }
        create("prod") {
            dimension = "env"
            // 占位域名：接自己后端时替换。未替换前列表页显示错误态（刻意保留的真实失败路径）
            buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
        }
    }

    buildFeatures {
        // AGP 9 默认不生成 BuildConfig —— 变体差异要靠 buildConfigField，所以开关必须打开
        buildConfig = true
        // gradle.properties 全局关了 resvalues（缩短配置期），dev 的应用名 override 需要它
        resValues = true
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
    // UI 组件库 + 图标集：miuix（HyperOS 观感，KMP 坐标）。
    // 图标只走 MiuixIcons.*，缺项补在 ui/icon/AppIcons.kt；骨架不自带 res/drawable/ic_*.xml
    implementation(libs.miuix.ui)
    implementation(libs.miuix.icons)
    // 类型安全导航：@Serializable 路由对象 → composable<T>() / toRoute<T>()
    implementation(libs.androidx.navigation.compose)

    // 依赖注入：Koin（无注解处理器，不拖构建）。koinViewModel() 在 -androidx-compose 里
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // 纵切（§7.2 形态 B）：Retrofit + OkHttp + kotlinx-serialization
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinxSerializationConverter)
    implementation(libs.okhttp.loggingInterceptor)

    implementation(libs.timber)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    // 纵切测试的回环 HTTP 服务端：五条 CRUD 路径真打 OkHttp 栈
    testImplementation(libs.okhttp.mockwebserver)

    // 仪器测试：**真机 UI 走查截图**（ScreenshotWalkthroughTest）。
    // 与单测的分工：单测锁逻辑（39 个/变体），仪器测试只负责「真跑起来、把屏拍下来」。
    // BOM 要在 androidTest 配置里再声明一次 —— platform 是逐配置生效的，不跨配置继承。
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    // ui-test-manifest 只给 debug 变体：它给测试补一个空壳 Activity 的 manifest 声明，
    // release 产物里不该有它。
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

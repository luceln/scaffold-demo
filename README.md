# scaffold-demo

Android 应用骨架：Kotlin + Jetpack Compose（Material 3）+ Retrofit/OkHttp，
由 [agent-toolsmith](https://github.com/luceln/agent-toolsmith) 的 `android-kotlin` 模板生成。
自带基础设施九件套与一条打通的 CRUD 纵切（五条路径真打 HTTP 栈的自动化测试）。

## 前置依赖

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | **17+** | 跑 Gradle 与 AGP 9（产物 bytecode 是 11 + desugaring，两回事） |
| Android SDK | Platform 36 | Android Studio 自带；纯命令行用 sdkmanager 装 |
| Android Studio | 建议 latest | 工程从 `settings.gradle.kts` 导入，无需提交 IDE 文件 |

## 命令对照

| 语义 | make | 等价 gradlew |
|---|---|---|
| 检查（格式+Lint+单测） | `make check` | `./gradlew spotlessCheck :app:lintDebug testDebugUnitTest` |
| 构建 | `make build` | `./gradlew assembleDebug` |
| 运行（装到连接的设备） | `make run` | `./gradlew installDebug` |

Windows 无 make 时直接用 gradlew 列。

## 纵切说明（这条链路是样板）

`DemoApi`（Retrofit 接口）→ `DemoRepository`（装配 OkHttp/序列化）→
`DemoRepositoryTest`（MockWebServer 回环，五条路径：分页/按 id/增/改/删）。

- 测试纯 JVM、离线、秒级；**新增网络资源时照这条链路的形状抄**
- 骨架的 `BASE_URL` 指向公开示例 API（jsonplaceholder），接自己的后端时改
  `DemoRepository.create` 的 baseUrl —— 但**别忘了同步改测试**（测试用 MockWebServer，不受影响）

## 发布

1. 在 `app/build.gradle.kts` 把 release 的 `signingConfig` 从 debug 换成正式签名配置
2. keystore 与密码**绝不入库**（`.gitignore` 已挡 `*.jks` / `*.keystore` / `keystore.properties`）；
   密码走环境变量或 CI 的 secret
3. 升 `versionCode` / `versionName`，在 `CHANGELOG.md` 记一行，打 tag

## 日志

统一走 Timber；`AppTree` 集中级别与脱敏（password/token/secret/authorization 键值对遮蔽）。
接崩溃上报或日志收集系统时替换 `DemoApplication.onCreate` 里 plant 的 Tree。

## 扩展路径（什么时候引入什么）

| 需求 | 建议 |
|---|---|
| 页面多于一个 | 引入 Navigation Compose；状态上收到 ViewModel（lifecycle-viewmodel-compose 已在目录） |
| 依赖开始交错 | 再考虑 DI 框架（Hilt 走 KSP）。骨架刻意手动注入，别提前加 |
| 多模块 | 照 build-logic 里 convention 的形态加 `toolsmith.android.library` |
| 换品牌 | 只改 `ui/theme/` 三个 token 文件 |

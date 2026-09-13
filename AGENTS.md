# AGENTS.md —— AI 结对编程宪法（本工程）

> 本文件由 agent-toolsmith 的 `android-kotlin` 模板在生成时写入，
> 技术栈声明已按本工程的实际技术栈填好。

## 核心身份

你是一位**极度严谨的资深架构师**。代码必须达生产级质量，表达必须通俗易懂。

## 项目技术栈声明

- 项目名称：scaffold-demo
- 技术栈：Kotlin 2.3 + Jetpack Compose (Material 3) + AGP 9.3 + Gradle 9.7 + Retrofit/OkHttp
- 运行平台：Android（minSdk 23 / target & compileSdk 36）
- 构建命令：`./gradlew assembleDebug`（或 `make build`）
- 测试命令：`./gradlew testDebugUnitTest`（纯 JVM，含纵切五条路径）
- Lint / 静态检查：`./gradlew spotlessCheck`（ktlint）+ `./gradlew :app:lintDebug`（Android Lint）；
  类型检查由 Kotlin 编译器承担（`allWarningsAsErrors` 可在 build-logic 里开）
- 本地运行命令：`./gradlew installDebug`（装到连接的设备/模拟器）
- 日志方案：Timber + `AppTree`（级别/脱敏集中在一处，禁裸 `Log` / `println`）
- UI 方案：Jetpack Compose + Material 3（品牌 token 集中在 `ui/theme/`，
  禁止自创基础组件，禁止 Emoji 当图标）
- 包管理：Gradle Wrapper + `gradle/libs.versions.toml` 版本目录（所有版本一处改）
- 主要目录约定：
  - `app/src/main/java/com.example.scaffolddemo/` —— 源码（包名 = applicationId）
  - `app/src/main/java/com.example.scaffolddemo/data/` —— 网络层（Api 接口 + Repository + 模型）
  - `app/src/main/java/com.example.scaffolddemo/ui/theme/` —— 品牌 token（色/字/形）
  - `app/src/main/java/com.example.scaffolddemo/logging/` —— 日志门面
  - `app/src/test/java/com.example.scaffolddemo/` —— JVM 单测（纵切在此）
  - `build-logic/` —— convention 插件（工程级约定集中处，先读再改）

## 工程级约定的位置（先读，别绕过）

`build-logic/convention/src/main/kotlin/` 集中了 compileSdk、bytecode target、
desugaring、Compose 开关等**全部工程级约定**。改这些行为时改 convention，
不要在模块 build.gradle.kts 里私自覆盖。

## 版本升级纪律

所有依赖版本只在 `gradle/libs.versions.toml` 一处声明。升级 = 改目录文件 +
跑 `make check` 全绿，禁止在模块脚本里写版本号覆盖目录。

## 生产级底线（编码默认意识）

1. 异常边界：网络失败/空列表/非法输入必须有降级路径（参考 `MainActivity` 的三态 UI）
2. 可观测性：错误走 `Timber.e` 带上下文；关键业务节点打 INFO
3. 性能：列表用 `LazyColumn` + 稳定 key；禁止主线程 IO
4. 状态一致：UI 状态用 sealed interface 表达完整状态集
5. 安全：密钥/签名不入库（`.gitignore` 已挡）；日志脱敏在 `AppTree`

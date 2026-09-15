# AGENTS.md —— AI 结对编程宪法（本工程）

> 本文件由 agent-toolsmith 的 `android-kotlin` 模板在生成时写入，
> 技术栈声明已按本工程的实际技术栈填好。

## 核心身份

你是一位**极度严谨的资深架构师**。代码必须达生产级质量，表达必须通俗易懂。

## 项目技术栈声明

- 项目名称：scaffold-demo
- 技术栈：Kotlin 2.3 + Jetpack Compose + miuix（HyperOS 观感 UI 组件库）+ AGP 9.3 + Gradle 9.7 + Retrofit/OkHttp
- 运行平台：Android（minSdk 24 / target 36 & compileSdk 37）
- 构建命令：`./gradlew assembleDebug`（或 `make build`）
- 测试命令：`./gradlew testDebugUnitTest`（纯 JVM，含纵切五条路径）
- Lint / 静态检查：`./gradlew spotlessCheck`（ktlint）+ `./gradlew :app:lintDebug`（Android Lint）；
  类型检查由 Kotlin 编译器承担（`allWarningsAsErrors` 可在 build-logic 里开）
- 本地运行命令：`./gradlew installDebug`（装到连接的设备/模拟器）
- 日志方案：Timber + `AppTree`（级别/脱敏集中在一处，禁裸 `Log` / `println`）
- UI 方案：Jetpack Compose + miuix（`top.yukonga.miuix.kmp`，品牌 token 与字阶集中在 `ui/theme/`；
  **禁自创基础原子组件**，图标用 `MiuixIcons` 体系、缺的补进 `ui/icon/AppIcons.kt`，禁止 Emoji 当图标）
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

## UI 交付双技巧（强制，不得跳过）

本工程**有 UI**（Jetpack Compose + miuix）。任何涉及界面的交付
（新增 / 修改屏幕、状态、交互）都必须走完下面两道闸门 —— **跳过即为违宪**。

**闸门一 · 低保真原型（开发前）**

1. 先列出准备画的：分组 / 屏幕 / 关键状态 / 跳转关系；
2. **停下等确认** —— 确认后才生成单页 HTML 低保真（零依赖、内联 CSS/JS、灰阶、
   hash 路由、每屏「标题行 + 屏框 + 注解卡」三段结构）；
3. 逐屏审阅，改到冻结。
   **未冻结之前，不许写任何 UI 代码。**

**闸门二 · 页面巡查（开发后）**

1. **实际运行产品**（真机 / 模拟器），按用户真实使用顺序截取核心页面与关键状态；
2. 生成走查页：按使用板块分组，每屏标注 页面名 / 当前状态 / **复现方式**，
   并带「可以 / 待改 / 阻塞」三档标记 + 反馈输入框；反馈自动本地保存、可导出 Markdown / JSON；
3. 把走查页交回来 → 按反馈逐条修改 → **回复具体修改措施** → 更新对应截图留档；
4. **回归**：新一轮保留上一轮反馈原话，改动过的页面加标记，逐条验证。
   **走查未收口，不算交付完成，不得提交。**

> 例外只有一种：该交付确实不含 UI（纯 CLI / 纯后端 / 纯库）。
> 例外必须由项目所有者判定 —— **AI 不得自行认定「这次 UI 简单、可以跳过」**。

**本工程怎么「跑起来 + 截图」**：

```bash
# 装 dev 变体并从启动器拉起（monkey 不依赖写死的 Activity 名，能适配 flavor 后缀）
adb install -r app/build/outputs/apk/dev/debug/app-dev-debug.apk
adb shell monkey -p com.example.scaffolddemo.dev -c android.intent.category.LAUNCHER 1
# 截当前屏到走查目录；用 exec-out（不是 shell），否则 Windows 下 \r\n 会破坏 PNG
adb exec-out screencap -p > .workbuddy/handoff/ui-patrol/shot-01-list.png
```

## 生产级底线（编码默认意识）

1. 异常边界：网络失败/空列表/非法输入必须有降级路径（参考 `MainActivity` 的三态 UI）
2. 可观测性：错误走 `Timber.e` 带上下文；关键业务节点打 INFO
3. 性能：列表用 `LazyColumn` + 稳定 key；禁止主线程 IO
4. 状态一致：UI 状态用 sealed interface 表达完整状态集
5. 安全：密钥/签名不入库（`.gitignore` 已挡）；日志脱敏在 `AppTree`

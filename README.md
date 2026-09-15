# scaffold-demo

Android 应用骨架：Kotlin + Jetpack Compose + **miuix**（HyperOS 观感 UI 组件库）+ Koin +
类型安全导航 + Retrofit/OkHttp，
由 [agent-toolsmith](https://github.com/luceln/agent-toolsmith) 的 `android-kotlin` 模板生成。

自带基础设施九件套、一条真打 HTTP 栈的 CRUD 纵切（五条路径的自动化测试），
以及三个**可直接照抄的界面样板**：列表 → 详情（带传参）、全屏搜索、四态演示。

UI 全栈是 miuix：组件只用 `MiuixTheme` / `MiuixIcons` 体系（**不自造基础原子组件**），
品牌色与字阶集中在 `ui/theme/`，图标集里确实缺的语义补在 `ui/icon/AppIcons.kt`。

## 前置依赖

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | **17+** | 跑 Gradle 与 AGP 9（产物 bytecode 是 11 + desugaring，两回事） |
| Android SDK | **Platform 37** | `compileSdk 37` 是 miuix 的硬要求；Android Studio 会提示安装 |
| Android Studio | 建议 latest | 工程从 `settings.gradle.kts` 导入，无需提交 IDE 文件 |

## 第一次跑

```bash
./gradlew :app:installDevDebug     # 装 dev 包（挂本地 mock，零出网，界面立刻有数据）
```

装好打开，底部三个 tab：

- **首页** —— 作者筛选 + 卡片列表 → 点进详情（`DetailRoute(id)` 传参，返回保留滚动与筛选）
- **能力** —— 组件画廊 / 网络实测（本地 mock ⇄ 真网络对照）/ 状态管理（四态演示）
- **我的** —— 构建变体的证据页（应用名、applicationId、BASE_URL、数据来源都来自构建配置）

## 构建变体：dev 与 prod

两个 flavor 的差异只有四处，而且**全部由构建配置与一处装配决定**，业务代码看不见 flavor：

| | dev | prod |
|---|---|---|
| 应用名 | `scaffold-demo dev` | `scaffold-demo` |
| applicationId | `com.example.scaffolddemo.dev`（与 prod 同机共存） | `com.example.scaffolddemo` |
| BASE_URL | `https://dev-api.example.com/`（假域名，不出网） | `https://api.example.com/`（占位，接自己后端时改） |
| 数据来源 | 本地 mock：请求在 OkHttp **最后一跳**被换成 `assets/mock/*.json` | 真网络栈 |

关键点：**mock 不是假的网络层**。Retrofit 接口、OkHttp 调用链、kotlinx-serialization、
协程调度、异常传播全部真跑，只换了 socket —— 所以"四态"是由真实数据层产生的，
切到空档刷新真的返空数组、切到失败档真的抛超时。

差异的落点是 `app/src/dev/` 与 `app/src/prod/` 下**同名**的 `di/FlavorModule.kt`：

- `dev`：注册 `MockControl`（`MockScenario`）与 `Interceptor`（`MockInterceptor`）
- `prod`：只注册 `MockControl` 的空实现（`enabled = false`），并**不挂拦截器**；
  `assets/mock/` 也不进这个包

`di/NetworkModule.kt` 只问一句 `getOrNull<Interceptor>()`：有就挂、没有就不挂。
这就是构建变体差异的全部实现。

## 命令对照

| 语义 | make | 等价 gradlew |
|---|---|---|
| 检查（格式 + Lint + 两个 flavor 的单测） | `make check` | `./gradlew spotlessCheck :app:lint testDevDebugUnitTest testProdDebugUnitTest` |
| 构建（dev + prod 两个 debug 包） | `make build` | `./gradlew assembleDebug` |
| 运行（装 dev 包到连接的设备） | `make run` | `./gradlew :app:installDevDebug` |

Windows 无 make 时直接用 gradlew 列。

⚠️ **加了 flavor 之后，"按构建类型命名"的聚合任务不复存在**：`lintDebug` /
`testDebugUnitTest` / `installDebug` 都会消失，改名为 `lint` / `testDevDebugUnitTest` /
`installDevDebug`（AGP 的变体命名规则）。`assembleDebug` 例外 —— 它按构建类型聚合，
一次编出两个 flavor 的包。本仓库的 CI（`.github/workflows/ci.yml`）只用 `check assembleDebug`
这一个稳定入口，不罗列任务清单。

## 能力清单（骨架给了什么，去哪看）

| 能力 | 在哪看 | 代码位置 |
|---|---|---|
| 三段式外壳（顶栏 action + 内容 + 底部 tab，切 tab 保状态） | 每个页面 | `ui/shell/AppShell.kt` |
| 类型安全导航 + 传参 | 列表 → 详情 | `ui/route/AppRoutes.kt`、`ui/AppNavHost.kt` |
| 四态（加载 / 成功 / 空 / 失败） | 首页、搜索、详情 | 各 `*ViewModel.kt` 的 `sealed interface` |
| 骨架屏（与真实内容同尺寸同位置，落数据不跳） | 首页 / 详情加载中 | `ui/common/StateViews.kt` |
| 统一错误映射（异常 → 用户可读文案 + 错误码） | 任何失败 | `ui/common/ErrorMapper.kt` |
| 依赖注入（Koin，无注解处理器） | 全部 | `di/AppModule.kt`、`di/NetworkModule.kt` |
| 本地 mock + 故障注入 | 状态管理演示 / 网络实测 | `data/mock/` |
| 真出网对照组（同一套栈，换数据来源） | 能力 → 网络实测 | `data/probe/` |
| 构建变体差异 | 我的 tab | `app/src/{dev,prod}/.../FlavorModule.kt` |

## 纵切说明（这条链路是样板）

`DemoApi`（Retrofit 接口）→ `DemoRepository`（薄封装）→ `DemoRepositoryTest`
（MockWebServer 回环，五条路径：分页 / 按 id / 增 / 改 / 删）。

- 装配（OkHttp / 序列化 / baseUrl）在 `di/NetworkModule.kt`，**不在 Repository 里**
  —— 所以 baseUrl 由构建变体决定，测试注入回环地址，同一份装配两种环境
- 测试纯 JVM、离线、秒级；**新增网络资源时照这条链路的形状抄**（加方法、加测试）

## 接自己的后端

1. 改 `app/build.gradle.kts` 里 prod 的 `BASE_URL`（dev 的假域名可以留着不动，反正不出网）
2. 把 `app/src/dev/assets/mock/*.json` 换成自己的假数据（字段名对齐 `demo/data/` 下的 DTO）
3. 接口路径变了就改 `DemoApi`；`MockInterceptor` 的白名单也要同步（只拦已声明的路径，
   其余一律放行到真网络 —— 这条语义是"网络实测"能真出网的基础）
4. 不需要 mock 了：删掉 `app/src/dev/di/FlavorModule.kt` 里的 `single<Interceptor>` 一行即可

## 发布

1. 在 `app/build.gradle.kts` 把 release 的 `signingConfig` 从 debug 换成正式签名配置
2. keystore 与密码**绝不入库**（`.gitignore` 已挡 `*.jks` / `*.keystore` / `keystore.properties`）；
   密码走环境变量或 CI 的 secret
3. 升 `versionCode` / `versionName`，在 `CHANGELOG.md` 记一行，打 tag
4. 发布包用 `./gradlew assembleProdRelease`；产物在 `app/build/outputs/apk/prod/release/`

## 日志

统一走 Timber；`AppTree` 集中级别与脱敏（password/token/secret/authorization 键值对遮蔽）。
接崩溃上报或日志收集系统时替换 `DemoApplication.onCreate` 里从容器取的 Tree。

启动日志带 `applicationId` / `baseUrl` / `mock` 三项，切 flavor 装两个包看这一行就能确认差异：

```bash
adb logcat | grep "应用启动"
```

## 换行符（别删 `.gitattributes`）

`spotlessCheck` 的判定依赖它：Spotless 的 `lineEndings` 默认取 `GIT_ATTRIBUTES`，
缺了这个文件会退化成"平台默认行尾"，于是同一份代码在 Windows 上红、Linux 上绿
—— 那种只在一台机器上红的假故障极难查。全仓统一 LF。

## 扩展路径（什么时候引入什么）

| 需求 | 建议 |
|---|---|
| 列表变长 | 上 Paging；现在是本地切片（`_start` / `_limit` 已真跑，只差 UI 触发） |
| 需要深链 / 外部唤起 | 给 `ui/route/AppRoutes.kt` 的路由加 `deepLinks`（类型安全路由原生支持） |
| 平板 / 折叠屏 | `ui/shell/` 加一个"列表 + 详情"双栏变体，按窗口宽度切换 |
| 换品牌 | 只改 `ui/theme/` 三个 token 文件 |
| 多模块 | 照 build-logic 里 convention 的形态加 `toolsmith.android.library` |

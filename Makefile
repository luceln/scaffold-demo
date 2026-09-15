# 统一任务入口（九件套 #6）：make check / make run / make build
# Windows 无 make 时直接用等价的 gradlew 命令（见 README「命令对照」）。
#
# ⚠️ 任务名都带构建变体：加上 dev/prod 两个 productFlavor 之后，
# `lintDebug` / `testDebugUnitTest` / `installDebug` 这些"按构建类型命名"的
# 聚合任务**不复存在**（AGP 的变体命名规则，已实测）。对应关系：
#   lintDebug          → lint（不分构建类型，覆盖全部变体）
#   testDebugUnitTest  → testDevDebugUnitTest / testProdDebugUnitTest（两档都要跑）
#   installDebug       → installDevDebug
# assembleDebug 不受影响：它按构建类型聚合，一次编出两个 flavor 的包。

GRADLE := ./gradlew --console=plain --no-daemon

.PHONY: check run build clean

# 格式检查 + Android Lint + 两个 flavor 的单测（含纵切）：PR 必过三件
check:
	$(GRADLE) spotlessCheck :app:lint testDevDebugUnitTest testProdDebugUnitTest

# 构建 debug 变体 APK（dev 与 prod 两个包）
build:
	$(GRADLE) assembleDebug

# 运行 = 安装到当前连接的设备/模拟器（Android 没有"本机运行"语义）。
# 装 dev 包：它挂本地 mock、零出网，适合走查界面。
# 无设备时 gradle 会报 No connected devices —— 不是构建坏了。
run:
	$(GRADLE) installDevDebug

clean:
	$(GRADLE) clean

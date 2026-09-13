# 统一任务入口（九件套 #6）：make check / make run / make build
# Windows 无 make 时直接用等价的 gradlew 命令（见 README「命令对照」）。

GRADLE := ./gradlew --console=plain --no-daemon

.PHONY: check run build clean

# 格式检查 + Android Lint + 单测（含纵切）：PR 必过三件
check:
	$(GRADLE) spotlessCheck :app:lintDebug testDebugUnitTest

# 构建debug 变体 APK
build:
	$(GRADLE) assembleDebug

# 运行 = 安装到当前连接的设备/模拟器（Android 没有"本机运行"语义）。
# 无设备时 gradle 会报 No connected devices —— 不是构建坏了。
run:
	$(GRADLE) installDebug

clean:
	$(GRADLE) clean

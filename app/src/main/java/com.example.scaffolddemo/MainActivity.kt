package com.example.scaffolddemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.scaffolddemo.ui.shell.AppShell
import com.example.scaffolddemo.ui.theme.AppTheme

/**
 * 唯一 Activity：只负责挂上主题与外壳，**状态 / 网络 / 导航全部下沉**。
 *
 * - 页面状态在 ViewModel（容器提供），Activity 不再持有任何页面状态；
 * - 三段式外壳（顶栏 + 内容 + 底部 tab）在 `ui/shell/AppShell.kt`；
 * - 导航图在 `ui/AppNavHost.kt`。新页面加在那里，不在这里加代码。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppShell()
            }
        }
    }
}

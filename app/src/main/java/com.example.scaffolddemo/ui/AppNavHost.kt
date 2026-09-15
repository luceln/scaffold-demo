package com.example.scaffolddemo.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.scaffolddemo.ui.ability.AbilityScreen
import com.example.scaffolddemo.ui.ability.GalleryScreen
import com.example.scaffolddemo.ui.ability.NetworkProbeScreen
import com.example.scaffolddemo.ui.ability.StateDemoScreen
import com.example.scaffolddemo.ui.home.DetailScreen
import com.example.scaffolddemo.ui.home.HomeScreen
import com.example.scaffolddemo.ui.mine.MineScreen
import com.example.scaffolddemo.ui.route.AbilityRoute
import com.example.scaffolddemo.ui.route.DetailRoute
import com.example.scaffolddemo.ui.route.GalleryRoute
import com.example.scaffolddemo.ui.route.HomeRoute
import com.example.scaffolddemo.ui.route.MineRoute
import com.example.scaffolddemo.ui.route.NetworkProbeRoute
import com.example.scaffolddemo.ui.route.SearchRoute
import com.example.scaffolddemo.ui.route.StateDemoRoute
import com.example.scaffolddemo.ui.search.SearchScreen
import com.example.scaffolddemo.ui.shell.TopLevelDestination

/**
 * 导航图：一处声明全部目的地。
 *
 * 类型安全路由写法要点：
 * - `composable<HomeRoute> { }` —— 目的地是 `@Serializable` 对象，拼错名字编译不过；
 * - 传参用 `entry.toRoute<DetailRoute>()` 取，不再是 `arguments?.getString(...)`；
 * - 返回统一 `popBackStack()`：顶栏箭头与系统返回键走同一条路。
 *
 * 新增页面：这里加一行 `composable<XxxRoute> { XxxScreen(...) }`，
 * 顶层 tab 的话再往 `TopLevelDestination` 加一项。
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    onSelectTab: (TopLevelDestination) -> Unit,
) {
    NavHost(navController = navController, startDestination = HomeRoute) {
        // ---- 顶层目的地（各带底部 tab）----

        composable<HomeRoute> {
            HomeScreen(
                onSelectTab = onSelectTab,
                onOpenSearch = { navController.navigate(SearchRoute) },
                onOpenDetail = { id -> navController.navigate(DetailRoute(id)) },
            )
        }

        composable<AbilityRoute> {
            AbilityScreen(
                onSelectTab = onSelectTab,
                onOpenGallery = { navController.navigate(GalleryRoute) },
                onOpenNetworkProbe = { navController.navigate(NetworkProbeRoute) },
                onOpenStateDemo = { navController.navigate(StateDemoRoute) },
            )
        }

        composable<MineRoute> {
            MineScreen(onSelectTab = onSelectTab)
        }

        // ---- 下级页（进入后底栏消失）----

        composable<SearchRoute> {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onOpenDetail = { id -> navController.navigate(DetailRoute(id)) },
            )
        }

        composable<DetailRoute> { entry ->
            // 只从路由取 id：进程被杀重建后参数依然正确，不依赖内存里的对象
            val route = entry.toRoute<DetailRoute>()
            DetailScreen(
                articleId = route.id,
                onBack = { navController.popBackStack() },
            )
        }

        composable<GalleryRoute> {
            GalleryScreen(onBack = { navController.popBackStack() })
        }

        composable<NetworkProbeRoute> {
            NetworkProbeScreen(onBack = { navController.popBackStack() })
        }

        composable<StateDemoRoute> {
            StateDemoScreen(onBack = { navController.popBackStack() })
        }
    }
}

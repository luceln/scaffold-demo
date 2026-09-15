package com.example.scaffolddemo.di

import com.example.scaffolddemo.logging.AppTree
import com.example.scaffolddemo.ui.ability.NetworkProbeViewModel
import com.example.scaffolddemo.ui.ability.StateDemoViewModel
import com.example.scaffolddemo.ui.home.DetailViewModel
import com.example.scaffolddemo.ui.home.HomeViewModel
import com.example.scaffolddemo.ui.mine.MineViewModel
import com.example.scaffolddemo.ui.search.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * 进程级单例与 ViewModel 声明。
 *
 * 为什么 ViewModel 也进容器：`viewModel { }` + 界面里 `koinViewModel()` 是 Koin 的主要卖点，
 * 而且两个页面各自有加载态 —— 状态留在 Activity 里没法同时服侍它们（也会写出"Activity 持多个
 * 页面状态"的坏样板）。
 *
 * `get()` 是显式的构造注入：把依赖写在构造参数上，装配写在容器里，两边都不藏。
 */
val appModule =
    module {

        // 日志门面（九件套 #2）：Timber Tree 由容器持有，`DemoApplication` 从容器取。
        // 这里用行注释而不是 KDoc —— ktlint 的 standard:kdoc 不允许在 `module { }` 这类块内放 KDoc。
        single { AppTree() }

        viewModel { HomeViewModel(get()) }
        viewModel { DetailViewModel(get()) }
        viewModel { SearchViewModel(get()) }
        viewModel { NetworkProbeViewModel(get(), get()) }
        // 「状态管理」与「我的」两个屏都只从容器取一样东西：mock 是否启用（按 flavor 装配）
        viewModel { StateDemoViewModel(get()) }
        viewModel { MineViewModel(get()) }
    }

package com.example.scaffolddemo.ui.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scaffolddemo.BuildConfig
import com.example.scaffolddemo.R
import com.example.scaffolddemo.ui.shell.TopLevelDestination
import com.example.scaffolddemo.ui.shell.TopLevelScaffold
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch

/**
 * 「我的」tab：**构建变体的证据页**。
 *
 * 应用名 / applicationId / BASE_URL / 数据来源 这四项**全部来自构建配置**，不是写死的文案：
 * 装上 `dev` 与 `prod` 两个包，这一页的内容会不一样 —— 这是 flavor 真的生效的凭据
 * （尤其应用名那条：它读的就是 `resValue` 覆盖过的 `R.string.app_name`）。
 *
 * 两个开关用 `rememberSaveable` 而不是 ViewModel：它们只是**页面内的 UI 偏好**，
 * 旋转屏幕要留住，但跟数据层没关系。真接到设置项时再提升到 DataStore —— 那时它才是"状态"。
 *
 * 组件形态：M3 的 `ListItem(headlineContent / supportingContent / trailingContent)` 换成
 * miuix 的 `BasicComponent(title / summary / startAction / endActions)` —— 同样是两行 + 尾随槽位，
 * 只是**主/次两行收字符串**、不再需要嵌套 lambda。分组标题用 `SmallTitle`。
 */
@Composable
fun MineScreen(
    onSelectTab: (TopLevelDestination) -> Unit,
    viewModel: MineViewModel = koinViewModel(),
) {
    // 页面内的 UI 偏好：旋转屏幕要留住（rememberSaveable），但跟数据层无关，所以不提升到 ViewModel
    var notifyEnabled by rememberSaveable { mutableStateOf(true) }
    var darkFollowSystem by rememberSaveable { mutableStateOf(false) }

    TopLevelScaffold(
        destination = TopLevelDestination.MINE,
        onSelectTab = onSelectTab,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            GroupLabel(text = stringResource(R.string.mine_group_app_info))

            InfoRow(label = stringResource(R.string.mine_label_app_name), value = stringResource(R.string.app_name))
            InfoRow(label = stringResource(R.string.mine_label_application_id), value = BuildConfig.APPLICATION_ID)
            InfoRow(label = stringResource(R.string.mine_label_base_url), value = BuildConfig.BASE_URL)
            InfoRow(
                label = stringResource(R.string.mine_label_data_source),
                value =
                    stringResource(
                        if (viewModel.mockEnabled) R.string.mine_data_source_mock else R.string.mine_data_source_real,
                    ),
            )
            InfoRow(
                label = stringResource(R.string.mine_label_build_type),
                value =
                    stringResource(
                        if (BuildConfig.BUILD_TYPE == RELEASE_BUILD_TYPE) {
                            R.string.mine_build_release
                        } else {
                            R.string.mine_build_debug
                        },
                    ),
            )
            InfoRow(
                label = stringResource(R.string.mine_label_version),
                value =
                    stringResource(
                        R.string.mine_version_format,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE,
                    ),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            GroupLabel(text = stringResource(R.string.mine_group_settings))

            SwitchRow(
                title = stringResource(R.string.mine_switch_notify),
                checked = notifyEnabled,
                onCheckedChange = { notifyEnabled = it },
            )
            SwitchRow(
                title = stringResource(R.string.mine_switch_dark),
                checked = darkFollowSystem,
                onCheckedChange = { darkFollowSystem = it },
            )

            // 清缓存/关于目前是占位项：**如实写"当前 0 B"**，不假装已经做了清理
            BasicComponent(
                title = stringResource(R.string.mine_item_clear_cache),
                summary = stringResource(R.string.mine_clear_cache_size),
            )
            BasicComponent(
                title = stringResource(R.string.mine_item_about),
                summary = stringResource(R.string.mine_about_desc),
            )
        }
    }
}

/** 分组标题：miuix 的 `SmallTitle`（用 `subtitle` 字阶），不自造文字样式。 */
@Composable
private fun GroupLabel(text: String) {
    SmallTitle(text = text)
}

/** 信息行：字段名走主行、字段值走次行（长值自动换行，不截断）。 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    BasicComponent(
        title = label,
        summary = value,
    )
}

/**
 * 开关行：**状态提升**的惯用写法 —— 状态放在调用方（`MineScreen`），这一行只负责显示与回调。
 * 可组合项自己不藏状态，复用与测试都简单。
 */
@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    BasicComponent(
        title = title,
        endActions = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
    )
}

private const val RELEASE_BUILD_TYPE = "release"

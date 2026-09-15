package com.example.scaffolddemo.ui.ability

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scaffolddemo.R
import com.example.scaffolddemo.data.probe.ProbeArticle
import com.example.scaffolddemo.data.probe.ProbeMeta
import com.example.scaffolddemo.data.probe.ProbeSourceKind
import com.example.scaffolddemo.ui.common.EmptyView
import com.example.scaffolddemo.ui.common.ErrorView
import com.example.scaffolddemo.ui.shell.SubPageScaffold
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.Locale

/**
 * 网络实测：**同一套列表、同一个状态机，只换数据来源**。
 *
 * 这一屏回答一个很实在的问题 ——「你给我的 mock，到底跟真网络差在哪」。所以它把两条通路摆在
 * 一起对照，并且把「真出网的活证据」（状态码 / 耗时 / 字节数 / 条数）**直接打在屏上**：
 *
 * - 「本地 mock」：走正常 `DemoRepository` → 被 `MockInterceptor` 拦下，零出网、状态码一栏写「未走 HTTP」；
 * - 「真实网络」：走绝对地址 `@Url` → 同一份 Retrofit / OkHttp 栈，真 DNS、真 TLS、真 socket。
 *
 * 默认停在 [ProbeUiState.Idle]，**点了右上角才发请求**。
 *
 * ⚠️ **一处 miuix 导致的形态收缩（待拍板）**：M3 版里 mock 这一档在 `prod` 下是**灰掉**的
 * （`SegmentedButton(enabled = false)`），而 miuix 的 `TabRow` **没有单项禁用**的能力
 * （内部 tab 项只有 `onClick`，没有 `enabled`）。既然做不出"看得见的灰"，
 * 就不摆一个点了没反应的假控件：`prod` 下**整条切换器不显示**，只留一句
 * 「当前构建未启用 mock」的说明。真机走查时按这个形态判断，要改再说。
 */
@Composable
fun NetworkProbeScreen(
    onBack: () -> Unit,
    viewModel: NetworkProbeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val source by viewModel.source.collectAsStateWithLifecycle()
    val requesting = state is ProbeUiState.Loading

    SubPageScaffold(
        title = stringResource(R.string.title_network_probe),
        onBack = onBack,
        actions = {
            IconButton(onClick = viewModel::request, enabled = !requesting) {
                Icon(
                    imageVector = MiuixIcons.Refresh,
                    contentDescription = stringResource(R.string.probe_action_request),
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SourceSwitcher(
                source = source,
                mockEnabled = viewModel.mockEnabled,
                onSelect = viewModel::selectSource,
            )
            if (!viewModel.mockEnabled) {
                // prod：说清"为什么这里没有 mock 档"，而不是留一个点不动的控件让人猜
                Note(text = stringResource(R.string.probe_mock_disabled))
            }

            MetaPanel(state = state, source = source)

            GroupHeader(source = source)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val current = state) {
                    ProbeUiState.Idle ->
                        Note(
                            // 提示文案分变体：dev 的主链路确实零出网，prod 不是 —— 同一句话在 prod 下是错的
                            text =
                                stringResource(
                                    if (viewModel.mockEnabled) R.string.probe_idle_hint else R.string.probe_idle_hint_prod,
                                ),
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            centered = true,
                        )

                    is ProbeUiState.Loading -> Requesting()

                    is ProbeUiState.Error -> ErrorView(kind = current.kind, onRetry = viewModel::request)

                    is ProbeUiState.Success -> {
                        val articles = current.result.articles
                        if (articles.isEmpty()) {
                            EmptyView(message = stringResource(R.string.error_not_found))
                        } else {
                            ArticleList(articles = articles)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 两档切换：M3 的 `SingleChoiceSegmentedButtonRow` → miuix `TabRow`，**几乎 1:1**。
 *
 * `prod` 下不出控件（原因见文件头注释：miuix 的 `TabRow` 做不出单档禁用）。
 */
@Composable
private fun SourceSwitcher(
    source: ProbeSourceKind,
    mockEnabled: Boolean,
    onSelect: (ProbeSourceKind) -> Unit,
) {
    if (!mockEnabled) return
    val kinds = ProbeSourceKind.entries
    TabRow(
        tabs = kinds.map { stringResource(it.labelRes) },
        selectedTabIndex = kinds.indexOf(source),
        onTabSelected = { index -> onSelect(kinds[index]) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

/**
 * 元信息面板：来源 / 请求地址 / 结果。**三行在所有状态下都在**（没值就写占位符），
 * 这样切档时版面不跳，也能一眼看出"哪一档还没请求过"。
 */
@Composable
private fun MetaPanel(
    state: ProbeUiState,
    source: ProbeSourceKind,
) {
    val meta = (state as? ProbeUiState.Success)?.result?.meta
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        KeyValue(label = stringResource(R.string.probe_kv_source), value = stringResource(source.sourceLabelRes))
        KeyValue(label = stringResource(R.string.probe_kv_request), value = meta?.url ?: PLACEHOLDER)
        KeyValue(
            label = stringResource(R.string.probe_kv_result),
            value = meta?.let { formatMeta(it) } ?: PLACEHOLDER,
        )
    }
}

/** 「结果」一行：`状态码 · 耗时 · 体积 · 条数`。mock 档没有状态码，走 [R.string.probe_meta_no_code]。 */
@Composable
private fun formatMeta(meta: ProbeMeta): String =
    stringResource(
        R.string.probe_meta_format,
        meta.code?.toString() ?: stringResource(R.string.probe_meta_no_code),
        meta.elapsedMs,
        formatSize(meta.bytes),
        meta.count,
    )

/** 体积：1 KB 以下给字节（数字更实在），以上给一位小数的 KB。 */
@Composable
private fun formatSize(bytes: Long): String =
    if (bytes < BYTES_PER_KB) {
        stringResource(R.string.probe_size_b, bytes)
    } else {
        stringResource(R.string.probe_size_kb, String.format(Locale.US, "%.1f", bytes / BYTES_PER_KB.toDouble()))
    }

@Composable
private fun KeyValue(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.weight(0.3f),
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.footnote2,
            modifier = Modifier.weight(0.7f),
        )
    }
}

/** 列表区小标题：说明"接下来这坨数据是从哪来的"。 */
@Composable
private fun GroupHeader(source: ProbeSourceKind) {
    val isReal = source == ProbeSourceKind.REAL
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(if (isReal) R.string.probe_group_real else R.string.probe_group_mock),
            style = MiuixTheme.textStyles.headline2,
            color = MiuixTheme.colorScheme.onBackgroundVariant,
        )
        Text(
            text = stringResource(if (isReal) R.string.probe_group_real_desc else R.string.probe_group_mock_desc),
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun Requesting() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.probe_action_requesting),
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun ArticleList(articles: List<ProbeArticle>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(articles, key = { it.title }) { article ->
            ProbeArticleCard(article = article)
        }
    }
}

/**
 * 实测列表卡片：**刻意不复用首页的 `ArticleCard`**。
 *
 * 首页卡片带 id、作者、日期，那是业务字段；这里只有"标题 + 一行副标题"，
 * 而且不可点（实测屏不接详情）—— 复用会引入一堆 `null` 参数和一个假的点击。
 */
@Composable
private fun ProbeArticleCard(article: ProbeArticle) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = article.title,
                style = MiuixTheme.textStyles.body1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (article.subtitle.isNotBlank()) {
                Text(
                    text = article.subtitle,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** 说明性文字。[centered] 用于整块留白区的提示。 */
@Composable
private fun Note(
    text: String,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.footnote1,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        modifier = modifier.padding(horizontal = 16.dp, vertical = if (centered) 0.dp else 8.dp),
    )
}

private val ProbeSourceKind.labelRes: Int
    @StringRes get() =
        when (this) {
            ProbeSourceKind.MOCK -> R.string.probe_seg_mock
            ProbeSourceKind.REAL -> R.string.probe_seg_real
        }

private val ProbeSourceKind.sourceLabelRes: Int
    @StringRes get() =
        when (this) {
            ProbeSourceKind.MOCK -> R.string.probe_source_mock
            ProbeSourceKind.REAL -> R.string.probe_source_real
        }

private const val BYTES_PER_KB = 1024
private const val PLACEHOLDER = "—"

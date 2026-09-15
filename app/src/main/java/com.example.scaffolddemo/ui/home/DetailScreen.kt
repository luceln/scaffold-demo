package com.example.scaffolddemo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scaffolddemo.R
import com.example.scaffolddemo.ui.common.ArticleUi
import com.example.scaffolddemo.ui.common.ErrorView
import com.example.scaffolddemo.ui.common.SkeletonDetail
import com.example.scaffolddemo.ui.icon.AppIcons
import com.example.scaffolddemo.ui.shell.SubPageScaffold
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 详情页：tab 内下级页 ⇒ **底栏整条隐藏**（用 [SubPageScaffold]，它没有 bottomBar）。
 *
 * 三态与首页同构（骨架屏 / 错误 / 内容），区别在错误态：404 不给「重试」（重试也没用），
 * 只给「返回列表」—— 出口比按钮诚实。
 */
@Composable
fun DetailScreen(
    articleId: Long,
    onBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel(),
) {
    // 只在 id 变化时拉一次：重组不会重复发起请求
    LaunchedEffect(articleId) { viewModel.load(articleId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SubPageScaffold(
        title = stringResource(R.string.title_detail),
        onBack = onBack,
        actions = {
            // 点赞：miuix 图标集里没有语义对得上的形态 ⇒ 用项目自有图标 AppIcons.ThumbUp
            // （来源与许可写在那个文件的 KDoc 里；脚手架自带这枚自绘示范，缺图标时照抄）
            IconButton(onClick = { /* 点赞：骨架只演示入口，接业务时补 */ }) {
                Icon(AppIcons.ThumbUp, contentDescription = stringResource(R.string.detail_action_like))
            }
            // 收藏：语义位由 miuix 的 Favorites 承担（HyperOS 里"收藏"就是它）
            IconButton(onClick = { /* 收藏 */ }) {
                Icon(MiuixIcons.Favorites, contentDescription = stringResource(R.string.detail_action_star))
            }
            IconButton(onClick = { /* 分享 */ }) {
                Icon(MiuixIcons.Share, contentDescription = stringResource(R.string.detail_action_share))
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                DetailUiState.Loading -> SkeletonDetail()

                is DetailUiState.Error ->
                    ErrorView(
                        kind = state.kind,
                        fallbackActionLabel = stringResource(R.string.detail_back_to_list),
                        onFallbackAction = onBack,
                    )

                is DetailUiState.Success ->
                    DetailContent(
                        article = state.article,
                        authorEmail = state.authorEmail,
                    )
            }
        }
    }
}

@Composable
private fun DetailContent(
    article: ArticleUi,
    authorEmail: String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = article.title, style = MiuixTheme.textStyles.title2)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuthorAvatar(name = article.authorName)
            Column {
                if (article.authorName.isNotBlank()) {
                    Text(text = article.authorName, style = MiuixTheme.textStyles.subtitle)
                }
                if (authorEmail.isNotBlank()) {
                    Text(
                        text = authorEmail,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                if (article.date.isNotBlank()) {
                    Text(
                        text = article.date,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }

        HorizontalDivider()

        // 正文按段落分块显示（不引入 Markdown 渲染：P1 的事，现在不提前加依赖）
        article.body.split("\n\n").filter { it.isNotBlank() }.forEach { paragraph ->
            Text(text = paragraph, style = MiuixTheme.textStyles.body1)
        }
    }
}

/**
 * 头像占位：纯色圆 + 作者名首字。
 *
 * 不引图片加载库（Coil 属 P1）：骨架的 demo 里没有网络图，为它加一个依赖不划算。
 * 接真实头像时把这里换成 AsyncImage 即可，调用点不变。
 */
@Composable
private fun AuthorAvatar(name: String) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MiuixTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.take(1),
            style = MiuixTheme.textStyles.headline2,
            color = MiuixTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
        )
    }
}

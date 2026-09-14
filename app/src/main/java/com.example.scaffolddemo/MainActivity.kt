package com.example.scaffolddemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scaffolddemo.data.DemoItem
import com.example.scaffolddemo.data.DemoRepository
import com.example.scaffolddemo.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 唯一 Activity：展示纵切网络层拉到的列表。
 *
 * UI 状态三态（加载 / 错误 / 数据）用 sealed interface 表达 ——
 * 新页面照这个形状起手，别用可空标志位组合状态。
 */
class MainActivity : ComponentActivity() {
    private val state = MutableStateFlow<UiState>(UiState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val uiState by state.collectAsStateWithLifecycle()
                DemoScreen(uiState)
            }
        }
        load()
    }

    private fun load() {
        // 简单起见直接在 Activity 发起；接 ViewModel 的时机见 README「扩展路径」
        state.value = UiState.Loading
        val repository = DemoRepository.create()
        CoroutineScope(Dispatchers.Main).launch {
            state.value =
                try {
                    UiState.Data(repository.page(start = 0, limit = 20))
                } catch (e: Exception) {
                    Timber.e(e, "加载 demo 列表失败 url=${DemoRepository.BASE_URL}")
                    UiState.Error
                }
        }
    }
}

private sealed interface UiState {
    data object Loading : UiState

    data object Error : UiState

    data class Data(
        val items: List<DemoItem>,
    ) : UiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoScreen(state: UiState) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.app_name)) }) },
    ) { innerPadding ->
        when (state) {
            UiState.Loading -> LoadingView(innerPadding)
            UiState.Error -> ErrorView(innerPadding)
            is UiState.Data -> ListView(innerPadding, state.items)
        }
    }
}

@Composable
private fun LoadingView(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(R.string.load_failed), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ListView(
    padding: PaddingValues,
    items: List<DemoItem>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(items, key = { it.id }) { item ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                Text(text = item.body, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

package com.samuelkrissi.orot.ui.reader

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.TextDecrease
import androidx.compose.material.icons.outlined.TextIncrease
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelkrissi.orot.catalog.Books
import com.samuelkrissi.orot.ui.common.ErrorState
import com.samuelkrissi.orot.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    lessonId: Long,
    viewModel: ReaderViewModel,
    onBack: () -> Unit,
    onOpenLesson: (String, Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val dark = isSystemInDarkTheme()
    LaunchedEffect(bookId, lessonId) { viewModel.load(bookId, lessonId) }

    val bookTitle = Books.byId(bookId)?.titleHe.orEmpty()
    val html = remember(state.lesson, settings.fontSize, dark) {
        viewModel.document(dark, settings.fontSize)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.lesson?.ref?.title ?: bookTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "חזרה")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::shrinkFont) {
                        Icon(Icons.Outlined.TextDecrease, contentDescription = "הקטן גופן")
                    }
                    IconButton(onClick = viewModel::growFont) {
                        Icon(Icons.Outlined.TextIncrease, contentDescription = "הגדל גופן")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { state.previous?.let { onOpenLesson(bookId, it.id) } },
                        enabled = state.previous != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (state.previous != null) "הפסקה הקודמת" else "תחילת הספר")
                    }
                    TextButton(
                        onClick = { state.next?.let { onOpenLesson(bookId, it.id) } },
                        enabled = state.next != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (state.next != null) "הפסקה הבאה" else "סוף הספר")
                    }
                }
            }
        },
    ) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.error != null -> ErrorState(
                message = state.error ?: "",
                onRetry = { viewModel.load(bookId, lessonId) },
                modifier = Modifier.padding(padding),
            )
            html != null -> Column(Modifier.padding(padding).fillMaxSize()) {
                ReaderWebView(html = html, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun ReaderWebView(html: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val webView = remember(context) {
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = false
            settings.defaultTextEncodingName = "utf-8"
            isVerticalScrollBarEnabled = true
        }
    }
    AndroidView(
        factory = { webView },
        update = { view ->
            view.loadDataWithBaseURL(
                "https://yhb.org.il/",
                html,
                "text/html",
                "utf-8",
                null,
            )
        },
        modifier = modifier,
    )
}

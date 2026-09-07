package com.samuelkrissi.orot.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelkrissi.orot.catalog.Books
import com.samuelkrissi.orot.ui.common.ErrorState
import com.samuelkrissi.orot.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    bookId: String?,
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onOpenLesson: (String, Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val book = bookId?.let { Books.byId(it) }
    val title = if (book != null) "חיפוש ב${book.titleHe}" else "חיפוש בסדרה"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "חזרה")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.onQuery(it, book) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("מילה או ביטוי…") },
            )
            when {
                state.loading -> LoadingState()
                state.error != null -> ErrorState(
                    message = state.error ?: "",
                    onRetry = { viewModel.onQuery(state.query, book) },
                )
                state.query.trim().length < 2 -> Text(
                    text = "הקלידו לפחות שתי אותיות. החיפוש נעשה באתר ישיבת הר ברכה.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
                state.results.isEmpty() -> Text(
                    text = "לא נמצאו תוצאות",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp),
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.results, key = { it.lesson.id }) { hit ->
                        val targetBookId = hit.book?.id ?: bookId ?: Books.all.first().id
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenLesson(targetBookId, hit.lesson.id) },
                        ) {
                            Text(
                                text = hit.book?.titleHe ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(hit.lesson.title, style = MaterialTheme.typography.titleLarge)
                            if (hit.lesson.excerpt.isNotBlank()) {
                                Text(
                                    text = hit.lesson.excerpt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.samuelkrissi.orot.ui.book

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelkrissi.orot.catalog.Book
import com.samuelkrissi.orot.catalog.Chapter
import com.samuelkrissi.orot.catalog.LessonRef
import com.samuelkrissi.orot.ui.common.ErrorState
import com.samuelkrissi.orot.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    book: Book,
    viewModel: BookViewModel,
    onBack: () -> Unit,
    onOpenLesson: (Long) -> Unit,
    onSearch: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(book.id) { viewModel.load(book) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book.titleHe) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "חזרה")
                    }
                },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Outlined.Search, contentDescription = "חיפוש בספר")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        when {
            state.loading && state.chapters.isEmpty() ->
                LoadingState(Modifier.padding(padding))
            state.error != null && state.chapters.isEmpty() ->
                ErrorState(
                    message = state.error ?: "",
                    onRetry = { viewModel.load(book, force = true) },
                    modifier = Modifier.padding(padding),
                )
            else -> BookContent(
                book = book,
                state = state,
                padding = padding,
                onQuery = viewModel::setQuery,
                onToggle = viewModel::toggleChapter,
                onOpenLesson = onOpenLesson,
            )
        }
    }
}

@Composable
private fun BookContent(
    book: Book,
    state: BookUiState,
    padding: PaddingValues,
    onQuery: (String) -> Unit,
    onToggle: (String) -> Unit,
    onOpenLesson: (Long) -> Unit,
) {
    val chapters = state.visibleChapters
    val lessonCount = state.chapters.sumOf { it.lessonCount }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "${chapters.size} פרקים · $lessonCount פסקאות",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.query,
                onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("סינון פרקים ופסקאות ב${book.titleHe}") },
            )
        }
        items(chapters, key = { it.code }) { chapter ->
            ChapterCard(
                chapter = chapter,
                expanded = state.expanded.contains(chapter.code) || state.query.isNotBlank(),
                onToggle = { onToggle(chapter.code) },
                onOpenLesson = onOpenLesson,
            )
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenLesson: (Long) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(chapter.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${chapter.lessonCount} פסקאות",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
            )
        }
        if (expanded) {
            Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 10.dp)) {
                chapter.lessons.forEach { lesson ->
                    LessonRow(lesson = lesson, onClick = { onOpenLesson(lesson.id) })
                }
            }
        }
    }
}

@Composable
private fun LessonRow(lesson: LessonRef, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(lesson.paragraphLabel, style = MaterialTheme.typography.bodyLarge)
        if (lesson.excerpt.isNotBlank()) {
            Text(
                text = lesson.excerpt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

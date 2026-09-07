package com.samuelkrissi.orot.ui.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelkrissi.orot.catalog.Book
import com.samuelkrissi.orot.catalog.Chapter
import com.samuelkrissi.orot.catalog.groupIntoChapters
import com.samuelkrissi.orot.data.repo.OrotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val chapters: List<Chapter> = emptyList(),
    val query: String = "",
    val expanded: Set<String> = emptySet(),
) {
    val visibleChapters: List<Chapter>
        get() {
            val q = query.trim()
            if (q.isEmpty()) return chapters
            return chapters.mapNotNull { chapter ->
                val lessons = chapter.lessons.filter {
                    it.title.contains(q, ignoreCase = true) ||
                        it.excerpt.contains(q, ignoreCase = true)
                }
                when {
                    lessons.isNotEmpty() -> chapter.copy(lessons = lessons)
                    chapter.title.contains(q, ignoreCase = true) -> chapter
                    else -> null
                }
            }
        }
}

class BookViewModel(
    private val repository: OrotRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BookUiState())
    val state: StateFlow<BookUiState> = _state
    private var loadedBookId: String? = null

    fun load(book: Book, force: Boolean = false) {
        if (!force && loadedBookId == book.id && _state.value.chapters.isNotEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.loadBookToc(book, forceRefresh = force) }
                .onSuccess { lessons ->
                    val chapters = groupIntoChapters(lessons)
                    loadedBookId = book.id
                    _state.update {
                        it.copy(
                            loading = false,
                            chapters = chapters,
                            expanded = chapters.take(1).map { chapter -> chapter.code }.toSet(),
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = error.message ?: "לא ניתן לטעון את תוכן הספר",
                        )
                    }
                }
        }
    }

    fun setQuery(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun toggleChapter(code: String) {
        _state.update { current ->
            val next = current.expanded.toMutableSet()
            if (!next.add(code)) next.remove(code)
            current.copy(expanded = next)
        }
    }
}

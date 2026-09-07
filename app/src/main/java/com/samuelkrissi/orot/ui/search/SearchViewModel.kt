package com.samuelkrissi.orot.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelkrissi.orot.catalog.Book
import com.samuelkrissi.orot.catalog.Books
import com.samuelkrissi.orot.catalog.LessonRef
import com.samuelkrissi.orot.catalog.matchesSlug
import com.samuelkrissi.orot.data.repo.OrotRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchHit(
    val book: Book?,
    val lesson: LessonRef,
)

data class SearchUiState(
    val query: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val results: List<SearchHit> = emptyList(),
)

class SearchViewModel(
    private val repository: OrotRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state
    private var job: Job? = null

    fun onQuery(query: String, book: Book?) {
        _state.update { it.copy(query = query) }
        job?.cancel()
        if (query.trim().length < 2) {
            _state.update { it.copy(results = emptyList(), loading = false, error = null) }
            return
        }
        job = viewModelScope.launch {
            delay(350)
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.search(query, book) }
                .onSuccess { lessons ->
                    _state.update {
                        it.copy(
                            loading = false,
                            results = lessons.map { lesson ->
                                SearchHit(
                                    book = book ?: Books.all.firstOrNull { candidate ->
                                        candidate.matchesSlug(lesson.slug)
                                    },
                                    lesson = lesson,
                                )
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(loading = false, error = error.message ?: "החיפוש נכשל")
                    }
                }
        }
    }
}

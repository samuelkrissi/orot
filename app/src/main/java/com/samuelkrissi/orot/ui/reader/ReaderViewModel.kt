package com.samuelkrissi.orot.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelkrissi.orot.catalog.Books
import com.samuelkrissi.orot.catalog.LessonRef
import com.samuelkrissi.orot.data.prefs.ReaderPrefs
import com.samuelkrissi.orot.data.prefs.ReaderSettings
import com.samuelkrissi.orot.data.repo.LessonContent
import com.samuelkrissi.orot.data.repo.OrotRepository
import com.samuelkrissi.orot.html.wrapReaderDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReaderUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val lesson: LessonContent? = null,
    val previous: LessonRef? = null,
    val next: LessonRef? = null,
)

class ReaderViewModel(
    private val repository: OrotRepository,
    private val prefs: ReaderPrefs,
) : ViewModel() {
    val settings: StateFlow<ReaderSettings> = prefs.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ReaderSettings(),
    )

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state

    fun load(bookId: String, lessonId: Long) {
        val book = Books.byId(bookId)
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val lesson = repository.loadLesson(lessonId)
                val toc = book?.let { repository.loadBookToc(it) }.orEmpty()
                val index = toc.indexOfFirst { it.id == lessonId }
                val previous = toc.getOrNull(index - 1)
                val next = toc.getOrNull(index + 1)
                Triple(lesson, previous, next)
            }.onSuccess { (lesson, previous, next) ->
                _state.update {
                    it.copy(loading = false, lesson = lesson, previous = previous, next = next)
                }
                prefs.rememberLesson(bookId, lesson.ref.id, lesson.ref.title)
            }.onFailure { error ->
                _state.update {
                    it.copy(loading = false, error = error.message ?: "לא ניתן לטעון את הפסקה")
                }
            }
        }
    }

    fun growFont() {
        viewModelScope.launch { prefs.setFontSize((settings.value.fontSize + 2f)) }
    }

    fun shrinkFont() {
        viewModelScope.launch { prefs.setFontSize((settings.value.fontSize - 2f)) }
    }

    fun document(dark: Boolean, fontSize: Float): String? {
        val lesson = _state.value.lesson ?: return null
        return wrapReaderDocument(
            title = lesson.ref.title,
            bodyHtml = lesson.html,
            fontSizePx = fontSize.toInt().coerceIn(14, 34),
            dark = dark,
        )
    }
}

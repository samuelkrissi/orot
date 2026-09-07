package com.samuelkrissi.orot.ui.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelkrissi.orot.data.prefs.ReaderPrefs
import com.samuelkrissi.orot.data.prefs.ReaderSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BooksViewModel(
    prefs: ReaderPrefs,
) : ViewModel() {
    val settings: StateFlow<ReaderSettings> = prefs.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ReaderSettings(),
    )
}

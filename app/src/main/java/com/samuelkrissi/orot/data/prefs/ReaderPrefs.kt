package com.samuelkrissi.orot.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "orot_reader")

data class ReaderSettings(
    val fontSize: Float = 20f,
    val lastBookId: String? = null,
    val lastLessonId: Long? = null,
    val lastLessonTitle: String? = null,
)

class ReaderPrefs(private val context: Context) {
    private val fontSizeKey = floatPreferencesKey("font_size")
    private val lastBookKey = stringPreferencesKey("last_book")
    private val lastLessonKey = longPreferencesKey("last_lesson")
    private val lastTitleKey = stringPreferencesKey("last_title")

    val settings: Flow<ReaderSettings> = context.dataStore.data.map { prefs ->
        ReaderSettings(
            fontSize = prefs[fontSizeKey] ?: 20f,
            lastBookId = prefs[lastBookKey],
            lastLessonId = prefs[lastLessonKey],
            lastLessonTitle = prefs[lastTitleKey],
        )
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { it[fontSizeKey] = size.coerceIn(14f, 34f) }
    }

    suspend fun rememberLesson(bookId: String, lessonId: Long, title: String) {
        context.dataStore.edit { prefs ->
            prefs[lastBookKey] = bookId
            prefs[lastLessonKey] = lessonId
            prefs[lastTitleKey] = title
        }
    }
}

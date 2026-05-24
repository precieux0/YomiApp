package com.yomi.mangaflow.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "yomi_settings")

class SettingsDataStore(private val context: Context) {

    private val THEME_KEY = stringPreferencesKey("theme_preference")
    private val READING_MODE_KEY = stringPreferencesKey("reading_mode")
    private val SYNC_ENABLED_KEY = booleanPreferencesKey("sync_enabled")
    private val CURRENT_SOURCE_KEY = stringPreferencesKey("current_source")
    private val FILTER_LANG_KEY = stringPreferencesKey("filter_lang")
    private val FILTER_GENRE_KEY = stringPreferencesKey("filter_genre")
    private val FILTER_STATUS_KEY = stringPreferencesKey("filter_status")
    private val SORT_ORDER_KEY = stringPreferencesKey("sort_order")

    fun getProgressKey(mangaId: String, chapterId: String) = intPreferencesKey("progress_${mangaId}_${chapterId}")

    val themePreference: Flow<ThemePreference> = context.dataStore.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            "light" -> ThemePreference.LIGHT
            "dark" -> ThemePreference.DARK
            "sepia" -> ThemePreference.SEPIA
            else -> ThemePreference.SYSTEM
        }
    }

    val readingMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[READING_MODE_KEY] ?: "standard"
    }

    val syncEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SYNC_ENABLED_KEY] ?: false
    }

    val currentSource: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_SOURCE_KEY] ?: "MANGADEX"
    }

    val filterLang: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[FILTER_LANG_KEY] ?: "all"
    }

    val filterGenre: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[FILTER_GENRE_KEY] ?: "all"
    }

    val filterStatus: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[FILTER_STATUS_KEY] ?: "all"
    }

    val sortOrder: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SORT_ORDER_KEY] ?: "popularity"
    }

    suspend fun setTheme(theme: ThemePreference) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = when (theme) {
                ThemePreference.LIGHT -> "light"
                ThemePreference.DARK -> "dark"
                ThemePreference.SEPIA -> "sepia"
                ThemePreference.SYSTEM -> "system"
            }
        }
    }

    suspend fun setReadingMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[READING_MODE_KEY] = mode
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SYNC_ENABLED_KEY] = enabled
        }
    }

    suspend fun setCurrentSource(sourceId: String) {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_SOURCE_KEY] = sourceId
        }
    }

    suspend fun setFilterLang(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[FILTER_LANG_KEY] = lang
        }
    }

    suspend fun setFilterGenre(genre: String) {
        context.dataStore.edit { prefs ->
            prefs[FILTER_GENRE_KEY] = genre
        }
    }

    suspend fun setFilterStatus(status: String) {
        context.dataStore.edit { prefs ->
            prefs[FILTER_STATUS_KEY] = status
        }
    }

    suspend fun setSortOrder(order: String) {
        context.dataStore.edit { prefs ->
            prefs[SORT_ORDER_KEY] = order
        }
    }

    suspend fun saveProgress(mangaId: String, chapterId: String, pageIndex: Int) {
        context.dataStore.edit { prefs ->
            prefs[getProgressKey(mangaId, chapterId)] = pageIndex
        }
    }

    suspend fun getProgress(mangaId: String, chapterId: String): Int {
        return context.dataStore.data.map { prefs ->
            prefs[getProgressKey(mangaId, chapterId)] ?: 0
        }.first()
    }
}

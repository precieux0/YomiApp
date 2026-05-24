package com.yomi.mangaflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yomi.mangaflow.data.local.SettingsDataStore
import com.yomi.mangaflow.data.local.ThemePreference
import com.yomi.mangaflow.data.model.Chapter
import com.yomi.mangaflow.data.model.Manga
import com.yomi.mangaflow.data.model.MangaDetail
import com.yomi.mangaflow.data.model.ReadingMode
import com.yomi.mangaflow.data.model.Tag
import com.yomi.mangaflow.data.repository.MangaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

sealed class SortOrder(val value: String) {
    data object Popularity : SortOrder("popularity")
    data object Title : SortOrder("title")
    data object Latest : SortOrder("latest")
    data object Rating : SortOrder("rating")
}

class MainViewModel(
    private val repository: MangaRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _themePreference = MutableStateFlow(ThemePreference.SYSTEM)
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()

    private val _currentSource = MutableStateFlow("MANGADEX")
    val currentSource: StateFlow<String> = _currentSource.asStateFlow()

    private val _availableSources = MutableStateFlow<List<String>>(emptyList())
    val availableSources: StateFlow<List<String>> = _availableSources.asStateFlow()

    private val _availableTags = MutableStateFlow<List<Tag>>(emptyList())
    val availableTags: StateFlow<List<Tag>> = _availableTags.asStateFlow()

    private val _filterLang = MutableStateFlow("all")
    val filterLang: StateFlow<String> = _filterLang.asStateFlow()

    private val _filterGenre = MutableStateFlow("all")
    val filterGenre: StateFlow<String> = _filterGenre.asStateFlow()

    private val _filterStatus = MutableStateFlow("all")
    val filterStatus: StateFlow<String> = _filterStatus.asStateFlow()

    private val _sortOrder = MutableStateFlow<SortOrder>(SortOrder.Popularity)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _rawPopular = MutableStateFlow<List<Manga>>(emptyList())
    private val _rawLatest = MutableStateFlow<List<Manga>>(emptyList())

    private val _popularManga = MutableStateFlow<UiState<List<Manga>>>(UiState.Loading)
    val popularManga: StateFlow<UiState<List<Manga>>> = _popularManga.asStateFlow()

    private val _latestManga = MutableStateFlow<UiState<List<Manga>>>(UiState.Loading)
    val latestManga: StateFlow<UiState<List<Manga>>> = _latestManga.asStateFlow()

    private val _mangaDetail = MutableStateFlow<UiState<MangaDetail>>(UiState.Loading)
    val mangaDetail: StateFlow<UiState<MangaDetail>> = _mangaDetail.asStateFlow()

    private val _selectedManga = MutableStateFlow<Manga?>(null)
    val selectedManga: StateFlow<Manga?> = _selectedManga.asStateFlow()

    private val _readerMode = MutableStateFlow(ReadingMode.STANDARD)
    val readerMode: StateFlow<ReadingMode> = _readerMode.asStateFlow()

    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    val currentChapter: StateFlow<Chapter?> = _currentChapter.asStateFlow()

    val favorites = repository.favorites
    val history = repository.history
    val downloads = repository.downloads

    init {
        viewModelScope.launch {
            _themePreference.value = settingsDataStore.themePreference.first()
            _currentSource.value = settingsDataStore.currentSource.first()
            _filterLang.value = settingsDataStore.filterLang.first()
            _filterGenre.value = settingsDataStore.filterGenre.first()
            _filterStatus.value = settingsDataStore.filterStatus.first()
            _sortOrder.value = when (settingsDataStore.sortOrder.first()) {
                "title" -> SortOrder.Title
                "latest" -> SortOrder.Latest
                "rating" -> SortOrder.Rating
                else -> SortOrder.Popularity
            }
            val savedMode = settingsDataStore.readingMode.first()
            _readerMode.value = when (savedMode) {
                "webtoon" -> ReadingMode.WEBTOON
                "dual" -> ReadingMode.DUAL_PAGE
                else -> ReadingMode.STANDARD
            }

            val sources = repository.getAllSources()
            _availableSources.value = sources.map { it.name }
            val tags = repository.getAllTags()
            _availableTags.value = tags
        }
        loadHomeData()

        viewModelScope.launch {
            combine(_filterLang, _filterGenre, _filterStatus, _sortOrder) { _, _, _, _ -> Unit }
                .collect { applyFiltersAndSort() }
        }
    }

    fun setCurrentSource(sourceId: String) {
        if (_currentSource.value == sourceId) return
        viewModelScope.launch {
            settingsDataStore.setCurrentSource(sourceId)
            _currentSource.value = sourceId
            loadHomeData()
        }
    }

    fun setFilterLang(lang: String) {
        viewModelScope.launch {
            settingsDataStore.setFilterLang(lang)
            _filterLang.value = lang
        }
    }

    fun setFilterGenre(genreId: String) {
        viewModelScope.launch {
            settingsDataStore.setFilterGenre(genreId)
            _filterGenre.value = genreId
        }
    }

    fun setFilterStatus(status: String) {
        viewModelScope.launch {
            settingsDataStore.setFilterStatus(status)
            _filterStatus.value = status
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            settingsDataStore.setSortOrder(order.value)
            _sortOrder.value = order
        }
    }

    fun resetFilters() {
        setFilterLang("all")
        setFilterGenre("all")
        setFilterStatus("all")
        setSortOrder(SortOrder.Popularity)
    }

    private fun applyFiltersAndSort() {
        _popularManga.value = UiState.Success(filterAndSortList(_rawPopular.value))
        _latestManga.value = UiState.Success(filterAndSortList(_rawLatest.value))
    }

    private fun filterAndSortList(list: List<Manga>): List<Manga> {
        var filtered = list
        if (_filterLang.value != "all") filtered = filtered.filter { it.lang == _filterLang.value }
        if (_filterGenre.value != "all") filtered = filtered.filter { it.genre.contains(_filterGenre.value) }
        if (_filterStatus.value != "all") filtered = filtered.filter { it.status.equals(_filterStatus.value, ignoreCase = true) }

        return when (_sortOrder.value) {
            SortOrder.Title -> filtered.sortedBy { it.title }
            SortOrder.Latest -> filtered.sortedByDescending { it.id }
            SortOrder.Rating -> filtered.sortedByDescending { it.rating }
            SortOrder.Popularity -> filtered.sortedByDescending { it.rating }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            val sourceId = _currentSource.value
            _popularManga.value = UiState.Loading
            _latestManga.value = UiState.Loading
            try {
                _rawPopular.value = repository.getPopularManga(sourceId)
                _rawLatest.value = repository.getLatestManga(sourceId)
                applyFiltersAndSort()
            } catch (e: Exception) {
                _popularManga.value = UiState.Error(e.message ?: "Erreur")
                _latestManga.value = UiState.Error(e.message ?: "Erreur")
            }
        }
    }

    fun loadMangaDetail(mangaUrl: String, sourceId: String) {
        viewModelScope.launch {
            _mangaDetail.value = UiState.Loading
            try {
                val detail = repository.getMangaDetail(mangaUrl, sourceId)
                _mangaDetail.value = UiState.Success(detail)
                _selectedManga.value = detail.manga
            } catch (e: Exception) {
                _mangaDetail.value = UiState.Error(e.message ?: "Erreur détail")
            }
        }
    }

    suspend fun getChapterPages(mangaId: String, chapter: Chapter): List<String> {
        val sourceId = _selectedManga.value?.sourceId ?: _currentSource.value
        return repository.getChapterPages(mangaId, chapter.url, sourceId)
    }

    suspend fun downloadChapter(manga: Manga, chapter: Chapter, onProgress: (Int, Int) -> Unit = { _, _ -> }) {
        repository.downloadChapter(manga, chapter, onProgress)
    }

    suspend fun deleteDownload(mangaId: String, chapterId: String) {
        repository.deleteDownload(mangaId, chapterId)
    }

    suspend fun saveProgress(mangaId: String, chapterId: String, pageIndex: Int) {
        settingsDataStore.saveProgress(mangaId, chapterId, pageIndex)
    }

    suspend fun getProgress(mangaId: String, chapterId: String): Int {
        return settingsDataStore.getProgress(mangaId, chapterId)
    }

    suspend fun searchManga(sourceId: String, query: String): List<Manga>? {
        return try {
            repository.searchManga(sourceId, query)
        } catch (e: Exception) {
            null
        }
    }

    fun setReaderMode(mode: ReadingMode) {
        viewModelScope.launch {
            val key = when (mode) {
                ReadingMode.WEBTOON -> "webtoon"
                ReadingMode.DUAL_PAGE -> "dual"
                else -> "standard"
            }
            settingsDataStore.setReadingMode(key)
            _readerMode.value = mode
        }
    }

    fun selectManga(manga: Manga) { _selectedManga.value = manga }
    fun selectChapter(chapter: Chapter) { _currentChapter.value = chapter }
    fun isFavorite(mangaId: String) = repository.isFavorite(mangaId)
    fun toggleFavorite(manga: Manga) { viewModelScope.launch { repository.toggleFavorite(manga) } }
    fun addToHistory(manga: Manga, chapter: Chapter) { viewModelScope.launch { repository.addToHistory(manga, chapter) } }
    fun addDownload(manga: Manga, chapter: Chapter) { viewModelScope.launch { repository.addDownload(manga, chapter) } }
    fun removeDownload(mangaId: String, chapterId: String) { viewModelScope.launch { repository.removeDownload(mangaId, chapterId) } }
    fun setTheme(theme: ThemePreference) { viewModelScope.launch { settingsDataStore.setTheme(theme); _themePreference.value = theme } }
    fun setSyncEnabled(enabled: Boolean) { viewModelScope.launch { settingsDataStore.setSyncEnabled(enabled) } }
}

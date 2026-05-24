package com.yomi.mangaflow.data.repository

import com.yomi.mangaflow.data.datasource.KotatsuDataSource
import com.yomi.mangaflow.data.model.Chapter
import com.yomi.mangaflow.data.model.Manga
import com.yomi.mangaflow.data.model.MangaDetail
import com.yomi.mangaflow.data.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class MangaRepository(
    private val kotatsuDataSource: KotatsuDataSource
) {

    private val _favorites = MutableStateFlow<List<Manga>>(emptyList())
    val favorites: StateFlow<List<Manga>> = _favorites.asStateFlow()

    private val _history = MutableStateFlow<List<Pair<Manga, Chapter>>>(emptyList())
    val history: StateFlow<List<Pair<Manga, Chapter>>> = _history.asStateFlow()

    private val _downloads = MutableStateFlow<List<Pair<Manga, Chapter>>>(emptyList())
    val downloads: StateFlow<List<Pair<Manga, Chapter>>> = _downloads.asStateFlow()

    suspend fun getAllSources() = kotatsuDataSource.getAllSources()
    suspend fun getAllTags(): List<Tag> = kotatsuDataSource.getAllTags()

    suspend fun getPopularManga(sourceId: String): List<Manga> = kotatsuDataSource.getPopularManga(sourceId)
    suspend fun getLatestManga(sourceId: String): List<Manga> = kotatsuDataSource.getLatestManga(sourceId)
    suspend fun getMangaDetail(mangaUrl: String, sourceId: String): MangaDetail = kotatsuDataSource.getMangaDetail(mangaUrl, sourceId)
    suspend fun getChapterPages(chapterUrl: String, sourceId: String): List<String> = kotatsuDataSource.getChapterPages(chapterUrl, sourceId)
    suspend fun searchManga(sourceId: String, query: String): List<Manga> = kotatsuDataSource.searchManga(sourceId, query)

    fun toggleFavorite(manga: Manga) {
        val current = _favorites.value.toMutableList()
        if (current.any { it.id == manga.id }) {
            _favorites.value = current.filter { it.id != manga.id }
        } else {
            _favorites.value = current + manga
        }
    }

    fun isFavorite(mangaId: String): Flow<Boolean> = _favorites.map { list -> list.any { it.id == mangaId } }

    fun addToHistory(manga: Manga, chapter: Chapter) {
        val current = _history.value.toMutableList()
        current.removeAll { it.first.id == manga.id && it.second.id == chapter.id }
        _history.value = listOf(manga to chapter) + current
    }

    fun addDownload(manga: Manga, chapter: Chapter) {
        val current = _downloads.value.toMutableList()
        if (current.none { it.first.id == manga.id && it.second.id == chapter.id }) {
            _downloads.value = current + (manga to chapter)
        }
    }

    fun removeDownload(mangaId: String, chapterId: String) {
        _downloads.value = _downloads.value.filterNot {
            it.first.id == mangaId && it.second.id == chapterId
        }
    }
}

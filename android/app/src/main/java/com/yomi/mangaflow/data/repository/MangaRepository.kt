package com.yomi.mangaflow.data.repository

import android.content.Context
import com.yomi.mangaflow.data.datasource.KotatsuDataSource
import com.yomi.mangaflow.data.model.Chapter
import com.yomi.mangaflow.data.model.Manga
import com.yomi.mangaflow.data.model.MangaDetail
import com.yomi.mangaflow.data.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class MangaRepository(
    private val kotatsuDataSource: KotatsuDataSource,
    private val context: Context
) {

    private val _favorites = MutableStateFlow<List<Manga>>(emptyList())
    val favorites: StateFlow<List<Manga>> = _favorites.asStateFlow()

    private val _history = MutableStateFlow<List<Pair<Manga, Chapter>>>(emptyList())
    val history: StateFlow<List<Pair<Manga, Chapter>>> = _history.asStateFlow()

    private val _downloads = MutableStateFlow<List<Pair<Manga, Chapter>>>(emptyList())
    val downloads: StateFlow<List<Pair<Manga, Chapter>>> = _downloads.asStateFlow()

    private val client = OkHttpClient()

    suspend fun getAllSources() = kotatsuDataSource.getAllSources()
    suspend fun getAllTags(): List<Tag> = kotatsuDataSource.getAllTags()

    suspend fun getPopularManga(sourceId: String): List<Manga> = kotatsuDataSource.getPopularManga(sourceId)
    suspend fun getLatestManga(sourceId: String): List<Manga> = kotatsuDataSource.getLatestManga(sourceId)
    suspend fun getMangaDetail(mangaUrl: String, sourceId: String): MangaDetail = kotatsuDataSource.getMangaDetail(mangaUrl, sourceId)

    suspend fun getChapterPages(mangaId: String, chapterUrl: String, sourceId: String): List<String> {
        // Vérifier si déjà téléchargé
        val downloadDir = File(context.filesDir, "downloads/$mangaId/${chapterUrl.hashCode()}")
        if (downloadDir.exists() && downloadDir.listFiles()?.isNotEmpty() == true) {
            return downloadDir.listFiles()?.sortedBy { it.name }?.map { it.absolutePath } ?: emptyList()
        }
        // Sinon récupérer depuis le parser
        return kotatsuDataSource.getChapterPages(chapterUrl, sourceId)
    }

    suspend fun downloadChapter(manga: Manga, chapter: Chapter, onProgress: (Int, Int) -> Unit = { _, _ -> }) {
        val mangaId = manga.id
        val chapterUrl = chapter.url
        val sourceId = manga.sourceId
        val pages = kotatsuDataSource.getChapterPages(chapterUrl, sourceId)
        val downloadDir = File(context.filesDir, "downloads/$mangaId/${chapterUrl.hashCode()}")
        downloadDir.mkdirs()

        var index = 0
        for (pageUrl in pages) {
            val fileName = pageUrl.substringAfterLast("/").substringBefore("?")
            val file = File(downloadDir, "$index-${fileName}.jpg")
            if (!file.exists()) {
                val request = Request.Builder().url(pageUrl).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }
            index++
            onProgress(index, pages.size)
        }
        // Ajouter aux téléchargements
        val current = _downloads.value.toMutableList()
        if (current.none { it.first.id == mangaId && it.second.id == chapter.id }) {
            _downloads.value = current + (manga to chapter)
        }
    }

    suspend fun deleteDownload(mangaId: String, chapterId: String) {
        val downloadDir = File(context.filesDir, "downloads/$mangaId/${chapterId.hashCode()}")
        downloadDir.deleteRecursively()
        _downloads.value = _downloads.value.filterNot { it.first.id == mangaId && it.second.id == chapterId }
    }

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
        _downloads.value = _downloads.value.filterNot { it.first.id == mangaId && it.second.id == chapterId }
    }
}

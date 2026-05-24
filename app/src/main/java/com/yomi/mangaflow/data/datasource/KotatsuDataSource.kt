package com.yomi.mangaflow.data.datasource

import com.yomi.mangaflow.data.model.Chapter
import com.yomi.mangaflow.data.model.Manga
import com.yomi.mangaflow.data.model.MangaDetail
import com.yomi.mangaflow.data.model.Tag
import kotlinx.coroutines.flow.first
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.model.Manga as KotatsuManga
import org.koitharu.kotatsu.parsers.model.MangaChapter as KotatsuChapter
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.model.MangaStatus
import org.koitharu.kotatsu.parsers.model.MangaTag

class KotatsuDataSource(
    private val loaderContext: MangaLoaderContext
) {

    fun getAllSources(): List<MangaSource> = MangaSource.entries.toList()

    suspend fun getAllTags(): List<Tag> = loaderContext.getTags().map { it.toYomiTag() }

    private fun MangaTag.toYomiTag() = Tag(id = this.key, name = this.title, category = this.category.name)

    private suspend fun getParser(sourceId: String): org.koitharu.kotatsu.parsers.MangaParser {
        val enumSource = MangaSource.valueOf(sourceId)
        return loaderContext.newParserInstance(enumSource)
    }

    suspend fun getPopularManga(sourceId: String): List<Manga> {
        val result = getParser(sourceId).getPopular().first()
        return result.map { it.toYomiManga(sourceId) }
    }

    suspend fun getLatestManga(sourceId: String): List<Manga> {
        val result = getParser(sourceId).getLatest().first()
        return result.map { it.toYomiManga(sourceId) }
    }

    suspend fun getMangaDetail(mangaUrl: String, sourceId: String): MangaDetail {
        val parser = getParser(sourceId)
        val details = parser.getDetails(mangaUrl).first()
        return MangaDetail(
            manga = details.manga.toYomiManga(sourceId),
            chapters = details.chapters.map { it.toYomiChapter() }
        )
    }

    suspend fun getChapterPages(chapterUrl: String, sourceId: String): List<String> {
        val parser = getParser(sourceId)
        val pages = parser.getPages(chapterUrl).first()
        return pages.map { it.url }
    }

    suspend fun searchManga(sourceId: String, query: String): List<Manga> {
        val result = getParser(sourceId).search(query).first()
        return result.map { it.toYomiManga(sourceId) }
    }

    private fun KotatsuManga.toYomiManga(sourceId: String) = Manga(
        id = this.url,
        title = this.title,
        coverUrl = this.coverUrl ?: "",
        description = this.description ?: "",
        author = this.author ?: "",
        status = when (this.status) {
            MangaStatus.ONGOING -> "Ongoing"
            MangaStatus.FINISHED -> "Completed"
            MangaStatus.PAUSED -> "Hiatus"
            MangaStatus.DROPPED -> "Cancelled"
            else -> "Unknown"
        },
        genre = this.tags.map { it.title },
        rating = this.rating?.toFloat() ?: 0f,
        source = this.source.name,
        sourceId = sourceId,
        lang = this.source.lang ?: "en",
        url = this.url
    )

    private fun KotatsuChapter.toYomiChapter() = Chapter(
        id = this.url,
        title = this.name,
        number = this.number,
        url = this.url,
        dateUpload = this.uploadDate?.time ?: 0L,
        read = false,
        bookmark = false,
        lastPageRead = 0,
        pages = emptyList()
    )
}

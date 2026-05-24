package com.yomi.mangaflow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Manga(
    val id: String,
    val title: String,
    val coverUrl: String,
    val description: String = "",
    val author: String = "",
    val status: String = "Ongoing",
    val genre: List<String> = emptyList(),
    val rating: Float = 0f,
    val source: String = "",
    val sourceId: String = "MANGADEX",  // Nouveau champ : identifiant du parser Kotatsu
    val lang: String = "en",
    val url: String = ""
)

@Serializable
data class Chapter(
    val id: String,
    val title: String,
    val number: Float,
    val url: String = "",
    val dateUpload: Long = 0L,
    val read: Boolean = false,
    val bookmark: Boolean = false,
    val lastPageRead: Int = 0,
    val pages: List<String> = emptyList()
)

@Serializable
data class MangaDetail(
    val manga: Manga,
    val chapters: List<Chapter> = emptyList()
)

@Serializable
data class Source(
    val id: String,
    val name: String,
    val lang: String = "en",
    val iconUrl: String = "",
    val isEnabled: Boolean = true,
    val supportsLatest: Boolean = true,
    val supportsSearch: Boolean = true,
    val isCustom: Boolean = false
)

enum class MangaStatus(val label: String) {
    ONGOING("En cours"),
    COMPLETED("Terminé"),
    HIATUS("En pause"),
    CANCELLED("Annulé"),
    UNKNOWN("Inconnu")
}

enum class ReadingMode {
    STANDARD,
    WEBTOON,
    DUAL_PAGE
}

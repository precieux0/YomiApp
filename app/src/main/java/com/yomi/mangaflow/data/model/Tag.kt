package com.yomi.mangaflow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: String,
    val name: String,
    val category: String
)

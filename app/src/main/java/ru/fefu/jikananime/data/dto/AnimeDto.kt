package ru.fefu.jikananime.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnimeSearchResponse(
    val data: List<AnimeDto>
)

@Serializable
data class AnimeDetailResponse(
    val data: AnimeDto
)

@Serializable
data class AnimeDto(
    val mal_id: Int,
    val title: String,
    val images: Images,
    val synopsis: String?,
    val score: Double?,
    val episodes: Int?,
    val status: String?,
    val year: Int?,
    val genres: List<Genre>?,
    val rating: String?,
    val duration: String?
)

@Serializable
data class Images(
    val jpg: JpgImage
)

@Serializable
data class JpgImage(
    val image_url: String
)

@Serializable
data class Genre(
    val name: String
)
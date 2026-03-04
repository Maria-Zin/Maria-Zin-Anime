package ru.fefu.jikananime.domain.model

data class Anime(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val synopsis: String,
    val score: Double,
    val episodes: Int?,
    val status: String?,
    val year: Int?,
    val genres: List<String> = emptyList(),
    val rating: String?,
    val duration: String?
) {
    val scoreFormatted: String
        get() = if (score > 0) String.format("%.2f", score) else "N/A"

    val yearString: String
        get() = year?.toString() ?: "Unknown"

    val episodesString: String
        get() = episodes?.toString() ?: "?"
}
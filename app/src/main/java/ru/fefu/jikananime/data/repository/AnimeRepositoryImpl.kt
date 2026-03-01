package ru.fefu.jikananime.data.repository

import ru.fefu.jikananime.data.api.JikanApi
import ru.fefu.jikananime.data.dto.AnimeDto
import ru.fefu.jikananime.domain.model.Anime
import ru.fefu.jikananime.domain.repository.AnimeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimeRepositoryImpl @Inject constructor(
    private val api: JikanApi
) : AnimeRepository {

    private var lastSearchResults: List<Anime>? = null
    private var lastSearchQuery: String? = null

    override suspend fun searchAnime(query: String): List<Anime> {
        if (query.isBlank()) {
            lastSearchResults = emptyList()
            lastSearchQuery = query
            return emptyList()
        }

        val response = api.searchAnime(query)
        val results = response.data.map { it.toDomain() }
        lastSearchResults = results
        lastSearchQuery = query
        return results
    }

    override suspend fun getAnimeDetail(id: Int): Anime {
        val response = api.getAnimeDetail(id)
        return response.data.toDomain()
    }

    override fun getCachedSearchResults(): List<Anime>? {
        return lastSearchResults
    }

    private fun AnimeDto.toDomain(): Anime {
        return Anime(
            id = mal_id,
            title = title,
            imageUrl = images.jpg.image_url,
            synopsis = synopsis ?: "No description available",
            score = score ?: 0.0,
            episodes = episodes,
            status = status ?: "Unknown",
            year = year,
            genres = genres?.map { it.name } ?: emptyList(),
            rating = rating ?: "Not rated",
            duration = duration ?: "Unknown"
        )
    }
}
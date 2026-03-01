package ru.fefu.jikananime.domain.repository

import ru.fefu.jikananime.domain.model.Anime

interface AnimeRepository {
    suspend fun searchAnime(query: String): List<Anime>
    suspend fun getAnimeDetail(id: Int): Anime
    fun getCachedSearchResults(): List<Anime>?
}
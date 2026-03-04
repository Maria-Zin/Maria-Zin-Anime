package ru.fefu.jikananime.data.api

import ru.fefu.jikananime.data.dto.AnimeDetailResponse
import ru.fefu.jikananime.data.dto.AnimeSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApi {

    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("page") page: Int = 1
    ): AnimeSearchResponse

    @GET("anime/{id}")
    suspend fun getAnimeDetail(
        @Path("id") id: Int
    ): AnimeDetailResponse
}
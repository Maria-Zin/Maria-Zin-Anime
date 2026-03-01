package ru.fefu.jikananime.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouritesManager @Inject constructor() {

    private val _favourites = MutableStateFlow<Set<Int>>(emptySet())
    val favourites: StateFlow<Set<Int>> = _favourites.asStateFlow()

    fun getFavourites(): Set<Int> = _favourites.value

    fun toggleFavourite(animeId: Int): Boolean {
        val current = _favourites.value
        val newSet = if (current.contains(animeId)) {
            current - animeId
        } else {
            current + animeId
        }
        _favourites.value = newSet
        return newSet.contains(animeId)
    }

    fun isFavourite(animeId: Int): Boolean = _favourites.value.contains(animeId)
}
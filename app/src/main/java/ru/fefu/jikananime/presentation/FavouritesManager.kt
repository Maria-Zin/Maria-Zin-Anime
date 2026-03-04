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

    fun getFavourites(): Set<Int> {
        println(" getFavourites() = ${_favourites.value}")
        return _favourites.value
    }

    fun toggleFavourite(animeId: Int): Boolean {
        val current = _favourites.value
        println("toggleFavourite($animeId)")
        println("   Текущие (${current.size}): $current")

        val newSet = if (current.contains(animeId)) {
            println("   Удаляем $animeId")
            current - animeId
        } else {
            println("   Добавляем $animeId")
            current + animeId
        }

        println("   Новые (${newSet.size}): $newSet")
        _favourites.value = newSet
        return newSet.contains(animeId)
    }

    fun isFavourite(animeId: Int): Boolean = _favourites.value.contains(animeId)
}
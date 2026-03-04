package ru.fefu.jikananime.presentation.screens.favourites

import ru.fefu.jikananime.domain.model.Anime

data class FavouritesUiState(
    val favourites: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class FavouritesEvent {
    object OnRefresh : FavouritesEvent()
    data class OnRemoveFromFavourites(val animeId: Int) : FavouritesEvent()
    data class OnSortChange(val sortType: FavouritesSortType) : FavouritesEvent()
}

enum class FavouritesSortType {
    BY_TITLE,
    BY_SCORE,
    BY_YEAR,
    BY_ADDED_DATE
}
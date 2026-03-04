package ru.fefu.jikananime.presentation.screens.search

import ru.fefu.jikananime.domain.model.Anime

data class SearchUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val animeList: List<Anime> = emptyList(),
    val favourites: Set<Int> = setOf(),
    val isRefreshing: Boolean = false
) {
    val isEmpty: Boolean = !isLoading && animeList.isEmpty() && errorMessage == null
}

sealed class SearchEvent {
    data class OnSearchQueryChange(val query: String) : SearchEvent()
    object OnSearch : SearchEvent()
    object OnRetry : SearchEvent()
    object OnRefresh : SearchEvent()
    data class OnToggleFavourite(val animeId: Int) : SearchEvent()
    object OnNavigateToFavourites : SearchEvent()
}
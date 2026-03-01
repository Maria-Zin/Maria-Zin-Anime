package ru.fefu.jikananime.presentation.screens.favourites

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import ru.fefu.jikananime.domain.model.Anime
import ru.fefu.jikananime.domain.repository.AnimeRepository
import ru.fefu.jikananime.presentation.FavouritesManager
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val savedStateHandle: SavedStateHandle,
    private val favouritesManager: FavouritesManager
) : ViewModel() {

    var state = mutableStateOf(FavouritesUiState())
        private set

    private var _favouriteAnime: MutableList<Anime> = mutableListOf()

    private var currentSortType: FavouritesSortType =
        savedStateHandle.get<FavouritesSortType>("sortType") ?: FavouritesSortType.BY_TITLE

    init {
        savedStateHandle["sortType"] = currentSortType

        viewModelScope.launch {
            favouritesManager.favourites.collectLatest { favourites ->
                loadFavourites()
            }
        }
    }

    fun onEvent(event: FavouritesEvent) {
        when (event) {
            is FavouritesEvent.OnRefresh -> loadFavourites()
            is FavouritesEvent.OnRemoveFromFavourites -> removeFromFavourites(event.animeId)
            is FavouritesEvent.OnSortChange -> {
                currentSortType = event.sortType
                savedStateHandle["sortType"] = currentSortType
                sortFavourites()
            }
        }
    }

    private fun loadFavourites() {
        val favouriteIds = favouritesManager.getFavourites()

        if (favouriteIds.isEmpty()) {
            state.value = state.value.copy(
                favourites = emptyList(),
                isLoading = false
            )
            return
        }

        state.value = state.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val loadedFavourites = mutableListOf<Anime>()
                favouriteIds.forEach { id ->
                    try {
                        val anime = repository.getAnimeDetail(id)
                        loadedFavourites.add(anime)
                    } catch (e: Exception) {
                    }
                    delay(100)
                }

                _favouriteAnime.clear()
                _favouriteAnime.addAll(loadedFavourites)

                sortFavourites()
                state.value = state.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                state.value = state.value.copy(
                    isLoading = false,
                    errorMessage = "Ошибка соединения с сервером"
                )
            }
        }
    }

    private fun removeFromFavourites(animeId: Int) {
        favouritesManager.toggleFavourite(animeId)
    }

    private fun sortFavourites() {
        val sorted = when (currentSortType) {
            FavouritesSortType.BY_TITLE -> _favouriteAnime.sortedBy { it.title }
            FavouritesSortType.BY_SCORE -> _favouriteAnime.sortedByDescending { it.score }
            FavouritesSortType.BY_YEAR -> _favouriteAnime.sortedByDescending { it.year ?: 0 }
            FavouritesSortType.BY_ADDED_DATE -> _favouriteAnime
        }

        state.value = state.value.copy(
            favourites = sorted,
            isLoading = false
        )
    }
}
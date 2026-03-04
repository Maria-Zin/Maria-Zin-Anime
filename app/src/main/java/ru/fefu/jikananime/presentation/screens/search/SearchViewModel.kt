package ru.fefu.jikananime.presentation.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import ru.fefu.jikananime.domain.repository.AnimeRepository
import ru.fefu.jikananime.presentation.FavouritesManager
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val savedStateHandle: SavedStateHandle,
    private val favouritesManager: FavouritesManager
) : ViewModel() {

    var state = mutableStateOf(SearchUiState())
        private set

    init {
        val savedQuery = savedStateHandle.get<String>("searchQuery") ?: ""
        state.value = state.value.copy(
            favourites = favouritesManager.getFavourites(),
            searchQuery = savedQuery
        )

        viewModelScope.launch {
            favouritesManager.favourites.collectLatest { favourites ->
                state.value = state.value.copy(
                    favourites = favourites
                )
            }
        }

        if (savedQuery.isNotBlank()) {
            performSearch(savedQuery)
        }
    }

    private var searchJob: Job? = null

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnSearchQueryChange -> {
                state.value = state.value.copy(searchQuery = event.query)
                savedStateHandle["searchQuery"] = event.query
                searchDebounced()
            }
            SearchEvent.OnSearch -> performSearch(state.value.searchQuery)
            SearchEvent.OnRetry -> performSearch(state.value.searchQuery)
            SearchEvent.OnRefresh -> performSearch(state.value.searchQuery, isRefreshing = true)
            is SearchEvent.OnToggleFavourite -> toggleFavourite(event.animeId)
            SearchEvent.OnNavigateToFavourites -> {}
        }
    }

    private fun searchDebounced() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            performSearch(state.value.searchQuery)
        }
    }

    private fun performSearch(query: String, isRefreshing: Boolean = false) {
        if (query.isBlank()) {
            state.value = state.value.copy(
                isLoading = false,
                errorMessage = null,
                isRefreshing = false,
                animeList = emptyList()
            )
            return
        }

        val currentList = state.value.animeList

        state.value = state.value.copy(
            isLoading = !isRefreshing,
            errorMessage = null,
            isRefreshing = isRefreshing
        )

        viewModelScope.launch {
            try {
                val results = repository.searchAnime(query)
                state.value = state.value.copy(
                    animeList = results,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null,
                    favourites = favouritesManager.getFavourites()
                )
            } catch (e: Exception) {
                state.value = state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "Ошибка соединения с сервером",
                    animeList = currentList
                )
            }
        }
    }

    private fun toggleFavourite(animeId: Int) {
        favouritesManager.toggleFavourite(animeId)
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
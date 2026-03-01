package ru.fefu.jikananime.presentation.screens.detail

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import ru.fefu.jikananime.domain.repository.AnimeRepository
import ru.fefu.jikananime.presentation.FavouritesManager
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val savedStateHandle: SavedStateHandle,
    private val favouritesManager: FavouritesManager
) : ViewModel() {

    var state = mutableStateOf(DetailUiState())
        private set

    private val animeId: Int = savedStateHandle.get<Int>("animeId") ?: 0

    init {
        if (animeId != 0) {
            viewModelScope.launch {
                favouritesManager.favourites.collectLatest { favourites ->
                    state.value = state.value.copy(
                        isFavourite = favourites.contains(animeId)
                    )
                }
            }

            loadAnimeDetail()
        } else {
            state.value = state.value.copy(errorMessage = "Invalid anime ID")
        }
    }

    fun onEvent(event: DetailEvent) {
        when (event) {
            is DetailEvent.OnRetry -> loadAnimeDetail()
            is DetailEvent.OnToggleFavourite -> toggleFavourite(event.animeId)
            is DetailEvent.OnShare -> {}
        }
    }

    private fun loadAnimeDetail() {
        state.value = state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val anime = repository.getAnimeDetail(animeId)
                state.value = state.value.copy(
                    isLoading = false,
                    anime = anime,
                    isFavourite = favouritesManager.isFavourite(animeId)
                )
            } catch (e: Exception) {
                state.value = state.value.copy(
                    isLoading = false,
                    errorMessage = "Ошибка соединения с сервером"
                )
            }
        }
    }

    private fun toggleFavourite(animeId: Int) {
        favouritesManager.toggleFavourite(animeId)
    }
}
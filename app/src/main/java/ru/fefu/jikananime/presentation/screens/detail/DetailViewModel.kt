package ru.fefu.jikananime.presentation.screens.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.fefu.jikananime.domain.repository.AnimeRepository
import ru.fefu.jikananime.presentation.FavouritesManager
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val favouritesManager: FavouritesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(DetailUiState())
        private set

    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])

    init {
        loadAnimeDetail()
        observeFavourites()
    }

    private fun observeFavourites() {
        viewModelScope.launch {
            favouritesManager.favourites.collectLatest { favourites ->
                state = state.copy(isFavourite = favourites.contains(animeId))
            }
        }
    }

    fun onEvent(event: DetailEvent) {
        when (event) {
            is DetailEvent.OnToggleFavourite -> favouritesManager.toggleFavourite(event.animeId)
            is DetailEvent.OnRetry -> loadAnimeDetail()
            is DetailEvent.OnShare -> { }
        }
    }

    private fun loadAnimeDetail() {
        state = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val anime = repository.getAnimeDetail(animeId)
                state = state.copy(anime = anime, isLoading = false)
            } catch (e: Exception) {
                val message = when (e) {
                    is IOException -> "Проблема с сетью. Включите VPN."
                    is HttpException -> "Ошибка сервера: ${e.code()}"
                    else -> "Непредвиденная ошибка"
                }
                state = state.copy(isLoading = false, errorMessage = message)
            }
        }
    }
}
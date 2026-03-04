package ru.fefu.jikananime.presentation.screens.favourites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.fefu.jikananime.domain.model.Anime
import ru.fefu.jikananime.domain.repository.AnimeRepository
import ru.fefu.jikananime.presentation.FavouritesManager
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val favouritesManager: FavouritesManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(FavouritesUiState())
        private set

    private var _favouriteAnime: MutableList<Anime> = mutableListOf()
    private var _favouriteAddedTime: MutableMap<Int, Long> = mutableMapOf()

    private var currentSortType: FavouritesSortType =
        savedStateHandle.get<FavouritesSortType>("sortType") ?: FavouritesSortType.BY_TITLE

    init {
        savedStateHandle["sortType"] = currentSortType

        viewModelScope.launch {
            favouritesManager.favourites.collectLatest {
                loadFavourites()
            }
        }
    }

    fun onEvent(event: FavouritesEvent) {
        when (event) {
            is FavouritesEvent.OnRefresh -> loadFavourites()
            is FavouritesEvent.OnRemoveFromFavourites -> {
                _favouriteAddedTime.remove(event.animeId)
                favouritesManager.toggleFavourite(event.animeId)
            }
            is FavouritesEvent.OnSortChange -> {
                currentSortType = event.sortType
                savedStateHandle["sortType"] = currentSortType
                sortFavourites()
            }
        }
    }

    private fun loadFavourites() {
        val ids = favouritesManager.getFavourites()
        println("Загрузка избранных ID: $ids")

        if (ids.isEmpty()) {
            state = state.copy(favourites = emptyList(), isLoading = false)
            return
        }

        state = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val loaded = mutableListOf<Anime>()
                val errors = mutableListOf<String>()

                ids.forEachIndexed { index, id ->
                    try {
                        println("Загрузка $index/${ids.size}: ID $id")

                        if (index > 0) {
                            delay(400)
                        }

                        val anime = repository.getAnimeDetail(id)
                        loaded.add(anime)

                        if (!_favouriteAddedTime.containsKey(id)) {
                            _favouriteAddedTime[id] = System.currentTimeMillis()
                        }

                        println("Загружено: ${anime.title}")

                    } catch (e: HttpException) {
                        if (e.code() == 429) {
                            errors.add("Слишком много запросов к API. Подождите немного.")
                            println("Rate limit для ID $id")
                            delay(2000)
                        } else {
                            errors.add("Ошибка загрузки ID $id: ${e.message}")
                            println("Ошибка HTTP ${e.code()} для ID $id")
                        }
                    } catch (e: Exception) {
                        errors.add("Ошибка загрузки ID $id")
                        println("Ошибка: ${e.message}")
                    }
                }

                _favouriteAnime.clear()
                _favouriteAnime.addAll(loaded)

                sortFavourites()

                state = state.copy(
                    isLoading = false,
                    errorMessage = if (errors.isNotEmpty() && loaded.isEmpty()) {
                        "Не удалось загрузить избранное. Превышен лимит запросов к API."
                    } else {
                        null
                    }
                )

            } catch (e: Exception) {
                println("Критическая ошибка: ${e.message}")
                state = state.copy(
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
            FavouritesSortType.BY_ADDED_DATE -> _favouriteAnime.sortedByDescending {
                _favouriteAddedTime[it.id] ?: 0
            }
        }

        state = state.copy(
            favourites = sorted,
            isLoading = false
        )
    }
}
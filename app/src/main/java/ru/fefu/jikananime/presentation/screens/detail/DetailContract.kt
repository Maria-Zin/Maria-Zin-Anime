package ru.fefu.jikananime.presentation.screens.detail

import ru.fefu.jikananime.domain.model.Anime

sealed class DetailEvent {
    object OnRetry : DetailEvent()
    data class OnToggleFavourite(val animeId: Int) : DetailEvent()
    object OnShare : DetailEvent()
}

data class DetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val anime: Anime? = null,
    val isFavourite: Boolean = false
)
package ru.fefu.jikananime.presentation.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.valentinilk.shimmer.shimmer
import ru.fefu.jikananime.domain.model.Anime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    onEvent: (SearchEvent) -> Unit,
    onAnimeClick: (Int) -> Unit,
    onNavigateToFavourites: () -> Unit
) {
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Jikan Anime")
                        Text(
                            text = "JIKAN-ANIME-MOD_A5_SKELETON",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToFavourites) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Favourites",
                            tint = if (state.favourites.isNotEmpty())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(SearchEvent.OnSearchQueryChange(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Search anime... (e.g., Naruto)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                },
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading && !state.isRefreshing && state.animeList.isEmpty()) {
                    SkeletonList()
                }

                if (state.animeList.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = state.animeList,
                            key = { it.id }
                        ) { anime ->
                            AnimeListItem(
                                anime = anime,
                                isFavourite = state.favourites.contains(anime.id),
                                onItemClick = { onAnimeClick(anime.id) },
                                onFavouriteClick = { onEvent(SearchEvent.OnToggleFavourite(anime.id)) }
                            )
                        }
                    }
                }

                if (state.errorMessage != null && !state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Button(
                                onClick = { onEvent(SearchEvent.OnRetry) },
                                modifier = Modifier.fillMaxWidth(0.6f)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                if (state.animeList.isEmpty() && state.errorMessage == null && !state.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No anime found. Try another search.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeListItem(
    anime: Anime,
    isFavourite: Boolean,
    onItemClick: () -> Unit,
    onFavouriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(anime.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Text(
                    text = "Score: ${anime.scoreFormatted}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Episodes: ${anime.episodes ?: "?"}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (anime.year != null) {
                    Text(
                        text = "Year: ${anime.year}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            IconButton(onClick = onFavouriteClick) {
                Icon(
                    imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                    tint = if (isFavourite)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun SkeletonList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(10) {
            SkeletonItem()
        }
    }
}

@Composable
fun SkeletonItem() {
    val shimmerModifier = Modifier.shimmer()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Spacer(
                modifier = shimmerModifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Spacer(
                    modifier = shimmerModifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Spacer(
                    modifier = shimmerModifier
                        .fillMaxWidth(0.3f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Spacer(
                    modifier = shimmerModifier
                        .fillMaxWidth(0.3f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Spacer(
                modifier = shimmerModifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}
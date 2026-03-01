package ru.fefu.jikananime.presentation.screens.favourites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sort
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
import androidx.compose.foundation.background


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    state: FavouritesUiState,
    onEvent: (FavouritesEvent) -> Unit,
    onAnimeClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favourites") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort"
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Title") },
                            onClick = {
                                onEvent(FavouritesEvent.OnSortChange(FavouritesSortType.BY_TITLE))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Score") },
                            onClick = {
                                onEvent(FavouritesEvent.OnSortChange(FavouritesSortType.BY_SCORE))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Year") },
                            onClick = {
                                onEvent(FavouritesEvent.OnSortChange(FavouritesSortType.BY_YEAR))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Added Date") },
                            onClick = {
                                onEvent(FavouritesEvent.OnSortChange(FavouritesSortType.BY_ADDED_DATE))
                                showSortMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    FavouritesSkeletonList()
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onEvent(FavouritesEvent.OnRefresh) }) {
                            Text("Retry")
                        }
                    }
                }

                state.favourites.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No favourites yet",
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Add anime to favourites from the search screen",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = state.favourites,
                            key = { it.id }
                        ) { anime ->
                            FavouriteItem(
                                anime = anime,
                                onItemClick = { onAnimeClick(anime.id) },
                                onRemoveClick = {
                                    onEvent(FavouritesEvent.OnRemoveFromFavourites(anime.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavouriteItem(
    anime: Anime,
    onItemClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(2.dp)
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
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2
                )
                Text(
                    text = "Score: ${anime.scoreFormatted}",
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun FavouritesSkeletonList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(5) {
            FavouritesSkeletonItem()
        }
    }
}

@Composable
fun FavouritesSkeletonItem() {
    val shimmerModifier = Modifier.shimmer()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Box(
                modifier = shimmerModifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = shimmerModifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = shimmerModifier
                        .fillMaxWidth(0.3f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Box(
                modifier = shimmerModifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}
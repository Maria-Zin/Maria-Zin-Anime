package ru.fefu.jikananime.presentation.screens.detail

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ru.fefu.jikananime.domain.model.Anime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailUiState,
    onEvent: (DetailEvent) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anime Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    state.anime?.let { anime ->
                        IconButton(onClick = { onEvent(DetailEvent.OnToggleFavourite(anime.id)) }) {
                            Icon(
                                imageVector = if (state.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favourite",
                                tint = if (state.isFavourite)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = {
                            onEvent(DetailEvent.OnShare)
                            shareAnime(context, anime)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onEvent(DetailEvent.OnRetry) }) {
                            Text("Retry")
                        }
                    }
                }

                state.anime != null -> {
                    DetailContent(
                        anime = state.anime,
                        isFavourite = state.isFavourite
                    )
                }
            }
        }
    }
}

@Composable
fun DetailContent(
    anime: Anime,
    isFavourite: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(anime.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        item {
            Text(
                text = anime.title,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRow("Score", anime.scoreFormatted)
                    InfoRow("Episodes", anime.episodesString)
                    InfoRow("Year", anime.yearString)
                    InfoRow("Status", anime.status ?: "Unknown")
                    InfoRow("Rating", anime.rating ?: "Not rated")
                    InfoRow("Duration", anime.duration ?: "Unknown")

                    if (anime.genres.isNotEmpty()) {
                        InfoRow("Genres", anime.genres.joinToString(", "))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Synopsis",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = anime.synopsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun shareAnime(context: Context, anime: Anime) {
    val shareText = """
        Check out this anime: ${anime.title}
        Score: ${anime.scoreFormatted}
        Episodes: ${anime.episodesString}
        Year: ${anime.yearString}
        Synopsis: ${anime.synopsis.take(100)}...
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Anime")
    context.startActivity(shareIntent)
}
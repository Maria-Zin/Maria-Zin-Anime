package ru.fefu.jikananime.presentation.screens.detail

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.valentinilk.shimmer.shimmer
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
                title = { Text(state.anime?.title ?: "Детали") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    state.anime?.let { anime ->
                        IconButton(onClick = { onEvent(DetailEvent.OnToggleFavourite(anime.id)) }) {
                            Icon(
                                imageVector = if (state.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (state.isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { shareAnime(context, anime) }) {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> DetailSkeleton()
                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.errorMessage, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { onEvent(DetailEvent.OnRetry) }) { Text("Повторить") }
                    }
                }
                state.anime != null -> DetailContent(state.anime)
            }
        }
    }
}

@Composable
fun DetailContent(anime: Anime) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(anime.imageUrl).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
            Text(anime.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Рейтинг: ${anime.scoreFormatted}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Text(anime.synopsis, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun DetailSkeleton() {
    val shimmerModifier = Modifier.shimmer()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(modifier = shimmerModifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray))
        Spacer(Modifier.height(16.dp))
        Box(modifier = shimmerModifier.fillMaxWidth(0.6f).height(30.dp).background(Color.LightGray))
        Spacer(Modifier.height(16.dp))
        repeat(5) {
            Box(modifier = shimmerModifier.fillMaxWidth().height(20.dp).padding(vertical = 4.dp).background(Color.LightGray))
        }
    }
}

fun shareAnime(context: android.content.Context, anime: Anime) {
    val sendIntent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        putExtra(android.content.Intent.EXTRA_TEXT, "Смотри аниме: ${anime.title}")
        type = "text/plain"
    }
    context.startActivity(android.content.Intent.createChooser(sendIntent, null))
}
package com.apple.music.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.foundation.lazy.list.itemsIndexed
import com.apple.music.tv.data.model.Playlist
import com.apple.music.tv.ui.components.HeroBanner
import com.apple.music.tv.ui.components.PlaylistCard
import com.apple.music.tv.ui.components.TrackCard
import com.apple.music.tv.ui.theme.AppleMusicRed
import com.apple.music.tv.ui.theme.BackgroundDark
import com.apple.music.tv.viewmodel.DashboardViewModel
import com.apple.music.tv.viewmodel.NowPlayingViewModel
import com.apple.music.tv.viewmodel.Shelf

/**
 * Home dashboard.
 *
 * Renders a featured [HeroBanner] followed by multiple horizontally-scrollable shelves
 * of real Apple Music catalogue tracks and a row of curated playlists. Selecting any
 * item loads it into the shared player queue and opens Now Playing.
 *
 * @param onOpenNowPlaying Navigates to the full-screen Now Playing view.
 */
@Composable
fun DashboardScreen(
    onOpenNowPlaying: () -> Unit,
    viewModel: DashboardViewModel = viewModel(),
    playerViewModel: NowPlayingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTrack by playerViewModel.currentTrack.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
    ) {
        when {
            state.isLoading && state.shelves.isEmpty() -> LoadingState()
            state.errorMessage != null && state.shelves.isEmpty() ->
                ErrorState(message = state.errorMessage!!, onRetry = viewModel::refresh)
            else -> {
                TvLazyColumn(
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    state.hero?.let { hero ->
                        item {
                            HeroBanner(
                                track = hero,
                                onPlay = {
                                    val firstShelf = state.shelves.firstOrNull()?.tracks ?: listOf(hero)
                                    viewModel.play(firstShelf, 0)
                                    onOpenNowPlaying()
                                },
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    items(state.shelves) { shelf ->
                        TrackShelf(
                            shelf = shelf,
                            currentTrackId = currentTrack?.id,
                            onTrackClick = { index ->
                                viewModel.play(shelf.tracks, index)
                                onOpenNowPlaying()
                            },
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    if (state.playlists.isNotEmpty()) {
                        item { SectionHeader(title = "Made For You") }
                        item {
                            PlaylistRow(
                                playlists = state.playlists,
                                onPlaylistClick = { playlist ->
                                    viewModel.playPlaylist(playlist)
                                    onOpenNowPlaying()
                                },
                            )
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackShelf(
    shelf: Shelf,
    currentTrackId: String?,
    onTrackClick: (index: Int) -> Unit,
) {
    Column {
        SectionHeader(title = shelf.title)
        TvLazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 4.dp),
        ) {
            itemsIndexed(shelf.tracks) { index, track ->
                TrackCard(
                    track = track,
                    isCurrent = track.id == currentTrackId,
                    onClick = { onTrackClick(index) },
                )
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
) {
    TvLazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 4.dp),
    ) {
        items(playlists) { playlist ->
            PlaylistCard(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist) },
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AppleMusicRed),
        )
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppleMusicRed)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Loading Apple Music…",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Spacer(Modifier.height(16.dp))
            androidx.tv.material3.Button(onClick = onRetry) {
                Text("Retry", color = Color.White)
            }
        }
    }
}

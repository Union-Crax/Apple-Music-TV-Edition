package com.apple.music.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.itemsIndexed
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import com.apple.music.tv.ui.components.TrackCard
import com.apple.music.tv.ui.theme.AppleMusicRed
import com.apple.music.tv.ui.theme.BackgroundDark
import com.apple.music.tv.ui.theme.SurfaceDark
import com.apple.music.tv.viewmodel.NowPlayingViewModel
import com.apple.music.tv.viewmodel.SearchViewModel

/**
 * Search screen.
 *
 * Provides a text field (for keyboard-equipped setups) plus a row of one-tap
 * suggestion chips so the feature is fully usable with only a D-pad remote. Results
 * render in a focusable grid; selecting a track plays the whole result set as a queue.
 */
@Composable
fun SearchScreen(
    onOpenNowPlaying: () -> Unit,
    viewModel: SearchViewModel = viewModel(),
    playerViewModel: NowPlayingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTrack by playerViewModel.currentTrack.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 48.dp),
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(0.6f),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
            },
            placeholder = { Text("Artists, songs, or albums", color = Color.White.copy(alpha = 0.5f)) },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppleMusicRed,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AppleMusicRed,
            ),
        )

        Spacer(Modifier.height(20.dp))

        // Quick suggestion chips (usable with a D-pad only, no typing required).
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            viewModel.suggestions.take(6).forEach { suggestion ->
                SuggestionChip(text = suggestion, onClick = { viewModel.searchNow(suggestion) })
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppleMusicRed)
                    }
                }

                state.hasSearched && state.results.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No results for \"${state.query}\"",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }

                state.results.isNotEmpty() -> {
                    TvLazyVerticalGrid(
                        columns = TvGridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        itemsIndexed(state.results) { index, track ->
                            TrackCard(
                                track = track,
                                isCurrent = track.id == currentTrack?.id,
                                onClick = {
                                    viewModel.play(index)
                                    onOpenNowPlaying()
                                },
                            )
                        }
                    }
                }

                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Search Apple Music for millions of songs",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.55f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = SurfaceDark,
            contentColor = Color.White.copy(alpha = 0.85f),
            focusedContainerColor = AppleMusicRed,
            focusedContentColor = Color.White,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}

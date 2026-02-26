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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import com.apple.music.tv.data.MockData
import com.apple.music.tv.ui.components.PlaylistCard
import com.apple.music.tv.ui.components.TrackCard
import com.apple.music.tv.ui.theme.BackgroundDark

/**
 * Main Dashboard screen.
 *
 * Displays two horizontally scrollable rows — **Recently Played** tracks and
 * **Playlists** — inside a vertically scrollable [TvLazyColumn].
 *
 * Both inner [TvLazyRow] grids are navigable via the D-pad left/right keys.
 * Vertical D-pad keys move focus between the two rows.  All focus management
 * is handled automatically by the Compose for TV foundation library.
 *
 * @param onTrackClick Called with the track ID when the user selects a track card.
 */
@Composable
fun DashboardScreen(onTrackClick: (trackId: String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
    ) {
        TvLazyColumn(
            contentPadding = PaddingValues(horizontal = 64.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            // ── App header ───────────────────────────────────────────────────
            item {
                Text(
                    text = "Apple Music",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(32.dp))
            }

            // ── Recently Played ───────────────────────────────────────────────
            item {
                SectionHeader(title = "Recently Played")
            }
            item {
                TvLazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 4.dp),
                ) {
                    items(MockData.recentlyPlayed) { track ->
                        TrackCard(
                            track = track,
                            onClick = { onTrackClick(track.id) },
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }

            // ── Playlists ─────────────────────────────────────────────────────
            item {
                SectionHeader(title = "Playlists")
            }
            item {
                TvLazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 4.dp),
                ) {
                    items(MockData.playlists) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { /* TODO: navigate to playlist detail */ },
                        )
                    }
                }
            }
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
        Spacer(Modifier.height(4.dp))
        // Short accent line below the heading (fixed width, Apple Music red)
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

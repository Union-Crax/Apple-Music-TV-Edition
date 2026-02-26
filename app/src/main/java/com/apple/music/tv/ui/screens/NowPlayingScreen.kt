package com.apple.music.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import com.apple.music.tv.data.MockData
import com.apple.music.tv.ui.components.LyricsPanel
import com.apple.music.tv.ui.theme.AppleMusicRed
import com.apple.music.tv.ui.theme.BackgroundDark
import com.apple.music.tv.viewmodel.NowPlayingViewModel
import java.util.Locale

/**
 * Full-screen Now Playing experience.
 *
 * Layout (landscape):
 *  ┌──────────────────────────────────────────────────────────────────┐
 *  │  [blurred album art fills entire background]                     │
 *  │  ┌─────────────┐   ┌───────────────────────────────────────┐    │
 *  │  │             │   │  Track title                          │    │
 *  │  │  Album art  │   │  Artist · Album                       │    │
 *  │  │  (hi-res)   │   │  ──────────────────── progress bar    │    │
 *  │  │             │   │  [◀◀]  [▶ / ‖]  [▶▶]                 │    │
 *  │  │             │   │                                        │    │
 *  │  │             │   │  Lyrics                               │    │
 *  │  │             │   │  (synchronized, auto-scrolling)       │    │
 *  │  └─────────────┘   └───────────────────────────────────────┘    │
 *  └──────────────────────────────────────────────────────────────────┘
 *
 * The background uses [Modifier.blur] applied to an [AsyncImage] loaded by Coil.
 * A dark gradient overlay ensures text remains legible over any album artwork.
 *
 * Playback state is managed by [NowPlayingViewModel].  The lyrics list auto-
 * scrolls to keep the currently active line centred in the lyrics panel.
 *
 * @param trackId  ID of the track to display (matches [com.apple.music.tv.data.model.Track.id]).
 * @param onBack   Callback invoked when the user presses Back / D-pad Back.
 * @param viewModel ViewModel instance (injected by default via [viewModel]).
 */
@Composable
fun NowPlayingScreen(
    trackId: String,
    onBack: () -> Unit,
    viewModel: NowPlayingViewModel = viewModel(),
) {
    // Handle Android TV back button / remote back key
    BackHandler(onBack = onBack)

    // Load the requested track when the screen first appears (or when trackId changes)
    LaunchedEffect(trackId) {
        viewModel.loadTrack(trackId)
    }

    val track by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()

    // Fallback to first track so the layout is never empty while the track loads
    val displayTrack = track ?: MockData.recentlyPlayed.first()

    // Determine the index of the currently active lyric line
    val currentLyricIndex = remember(currentPositionMs, lyrics) {
        lyrics.indexOfLast { it.timeMs <= currentPositionMs }.coerceAtLeast(0)
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {

        // ── Blurred background ────────────────────────────────────────────────
        AsyncImage(
            model = displayTrack.artworkUrl,
            contentDescription = null, // decorative
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(
                    radiusX = 80.dp,
                    radiusY = 80.dp,
                    edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded,
                ),
        )

        // Dark gradient overlay for legibility (darker on the left where text lives)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        0.0f to Color.Black.copy(alpha = 0.80f),
                        0.6f to Color.Black.copy(alpha = 0.55f),
                        1.0f to Color.Black.copy(alpha = 0.30f),
                    ),
                ),
        )

        // ── Main content ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 64.dp, vertical = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            // ── High-resolution album art ─────────────────────────────────────
            AsyncImage(
                model = displayTrack.artworkUrl,
                contentDescription = "${displayTrack.title} artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )

            // ── Right panel: track info + controls + lyrics ───────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                // Track title
                Text(
                    text = displayTrack.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // Artist
                Text(
                    text = displayTrack.artist,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Album
                Text(
                    text = displayTrack.album,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.50f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(4.dp))

                // ── Progress bar ──────────────────────────────────────────────
                val progress =
                    if (displayTrack.durationMs > 0L) {
                        (currentPositionMs.toFloat() / displayTrack.durationMs).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AppleMusicRed,
                    trackColor = Color.White.copy(alpha = 0.30f),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatMillis(currentPositionMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                    Text(
                        text = formatMillis(displayTrack.durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                }

                Spacer(Modifier.height(4.dp))

                // ── Playback controls ─────────────────────────────────────────
                // Surface from tv-material3 gives each button a TV focus ring,
                // correct keyboard/D-pad activation, and the right ripple behaviour.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Previous
                    ControlButton(
                        onClick = {
                            // Restart from the beginning (no previous track in mock data)
                            viewModel.seekTo(0L)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    // Play / Pause – slightly larger and accent-coloured to draw focus
                    Surface(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = AppleMusicRed,
                            contentColor = Color.White,
                            focusedContainerColor = AppleMusicRed,
                            focusedContentColor = Color.White,
                        ),
                        scale = ClickableSurfaceDefaults.scale(
                            focusedScale = 1.10f,
                        ),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp),
                            )
                        }
                    }

                    // Next
                    ControlButton(onClick = { /* TODO: advance to next track */ }) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Synchronized lyrics ───────────────────────────────────────
                if (lyrics.isNotEmpty()) {
                    Text(
                        text = "Lyrics",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                    LyricsPanel(
                        lyrics = lyrics,
                        currentIndex = currentLyricIndex,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

// ── Small helpers ─────────────────────────────────────────────────────────────

/**
 * Focusable icon button styled for the TV remote control.
 * Wraps [Surface] from [androidx.tv.material3] to inherit TV focus handling.
 */
@Composable
private fun ControlButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.15f),
            contentColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.30f),
            focusedContentColor = Color.White,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.10f),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/** Formats [millis] as `m:ss` (e.g. `3:25`). */
private fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1_000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

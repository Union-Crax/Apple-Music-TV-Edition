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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.apple.music.tv.data.model.Track
import com.apple.music.tv.playback.RepeatMode
import com.apple.music.tv.ui.components.LyricsPanel
import com.apple.music.tv.ui.theme.AppleMusicRed
import com.apple.music.tv.ui.theme.BackgroundDark
import com.apple.music.tv.ui.util.formatMillis
import com.apple.music.tv.viewmodel.NowPlayingViewModel

/**
 * Full-screen Now Playing experience, driven entirely by the shared ExoPlayer state.
 *
 * Left: high-resolution album art over a blurred backdrop. Right: track metadata, a live
 * progress bar, a full transport (shuffle · previous · play/pause · next · repeat), and
 * either synchronized lyrics or an "Up Next" queue depending on availability.
 *
 * @param onBack     Callback invoked when the user presses Back / the D-pad back key.
 * @param viewModel  Player-backed ViewModel (shared with the mini-player).
 */
@Composable
fun NowPlayingScreen(
    onBack: () -> Unit,
    viewModel: NowPlayingViewModel = viewModel(),
) {
    BackHandler(onBack = onBack)

    val track by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isBuffering by viewModel.isBuffering.collectAsStateWithLifecycle()
    val positionMs by viewModel.positionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyrics.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    val shuffle by viewModel.shuffle.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()

    val displayTrack = track

    if (displayTrack == null) {
        EmptyPlaybackState()
        return
    }

    val currentLyricIndex = remember(positionMs, lyrics) {
        lyrics.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0)
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {

        // ── Blurred background ────────────────────────────────────────────────
        AsyncImage(
            model = displayTrack.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(
                    radiusX = 80.dp,
                    radiusY = 80.dp,
                    edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded,
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        0.0f to Color.Black.copy(alpha = 0.82f),
                        0.6f to Color.Black.copy(alpha = 0.55f),
                        1.0f to Color.Black.copy(alpha = 0.30f),
                    ),
                ),
        )

        // ── Main content ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 56.dp, vertical = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            AsyncImage(
                model = displayTrack.artworkUrl,
                contentDescription = "${displayTrack.title} artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(340.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = displayTrack.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = displayTrack.artist,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = displayTrack.album,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(6.dp))

                // ── Progress bar ──────────────────────────────────────────────
                val progress = if (durationMs > 0L) {
                    (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
                } else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.25f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(AppleMusicRed),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatMillis(positionMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                    Text(
                        text = if (durationMs > 0L) formatMillis(durationMs) else "0:30",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                }

                Spacer(Modifier.height(6.dp))

                // ── Transport controls ────────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ControlButton(
                        icon = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        active = shuffle,
                        onClick = viewModel::toggleShuffle,
                    )
                    ControlButton(
                        icon = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        onClick = viewModel::previous,
                    )

                    // Play / Pause – larger, accent-coloured focal control.
                    Surface(
                        onClick = viewModel::togglePlayPause,
                        modifier = Modifier.size(72.dp),
                        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = AppleMusicRed,
                            contentColor = Color.White,
                            focusedContainerColor = Color.White,
                            focusedContentColor = AppleMusicRed,
                        ),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.10f),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (isBuffering) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(32.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    modifier = Modifier.size(40.dp),
                                )
                            }
                        }
                    }

                    ControlButton(
                        icon = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        onClick = viewModel::next,
                    )
                    ControlButton(
                        icon = if (repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Repeat",
                        active = repeatMode != RepeatMode.OFF,
                        onClick = viewModel::cycleRepeat,
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ── Lyrics OR Up Next ─────────────────────────────────────────
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
                } else if (queue.size > 1) {
                    Text(
                        text = "Up Next",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                    Spacer(Modifier.height(6.dp))
                    UpNextList(
                        queue = queue,
                        currentIndex = currentIndex,
                        onPlayIndex = viewModel::playIndex,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

// ── Small helpers ─────────────────────────────────────────────────────────────

/** Focusable icon button styled for the TV remote; [active] tints it with the accent. */
@Composable
private fun ControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    active: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(52.dp),
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) AppleMusicRed.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.15f),
            contentColor = Color.White,
            focusedContainerColor = if (active) AppleMusicRed else Color.White.copy(alpha = 0.30f),
            focusedContentColor = Color.White,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.12f),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun UpNextList(
    queue: List<Track>,
    currentIndex: Int,
    onPlayIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        itemsIndexed(queue) { index, item ->
            if (index <= currentIndex) return@itemsIndexed
            Surface(
                onClick = { onPlayIndex(index) },
                shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.White.copy(alpha = 0.06f),
                    contentColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.18f),
                    focusedContentColor = Color.White,
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.01f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AsyncImage(
                        model = item.artworkUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp)),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = item.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPlaybackState() {
    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundDark),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Nothing playing yet.\nPick a song to start listening.",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.7f),
        )
    }
}

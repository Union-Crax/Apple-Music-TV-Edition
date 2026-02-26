package com.apple.music.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import coil.compose.AsyncImage
import com.apple.music.tv.data.model.Playlist
import com.apple.music.tv.data.model.Track
import com.apple.music.tv.ui.theme.AppleMusicRed
import com.apple.music.tv.ui.theme.SurfaceDark

// ── Dimensions ────────────────────────────────────────────────────────────────
private val CardWidth = 180.dp
private val CardHeight = 220.dp
private val ArtworkSize = 180.dp
private val CornerRadius = 12.dp

/**
 * Focusable card that displays a [Track] thumbnail, title, and artist name.
 *
 * Uses [androidx.tv.material3.Card] which provides the TV-platform focus indicator
 * and D-pad / ENTER key click handling out of the box.
 */
@Composable
fun TrackCard(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(width = CardWidth, height = CardHeight),
        shape = CardDefaults.shape(shape = RoundedCornerShape(CornerRadius)),
        colors = CardDefaults.colors(containerColor = SurfaceDark),
        scale = CardDefaults.scale(focusedScale = 1.08f),
    ) {
        Column {
            // Album artwork fills the top portion of the card
            Box(modifier = Modifier.size(ArtworkSize)) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = "${track.title} artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = CornerRadius, topEnd = CornerRadius)),
                )
                // Subtle bottom gradient so white text remains readable on bright artwork
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                startY = ArtworkSize.value * 0.5f,
                            ),
                        ),
                )
            }

            // Track metadata
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Focusable card that displays a [Playlist] cover image, name, and track count.
 */
@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(width = CardWidth, height = CardHeight),
        shape = CardDefaults.shape(shape = RoundedCornerShape(CornerRadius)),
        colors = CardDefaults.colors(containerColor = SurfaceDark),
        scale = CardDefaults.scale(focusedScale = 1.08f),
    ) {
        Column {
            Box(modifier = Modifier.size(ArtworkSize)) {
                AsyncImage(
                    model = playlist.artworkUrl,
                    contentDescription = "${playlist.name} cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = CornerRadius, topEnd = CornerRadius)),
                )
                // "Playlist" badge in the top-right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(color = AppleMusicRed, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "Playlist",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${playlist.trackCount} tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }
    }
}

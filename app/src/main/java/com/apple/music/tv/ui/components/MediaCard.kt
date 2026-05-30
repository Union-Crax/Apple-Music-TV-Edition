package com.apple.music.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apple.music.tv.data.model.Playlist
import com.apple.music.tv.data.model.Track
import com.apple.music.tv.ui.theme.AppleMusicRed
import com.apple.music.tv.ui.theme.TextPrimary
import com.apple.music.tv.ui.theme.TextSecondary

private val CardWidth = 196.dp
private val ArtCorner = 12.dp

/** Card for a song. The whole card lifts and reveals a Play badge on focus. */
@Composable
fun TrackCard(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
) {
    MediaCard(
        title = track.title,
        subtitle = track.artist,
        colorStart = track.colorStart,
        colorEnd = track.colorEnd,
        seed = track.id + track.title,
        badge = if (track.isLive) "Live" else null,
        coverLabel = if (track.isLive) track.title else null,
        onClick = onClick,
        modifier = modifier,
        focusRequester = focusRequester,
    )
}

/** Card for a playlist. The cover shows the playlist name, Apple-style. */
@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
) {
    MediaCard(
        title = playlist.name,
        subtitle = playlist.subtitle,
        colorStart = playlist.colorStart,
        colorEnd = playlist.colorEnd,
        seed = playlist.id + playlist.name,
        coverLabel = playlist.name,
        badge = "Playlist",
        onClick = onClick,
        modifier = modifier,
        focusRequester = focusRequester,
    )
}

@Composable
private fun MediaCard(
    title: String,
    subtitle: String,
    colorStart: Long,
    colorEnd: Long,
    seed: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    coverLabel: String? = null,
    badge: String? = null,
    focusRequester: FocusRequester? = null,
) {
    FocusableItem(onClick = onClick, modifier = modifier, focusRequester = focusRequester) { focused ->
        val scale by animateFloatAsState(if (focused) 1.06f else 1f, label = "cardScale")

        Column(
            modifier = Modifier
                .width(CardWidth)
                .graphicsLayer { scaleX = scale; scaleY = scale },
        ) {
            Box(
                modifier = Modifier
                    .size(CardWidth)
                    .shadow(
                        elevation = if (focused) 22.dp else 6.dp,
                        shape = RoundedCornerShape(ArtCorner),
                        clip = false,
                    ),
            ) {
                Artwork(
                    colorStart = colorStart,
                    colorEnd = colorEnd,
                    seed = seed,
                    cornerRadius = ArtCorner,
                    label = coverLabel,
                    badge = badge,
                    labelSize = 21.sp,
                    modifier = Modifier.size(CardWidth),
                )

                if (focused) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                color = if (focused) AppleMusicRed else TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

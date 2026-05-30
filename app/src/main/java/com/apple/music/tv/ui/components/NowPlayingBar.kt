package com.apple.music.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apple.music.tv.playback.PlayerViewModel
import com.apple.music.tv.ui.theme.AppleMusicRed
import com.apple.music.tv.ui.theme.Elevated
import com.apple.music.tv.ui.theme.Hairline
import com.apple.music.tv.ui.theme.SidebarBackground
import com.apple.music.tv.ui.theme.TextPrimary
import com.apple.music.tv.ui.theme.TextSecondary

/**
 * Compact, always-on player pinned to the bottom of the browse shell. Shows the
 * current track, a live progress line, and transport controls. Selecting the
 * track info expands the immersive full-screen player.
 */
@Composable
fun NowPlayingBar(
    player: PlayerViewModel,
    onExpand: () -> Unit,
) {
    val track by player.currentTrack.collectAsState()
    val isPlaying by player.isPlaying.collectAsState()
    val positionMs by player.positionMs.collectAsState()
    val t = track ?: return

    val progress = if (t.durationMs > 0) (positionMs.toFloat() / t.durationMs).coerceIn(0f, 1f) else 0f

    Column {
        // Hairline + thin progress line across the very top of the bar.
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Hairline)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(2.dp)
                    .background(AppleMusicRed),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(SidebarBackground)
                .padding(horizontal = 22.dp, vertical = 12.dp),
        ) {
            // Track info — focus to expand the full player.
            FocusableItem(onClick = onExpand, modifier = Modifier.weight(1f)) { focused ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Artwork(
                        colorStart = t.colorStart,
                        colorEnd = t.colorEnd,
                        seed = t.id + t.title,
                        cornerRadius = 8.dp,
                        modifier = Modifier.size(52.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = t.title,
                            color = if (focused) AppleMusicRed else TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (t.isLive) "${t.artist} · LIVE" else t.artist,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BarButton(Icons.Rounded.SkipPrevious, "Previous", size = 40.dp, iconSize = 24.dp) { player.previous() }
                BarButton(
                    icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    size = 46.dp,
                    iconSize = 28.dp,
                    accentWhenFocused = true,
                ) { player.togglePlay() }
                BarButton(Icons.Rounded.SkipNext, "Next", size = 40.dp, iconSize = 24.dp) { player.next() }
            }
        }
    }
}

@Composable
private fun BarButton(
    icon: ImageVector,
    contentDescription: String,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    accentWhenFocused: Boolean = false,
    onClick: () -> Unit,
) {
    FocusableItem(onClick = onClick) { focused ->
        val scale by animateFloatAsState(if (focused) 1.12f else 1f, label = "barBtn")
        val bg = when {
            focused && accentWhenFocused -> AppleMusicRed
            focused -> Elevated
            else -> Color.Transparent
        }
        Box(
            modifier = Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .size(size)
                .background(bg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (focused && accentWhenFocused) Color.White else TextPrimary,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

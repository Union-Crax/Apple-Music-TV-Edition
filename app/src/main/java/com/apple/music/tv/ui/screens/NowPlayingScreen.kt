package com.apple.music.tv.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apple.music.tv.playback.PlayerViewModel
import com.apple.music.tv.playback.RepeatMode
import com.apple.music.tv.ui.components.Artwork
import com.apple.music.tv.ui.components.FocusableItem
import com.apple.music.tv.ui.components.LyricsPanel
import com.apple.music.tv.ui.theme.AppleMusicRed
import com.apple.music.tv.ui.theme.Black
import com.apple.music.tv.ui.theme.Elevated
import com.apple.music.tv.ui.theme.TextPrimary
import com.apple.music.tv.ui.theme.TextSecondary
import com.apple.music.tv.ui.theme.TextTertiary
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect

private const val SEEK_STEP_MS = 5_000L
private const val VOLUME_STEP = 0.05f

/**
 * Immersive, full-screen Now Playing experience.
 *
 * Left: large generated cover. Right: metadata, a D-pad-driven scrubber
 * (◀/▶ to seek), transport controls, a volume slider, and time-synced lyrics.
 * The ambient background is tinted with the current track's gradient.
 */
@Composable
fun NowPlayingScreen(
    player: PlayerViewModel,
    onCollapse: () -> Unit,
) {
    val track by player.currentTrack.collectAsState()
    val isPlaying by player.isPlaying.collectAsState()
    val positionMs by player.positionMs.collectAsState()
    val shuffle by player.shuffle.collectAsState()
    val repeat by player.repeat.collectAsState()
    val volume by player.volume.collectAsState()
    val lyrics by player.lyrics.collectAsState()

    val t = track ?: return
    val progress = if (t.durationMs > 0) (positionMs.toFloat() / t.durationMs).coerceIn(0f, 1f) else 0f
    val currentLyric = remember(positionMs, lyrics) {
        lyrics.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0)
    }

    val playFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { playFocus.requestFocus() } }

    Box(modifier = Modifier.fillMaxSize().background(Black)) {

        // Ambient tint from the track's gradient.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(t.colorStart).copy(alpha = 0.55f),
                        0.5f to Color(t.colorEnd).copy(alpha = 0.22f),
                        1f to Black,
                    ),
                ),
        )

        Column(modifier = Modifier.fillMaxSize().padding(40.dp)) {

            // Collapse affordance
            FocusableItem(onClick = onCollapse) { focused ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (focused) AppleMusicRed else Elevated, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {

                // ── Cover ────────────────────────────────────────────────
                Artwork(
                    colorStart = t.colorStart,
                    colorEnd = t.colorEnd,
                    seed = t.id + t.title,
                    cornerRadius = 18.dp,
                    label = if (t.isLive) t.title else null,
                    badge = if (t.isLive) "Live" else null,
                    labelSize = 30.sp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                )

                Spacer(Modifier.width(44.dp))

                // ── Right column ─────────────────────────────────────────
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Text(
                        text = t.title,
                        color = TextPrimary,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(t.artist, color = AppleMusicRed, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(t.album, color = TextSecondary, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                    Spacer(Modifier.height(22.dp))

                    // Scrubber
                    if (t.isLive) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(AppleMusicRed, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("LIVE", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        ValueBar(
                            fraction = progress,
                            onDecrease = { player.seekTo((positionMs - SEEK_STEP_MS)) },
                            onIncrease = { player.seekTo((positionMs + SEEK_STEP_MS)) },
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatMs(positionMs), color = TextSecondary, fontSize = 13.sp)
                            Text(formatMs(t.durationMs), color = TextSecondary, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Transport
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        TransportButton(Icons.Rounded.Shuffle, "Shuffle", active = shuffle) { player.toggleShuffle() }
                        TransportButton(Icons.Rounded.SkipPrevious, "Previous", iconSize = 30.dp) { player.previous() }
                        TransportButton(
                            icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            size = 64.dp,
                            iconSize = 38.dp,
                            primary = true,
                            focusRequester = playFocus,
                        ) { player.togglePlay() }
                        TransportButton(Icons.Rounded.SkipNext, "Next", iconSize = 30.dp) { player.next() }
                        TransportButton(
                            icon = if (repeat == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                            contentDescription = "Repeat",
                            active = repeat != RepeatMode.OFF,
                        ) { player.cycleRepeat() }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Volume
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Box(modifier = Modifier.width(220.dp)) {
                            ValueBar(
                                fraction = volume,
                                onDecrease = { player.setVolume(volume - VOLUME_STEP) },
                                onIncrease = { player.setVolume(volume + VOLUME_STEP) },
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Lyrics
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        if (lyrics.isNotEmpty()) {
                            LyricsPanel(
                                lyrics = lyrics,
                                currentIndex = currentLyric,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Text(
                                text = "Lyrics not available for this track.",
                                color = TextTertiary,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Controls ─────────────────────────────────────────────────────────────────────

@Composable
private fun TransportButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp = 50.dp,
    iconSize: Dp = 24.dp,
    primary: Boolean = false,
    active: Boolean = false,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
) {
    FocusableItem(onClick = onClick, focusRequester = focusRequester) { focused ->
        val scale by animateFloatAsState(if (focused) 1.12f else 1f, label = "tBtn")
        val bg = when {
            primary -> AppleMusicRed
            focused -> Elevated
            else -> Color.Transparent
        }
        val tint = when {
            primary -> Color.White
            active -> AppleMusicRed
            focused -> Color.White
            else -> TextPrimary
        }
        Box(
            modifier = Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .size(size)
                .background(bg, CircleShape)
                .then(
                    if (focused && !primary) Modifier.background(Elevated, CircleShape) else Modifier,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
        }
    }
}

/**
 * A focusable progress/volume bar driven by the D-pad: ◀ decreases, ▶ increases.
 * Built directly on [focusable] + key handling so left/right are consumed here
 * instead of moving focus away.
 */
@Composable
private fun ValueBar(
    fraction: Float,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val height = if (focused) 8.dp else 5.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent { e ->
                if (e.type == KeyEventType.KeyDown) {
                    when (e.key) {
                        Key.DirectionLeft -> { onDecrease(); true }
                        Key.DirectionRight -> { onIncrease(); true }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusable(interactionSource = interaction),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f)),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height)
                .clip(CircleShape)
                .background(if (focused) AppleMusicRed else Color.White),
        )
        if (focused) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(modifier = Modifier.size(14.dp).background(Color.White, CircleShape))
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%d:%02d", totalSeconds / 60, totalSeconds % 60)
}

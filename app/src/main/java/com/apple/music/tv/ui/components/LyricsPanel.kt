package com.apple.music.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apple.music.tv.data.model.LyricLine
import com.apple.music.tv.ui.theme.TextPrimary
import com.apple.music.tv.ui.theme.TextTertiary

/**
 * Time-synced lyrics, Apple-Music style: the active line is large, bright and
 * gently scaled, sung lines fade back, and the list auto-scrolls to keep the
 * current line in view.
 *
 * @param currentIndex Last index whose [LyricLine.timeMs] ≤ playback position.
 */
@Composable
fun LyricsPanel(
    lyrics: List<LyricLine>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentIndex) {
        if (lyrics.isNotEmpty()) {
            listState.animateScrollToItem(
                index = currentIndex.coerceIn(0, lyrics.lastIndex),
                scrollOffset = -120,
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isActive = index == currentIndex
            val color by animateColorAsState(
                targetValue = when {
                    isActive -> TextPrimary
                    index < currentIndex -> TextTertiary
                    else -> Color.White.copy(alpha = 0.5f)
                },
                animationSpec = tween(280),
                label = "lyricColor",
            )
            val scale by animateFloatAsState(if (isActive) 1f else 0.96f, label = "lyricScale")

            androidx.compose.material3.Text(
                text = line.text.ifEmpty { "• • •" },
                color = color,
                fontSize = if (isActive) 26.sp else 22.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale; scaleY = scale
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                    }
                    .padding(vertical = 7.dp),
            )
        }
    }
}

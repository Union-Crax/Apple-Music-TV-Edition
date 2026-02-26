package com.apple.music.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apple.music.tv.data.model.LyricLine

/**
 * Vertically scrollable lyrics panel that highlights the currently active line
 * and auto-scrolls to keep it visible.
 *
 * The current line is determined by the caller via [currentIndex], which should
 * be the last index whose [LyricLine.timeMs] ≤ the playback position.
 *
 * Empty lyric lines (instrumental gaps) are rendered as a blank spacer so timing
 * stays aligned with the music.
 *
 * @param lyrics       Ordered list of timestamped lyric lines.
 * @param currentIndex Index of the currently active line (0-based).
 * @param modifier     Additional layout modifiers applied to the [LazyColumn].
 */
@Composable
fun LyricsPanel(
    lyrics: List<LyricLine>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Smoothly scroll so the active lyric line is always visible.
    LaunchedEffect(currentIndex) {
        if (lyrics.isNotEmpty()) {
            listState.animateScrollToItem(
                index = currentIndex.coerceIn(0, lyrics.lastIndex),
                scrollOffset = -80,
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isActive = index == currentIndex

            // Animate the colour transition so lines fade in/out gracefully.
            val textColor by animateColorAsState(
                targetValue = when {
                    isActive -> Color.White
                    index < currentIndex -> Color.White.copy(alpha = 0.35f) // already sung
                    else -> Color.White.copy(alpha = 0.55f)                  // upcoming
                },
                animationSpec = tween(durationMillis = 300),
                label = "lyricColor-$index",
            )

            Text(
                text = line.text.ifEmpty { "\u2022" }, // bullet for instrumental gaps
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (isActive) {
                        MaterialTheme.typography.titleMedium.fontSize
                    } else {
                        MaterialTheme.typography.bodyLarge.fontSize
                    },
                ),
                color = textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
            )
        }
    }
}

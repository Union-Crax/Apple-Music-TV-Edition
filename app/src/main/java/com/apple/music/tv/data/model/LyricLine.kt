package com.apple.music.tv.data.model

/**
 * A single timestamped line of synchronized lyrics.
 *
 * @param timeMs Offset from the start of the track (in milliseconds) at which this line begins.
 * @param text   The lyric text to display. May be empty for instrumental sections.
 */
data class LyricLine(
    val timeMs: Long,
    val text: String,
)

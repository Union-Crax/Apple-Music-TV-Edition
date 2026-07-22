package com.apple.music.tv.ui.util

import java.util.Locale

/** Formats [millis] as `m:ss` (e.g. `3:25`). Negative values clamp to `0:00`. */
fun formatMillis(millis: Long): String {
    val totalSeconds = (millis.coerceAtLeast(0L)) / 1_000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

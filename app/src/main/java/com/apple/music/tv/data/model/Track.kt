package com.apple.music.tv.data.model

/**
 * Represents a single music track surfaced in the dashboard and Now Playing screen.
 *
 * @param id          Unique identifier (used as navigation argument).
 * @param title       Song title.
 * @param artist      Primary artist name.
 * @param album       Album name.
 * @param artworkUrl  Remote URL for high-resolution (600×600) square album artwork.
 * @param durationMs  Total playback duration in milliseconds.
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String,
    val durationMs: Long,
)

package com.apple.music.tv.data.model

/**
 * Represents a single music track surfaced in the dashboard and Now Playing screen.
 *
 * @param id          Unique identifier (used as navigation argument). For catalogue
 *                    tracks this is the Apple/iTunes `trackId`.
 * @param title       Song title.
 * @param artist      Primary artist name.
 * @param album       Album name.
 * @param artworkUrl  Remote URL for high-resolution (600×600) square album artwork.
 * @param durationMs  Total playback duration in milliseconds.
 * @param previewUrl  Direct URL to the 30-second AAC preview stream returned by
 *                    Apple. `null` when no playable preview is available.
 * @param genre       Primary genre name, when known.
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String,
    val durationMs: Long,
    val previewUrl: String? = null,
    val genre: String? = null,
) {
    /** Whether this track has a stream that ExoPlayer can actually play. */
    val isPlayable: Boolean get() = !previewUrl.isNullOrBlank()
}

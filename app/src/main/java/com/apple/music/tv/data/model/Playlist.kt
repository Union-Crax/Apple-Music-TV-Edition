package com.apple.music.tv.data.model

/**
 * Represents a user playlist shown in the dashboard grid.
 *
 * @param id         Unique identifier.
 * @param name       Display name of the playlist.
 * @param artworkUrl Remote URL for the playlist cover image.
 * @param trackCount Number of tracks in the playlist.
 */
data class Playlist(
    val id: String,
    val name: String,
    val artworkUrl: String,
    val trackCount: Int,
)

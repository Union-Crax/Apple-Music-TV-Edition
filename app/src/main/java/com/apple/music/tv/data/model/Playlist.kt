package com.apple.music.tv.data.model

/**
 * Represents a curated "mix" / playlist shown in the dashboard grid.
 *
 * Each playlist is backed by an Apple Music search [term]; selecting it loads the
 * matching catalogue tracks (with real preview streams) into the playback queue.
 *
 * @param id          Unique identifier.
 * @param name        Display name of the playlist.
 * @param description Short one-line subtitle shown under the name.
 * @param artworkUrl  Remote URL for the playlist cover image.
 * @param term        Apple Music search term used to populate the playlist.
 * @param trackCount  Approximate number of tracks (updated once loaded).
 */
data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val artworkUrl: String,
    val term: String,
    val trackCount: Int,
)

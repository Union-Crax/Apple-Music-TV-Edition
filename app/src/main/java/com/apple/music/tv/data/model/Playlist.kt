package com.apple.music.tv.data.model

/**
 * A curated collection of tracks shown as a single artwork-forward card.
 *
 * Like [Track], the cover is a generated gradient ([colorStart] / [colorEnd])
 * so it renders instantly and offline.
 *
 * @param id         Unique identifier.
 * @param name       Display name of the playlist.
 * @param subtitle   Short curator line (e.g. "Apple Music Chill").
 * @param mood       Vibe used when the playlist is played.
 * @param colorStart Top-left gradient colour of the cover (ARGB).
 * @param colorEnd   Bottom-right gradient colour of the cover (ARGB).
 * @param trackIds   Ordered IDs of the tracks that make up the playlist.
 */
data class Playlist(
    val id: String,
    val name: String,
    val subtitle: String,
    val mood: Mood,
    val colorStart: Long,
    val colorEnd: Long,
    val trackIds: List<String>,
) {
    /** Number of tracks in the playlist. */
    val trackCount: Int get() = trackIds.size
}

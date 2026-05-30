package com.apple.music.tv.data.model

/**
 * A single playable item — a song, or a live radio station.
 *
 * Artwork is generated procedurally from [colorStart] / [colorEnd] (an Apple
 * Music–style gradient cover) rather than loaded from the network, so the app
 * looks polished and works fully offline.
 *
 * Colours are stored as ARGB [Long] values (e.g. `0xFFFA2D48`) to keep this
 * model free of any Compose dependency, which keeps the unit tests fast.
 *
 * @param id          Unique identifier.
 * @param title       Song / station title.
 * @param artist      Primary artist (or network name for stations).
 * @param album       Album name (or tagline for stations).
 * @param durationMs  Total playback duration in milliseconds.
 * @param mood        Drives the procedural audio engine and accents.
 * @param colorStart  Top-left gradient colour of the generated artwork (ARGB).
 * @param colorEnd    Bottom-right gradient colour of the generated artwork (ARGB).
 * @param isLive      `true` for radio stations (shows a LIVE badge, no seek bar).
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mood: Mood,
    val colorStart: Long,
    val colorEnd: Long,
    val isLive: Boolean = false,
)

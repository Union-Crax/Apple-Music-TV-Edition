package com.apple.music.tv.data.model

/**
 * High-level "vibe" of a track.
 *
 * The value drives the procedural audio engine (chord progression, tempo,
 * timbre) so that every track produces genuinely different sound, and it also
 * informs subtle UI accents. Kept in the data layer (no Compose dependency) so
 * it can be unit-tested on the JVM.
 */
enum class Mood {
    CHILL,
    DREAMY,
    BRIGHT,
    WARM,
    NIGHT,
    FOCUS,
    SUNRISE,
}

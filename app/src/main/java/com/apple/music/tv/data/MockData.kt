package com.apple.music.tv.data

import com.apple.music.tv.data.model.LyricLine
import com.apple.music.tv.data.model.Playlist
import com.apple.music.tv.data.model.Track

/**
 * In-memory mock catalogue used by the UI until a real Apple Music API integration is wired up.
 *
 * Artwork URLs use picsum.photos with deterministic seeds so the same image is always
 * returned for the same item, making the UI preview stable across runs.
 */
object MockData {

    // ── Recently Played ──────────────────────────────────────────────────────────

    val recentlyPlayed: List<Track> = listOf(
        Track(
            id = "1",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            artworkUrl = "https://picsum.photos/seed/track1/600/600",
            durationMs = 200_000L,
        ),
        Track(
            id = "2",
            title = "Anti-Hero",
            artist = "Taylor Swift",
            album = "Midnights",
            artworkUrl = "https://picsum.photos/seed/track2/600/600",
            durationMs = 195_000L,
        ),
        Track(
            id = "3",
            title = "As It Was",
            artist = "Harry Styles",
            album = "Harry's House",
            artworkUrl = "https://picsum.photos/seed/track3/600/600",
            durationMs = 167_000L,
        ),
        Track(
            id = "4",
            title = "Stay",
            artist = "The Kid LAROI & Justin Bieber",
            album = "F*CK LOVE 3",
            artworkUrl = "https://picsum.photos/seed/track4/600/600",
            durationMs = 141_000L,
        ),
        Track(
            id = "5",
            title = "Levitating",
            artist = "Dua Lipa",
            album = "Future Nostalgia",
            artworkUrl = "https://picsum.photos/seed/track5/600/600",
            durationMs = 203_000L,
        ),
        Track(
            id = "6",
            title = "Peaches",
            artist = "Justin Bieber ft. Daniel Caesar",
            album = "Justice",
            artworkUrl = "https://picsum.photos/seed/track6/600/600",
            durationMs = 198_000L,
        ),
    )

    // ── Playlists ─────────────────────────────────────────────────────────────────

    val playlists: List<Playlist> = listOf(
        Playlist(
            id = "p1",
            name = "Chill Vibes",
            artworkUrl = "https://picsum.photos/seed/playlist1/600/600",
            trackCount = 42,
        ),
        Playlist(
            id = "p2",
            name = "Workout Mix",
            artworkUrl = "https://picsum.photos/seed/playlist2/600/600",
            trackCount = 28,
        ),
        Playlist(
            id = "p3",
            name = "Top Hits 2024",
            artworkUrl = "https://picsum.photos/seed/playlist3/600/600",
            trackCount = 50,
        ),
        Playlist(
            id = "p4",
            name = "Late Night Drive",
            artworkUrl = "https://picsum.photos/seed/playlist4/600/600",
            trackCount = 35,
        ),
        Playlist(
            id = "p5",
            name = "Morning Coffee",
            artworkUrl = "https://picsum.photos/seed/playlist5/600/600",
            trackCount = 20,
        ),
    )

    // ── Synchronized Lyrics ───────────────────────────────────────────────────────

    private val lyricsMap: Map<String, List<LyricLine>> = mapOf(
        "1" to listOf(
            LyricLine(0L, ""),
            LyricLine(3_000L, "I've been tryna call"),
            LyricLine(6_500L, "I've been on my own for long enough"),
            LyricLine(10_000L, "Maybe you can show me how to love, maybe"),
            LyricLine(14_000L, "I'm going through withdrawals"),
            LyricLine(17_500L, "You don't even have to do too much"),
            LyricLine(21_000L, "You can turn me on with just a touch, baby"),
            LyricLine(25_000L, "I look around and Sin City's cold and empty"),
            LyricLine(29_000L, "No one's around to judge me"),
            LyricLine(33_000L, "I can't see clearly when you're gone"),
            LyricLine(37_000L, "I said, ooh, I'm blinded by the lights"),
            LyricLine(41_000L, "No, I can't sleep until I feel your touch"),
            LyricLine(45_000L, "I said, ooh, I'm drowning in the night"),
            LyricLine(49_000L, "Oh, when I'm like this, you're the one I trust"),
            LyricLine(53_000L, "Hey, hey, hey"),
            LyricLine(57_000L, "I'm running out of time"),
            LyricLine(60_500L, "'Cause I can see the sun light up the sky"),
            LyricLine(64_000L, "So I hit the road in overdrive, baby"),
            LyricLine(68_000L, "Oh, the city's cold and empty"),
            LyricLine(72_000L, "No one's around to judge me"),
            LyricLine(76_000L, "I can't see clearly when you're gone"),
        ),
    )

    // ── Lookup helpers ────────────────────────────────────────────────────────────

    /** Returns the [Track] with the given [id], or `null` if not found. */
    fun getTrack(id: String): Track? = recentlyPlayed.find { it.id == id }

    /**
     * Returns the timestamped lyrics for the given [trackId].
     * Returns an empty list when no lyrics are available (instrumental tracks, etc.).
     */
    fun getLyrics(trackId: String): List<LyricLine> = lyricsMap[trackId] ?: emptyList()
}

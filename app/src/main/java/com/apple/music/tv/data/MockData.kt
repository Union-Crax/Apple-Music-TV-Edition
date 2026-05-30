package com.apple.music.tv.data

import com.apple.music.tv.data.model.LyricLine
import com.apple.music.tv.data.model.Mood
import com.apple.music.tv.data.model.Playlist
import com.apple.music.tv.data.model.Track

/**
 * In-memory catalogue that powers the entire UI.
 *
 * There is no real Apple Music backend (and the app can't legally stream it), so
 * every item ships with a procedurally generated gradient cover and a [Mood] that
 * the on-device audio engine turns into actual sound. That makes the app fully
 * self-contained: it looks rich and *plays* with zero network access.
 */
object MockData {

    // ── Songs ──────────────────────────────────────────────────────────────────
    // Track "1" intentionally stays "Blinding Lights" so the bundled lyrics and
    // the unit tests remain stable.

    val songs: List<Track> = listOf(
        Track("1", "Blinding Lights", "The Weeknd", "After Hours", 200_000L, Mood.NIGHT, 0xFF5E5CE6L, 0xFF0A84FFL),
        Track("2", "Anti-Hero", "Taylor Swift", "Midnights", 195_000L, Mood.DREAMY, 0xFF5E5CE6L, 0xFFBF5AF2L),
        Track("3", "As It Was", "Harry Styles", "Harry's House", 167_000L, Mood.BRIGHT, 0xFFFF375FL, 0xFFFF9F0AL),
        Track("4", "Stay", "The Kid LAROI & Justin Bieber", "Stay", 141_000L, Mood.BRIGHT, 0xFF0A84FFL, 0xFF64D2FFL),
        Track("5", "Levitating", "Dua Lipa", "Future Nostalgia", 203_000L, Mood.SUNRISE, 0xFFFF9F0AL, 0xFFFF375FL),
        Track("6", "Peaches", "Justin Bieber", "Justice", 198_000L, Mood.WARM, 0xFFFFD60AL, 0xFFFF9F0AL),
        Track("7", "Flowers", "Miley Cyrus", "Endless Summer Vacation", 200_000L, Mood.SUNRISE, 0xFFFF9F0AL, 0xFFFFD60AL),
        Track("8", "Midnight City", "M83", "Hurry Up, We're Dreaming", 241_000L, Mood.NIGHT, 0xFF0A84FFL, 0xFF5E5CE6L),
        Track("9", "Sunflower", "Post Malone & Swae Lee", "Spider-Man: Into the Spider-Verse", 158_000L, Mood.WARM, 0xFFFFD60AL, 0xFFFF453AL),
        Track("10", "Good 4 U", "Olivia Rodrigo", "SOUR", 178_000L, Mood.BRIGHT, 0xFFFF2D55L, 0xFFBF5AF2L),
        Track("11", "Starboy", "The Weeknd", "Starboy", 230_000L, Mood.NIGHT, 0xFFFF2D55L, 0xFF5E5CE6L),
        Track("12", "Vampire", "Olivia Rodrigo", "GUTS", 219_000L, Mood.DREAMY, 0xFFBF5AF2L, 0xFFFF2D55L),
        Track("13", "Pure Shores", "All Saints", "Saints & Sinners", 240_000L, Mood.CHILL, 0xFF64D2FFL, 0xFF5E5CE6L),
        Track("14", "Weightless", "Marconi Union", "Ambient", 360_000L, Mood.FOCUS, 0xFF30D158L, 0xFF64D2FFL),
    )

    // ── Live radio stations ──────────────────────────────────────────────────────
    // Modelled as long, "live" tracks so they flow straight through the player.

    val stations: List<Track> = listOf(
        Track("s1", "Apple Music 1", "Apple Music", "Worldwide · Live", 3_600_000L, Mood.BRIGHT, 0xFFFA2D48L, 0xFFFF5E7EL, isLive = true),
        Track("s2", "Apple Music Hits", "Apple Music", "The biggest songs", 3_600_000L, Mood.WARM, 0xFF5E5CE6L, 0xFFBF5AF2L, isLive = true),
        Track("s3", "Apple Music Chill", "Apple Music", "Unwind and relax", 3_600_000L, Mood.DREAMY, 0xFF64D2FFL, 0xFF5E5CE6L, isLive = true),
        Track("s4", "Apple Music Club", "Apple Music", "Dance · Live", 3_600_000L, Mood.NIGHT, 0xFFFF2D55L, 0xFF5E5CE6L, isLive = true),
    )

    /** Every playable item (songs + stations), used for global lookups and search. */
    val allTracks: List<Track> = songs + stations

    // ── Playlists ─────────────────────────────────────────────────────────────────

    val playlists: List<Playlist> = listOf(
        Playlist("p1", "Chill Vibes", "Apple Music Chill", Mood.CHILL, 0xFF5E5CE6L, 0xFFBF5AF2L, listOf("13", "14", "8", "2", "12")),
        Playlist("p2", "Today's Hits", "Apple Music Pop", Mood.BRIGHT, 0xFFFF375FL, 0xFFFF9F0AL, listOf("3", "4", "10", "5", "7")),
        Playlist("p3", "New Music Daily", "Apple Music", Mood.SUNRISE, 0xFF0A84FFL, 0xFF64D2FFL, listOf("7", "12", "10", "3", "9")),
        Playlist("p4", "Pure Focus", "Apple Music", Mood.FOCUS, 0xFF30D158L, 0xFF0A84FFL, listOf("14", "13", "8", "1")),
        Playlist("p5", "Late Night Drive", "Apple Music", Mood.NIGHT, 0xFF0A84FFL, 0xFF5E5CE6L, listOf("8", "11", "1", "12")),
        Playlist("p6", "Morning Coffee", "Apple Music", Mood.WARM, 0xFFAC8E68L, 0xFFFFD60AL, listOf("9", "6", "2", "13")),
        Playlist("p7", "Throwbacks", "Apple Music", Mood.WARM, 0xFFFF9F0AL, 0xFFFFD60AL, listOf("13", "8", "11", "6")),
        Playlist("p8", "Feel Good", "Apple Music Pop", Mood.SUNRISE, 0xFFFF9F0AL, 0xFFFF2D55L, listOf("5", "7", "3", "10", "4")),
    )

    // ── Curated shelves (what each screen shows) ────────────────────────────────────

    /** Big hero banner on Listen Now. */
    val featured: Playlist = playlists.first { it.id == "p3" }

    val topPicks: List<Playlist> = idsToPlaylists("p1", "p2", "p4", "p7", "p8")
    val recentlyPlayed: List<Track> = idsToSongs("1", "8", "2", "12", "11", "5")
    val madeForYou: List<Playlist> = idsToPlaylists("p4", "p5", "p6", "p1")

    val newReleases: List<Track> = idsToSongs("7", "12", "10", "3", "9", "4")
    val browseHits: List<Track> = idsToSongs("3", "4", "10", "5", "7", "6")

    val genreStations: List<Playlist> = idsToPlaylists("p1", "p4", "p7", "p8")

    val libraryRecentlyAdded: List<Track> = idsToSongs("8", "11", "12", "1", "9")
    val libraryPlaylists: List<Playlist> = idsToPlaylists("p1", "p4", "p8")

    // ── Synchronized lyrics ─────────────────────────────────────────────────────────

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
        "8" to listOf(
            LyricLine(0L, ""),
            LyricLine(8_000L, "Waiting in a car"),
            LyricLine(13_000L, "Waiting for a ride in the dark"),
            LyricLine(19_000L, "The night city grows"),
            LyricLine(25_000L, "Look and see her eyes, they glow"),
        ),
    )

    // ── Lookup helpers ────────────────────────────────────────────────────────────

    /** Returns the [Track] (song or station) with the given [id], or `null`. */
    fun getTrack(id: String): Track? = allTracks.find { it.id == id }

    /** Returns the [Playlist] with the given [id], or `null`. */
    fun getPlaylist(id: String): Playlist? = playlists.find { it.id == id }

    /** Resolves a playlist to its ordered list of playable tracks. */
    fun tracksFor(playlist: Playlist): List<Track> = playlist.trackIds.mapNotNull(::getTrack)

    /**
     * Timestamped lyrics for [trackId]; empty when none are available
     * (instrumentals, stations, etc.).
     */
    fun getLyrics(trackId: String): List<LyricLine> = lyricsMap[trackId] ?: emptyList()

    /** Case-insensitive search across songs and playlists. */
    fun searchTracks(query: String): List<Track> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return allTracks.filter {
            it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
        }
    }

    fun searchPlaylists(query: String): List<Playlist> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return playlists.filter {
            it.name.lowercase().contains(q) || it.subtitle.lowercase().contains(q)
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────────────

    private fun idsToSongs(vararg ids: String): List<Track> = ids.mapNotNull(::getTrack)
    private fun idsToPlaylists(vararg ids: String): List<Playlist> = ids.mapNotNull(::getPlaylist)
}

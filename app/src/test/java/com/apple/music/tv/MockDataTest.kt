package com.apple.music.tv

import com.apple.music.tv.data.MockData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for [MockData] — verifies catalogue integrity, lookups, and search.
 */
class MockDataTest {

    // ── Catalogue contents ───────────────────────────────────────────────────────

    @Test
    fun `recentlyPlayed is not empty`() {
        assertTrue(MockData.recentlyPlayed.isNotEmpty())
    }

    @Test
    fun `all tracks have non-blank title and artist`() {
        MockData.allTracks.forEach {
            assertTrue("title blank for ${it.id}", it.title.isNotBlank())
            assertTrue("artist blank for ${it.id}", it.artist.isNotBlank())
        }
    }

    @Test
    fun `all tracks have positive duration`() {
        MockData.allTracks.forEach { assertTrue("duration for ${it.id}", it.durationMs > 0) }
    }

    @Test
    fun `track ids are unique`() {
        val ids = MockData.allTracks.map { it.id }
        assertEquals("track ids must be unique", ids.size, ids.toSet().size)
    }

    @Test
    fun `stations are marked live`() {
        assertTrue(MockData.stations.isNotEmpty())
        MockData.stations.forEach { assertTrue("station ${it.id} should be live", it.isLive) }
    }

    // ── Playlists ─────────────────────────────────────────────────────────────────

    @Test
    fun `playlists are not empty and well formed`() {
        assertTrue(MockData.playlists.isNotEmpty())
        MockData.playlists.forEach {
            assertTrue("name blank for ${it.id}", it.name.isNotBlank())
            assertTrue("subtitle blank for ${it.id}", it.subtitle.isNotBlank())
            assertTrue("track count for ${it.id}", it.trackCount > 0)
        }
    }

    @Test
    fun `playlist ids are unique`() {
        val ids = MockData.playlists.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every playlist track id resolves to a real track`() {
        MockData.playlists.forEach { pl ->
            val resolved = MockData.tracksFor(pl)
            assertEquals(
                "playlist ${pl.id} has dangling track ids",
                pl.trackIds.size,
                resolved.size,
            )
        }
    }

    // ── Lookups ───────────────────────────────────────────────────────────────────

    @Test
    fun `getTrack returns correct track for known id`() {
        val track = MockData.getTrack("1")
        assertNotNull(track)
        assertEquals("Blinding Lights", track?.title)
    }

    @Test
    fun `getTrack returns null for unknown id`() {
        assertNull(MockData.getTrack("nope_xyz"))
    }

    @Test
    fun `getPlaylist resolves and exposes tracks`() {
        val pl = MockData.getPlaylist("p1")
        assertNotNull(pl)
        assertTrue(MockData.tracksFor(pl!!).isNotEmpty())
    }

    // ── Lyrics ────────────────────────────────────────────────────────────────────

    @Test
    fun `getLyrics returns lines for track 1 in ascending order`() {
        val lyrics = MockData.getLyrics("1")
        assertTrue(lyrics.isNotEmpty())
        var prev = -1L
        lyrics.forEach {
            assertTrue("timestamp non-negative", it.timeMs >= 0)
            assertTrue("timestamps ascending", it.timeMs >= prev)
            prev = it.timeMs
        }
    }

    @Test
    fun `getLyrics is empty for a track without lyrics`() {
        assertTrue(MockData.getLyrics("3").isEmpty())
    }

    @Test
    fun `getLyrics is empty for unknown id`() {
        assertTrue(MockData.getLyrics("unknown_xyz").isEmpty())
    }

    // ── Search ──────────────────────────────────────────────────────────────────────

    @Test
    fun `searchTracks finds all songs by an artist`() {
        val results = MockData.searchTracks("weeknd").map { it.id }
        assertTrue("expected Blinding Lights", results.contains("1"))
        assertTrue("expected Starboy", results.contains("11"))
    }

    @Test
    fun `searchTracks is case-insensitive`() {
        assertEquals(
            MockData.searchTracks("DUA").map { it.id },
            MockData.searchTracks("dua").map { it.id },
        )
    }

    @Test
    fun `searchTracks returns empty for blank query`() {
        assertTrue(MockData.searchTracks("   ").isEmpty())
    }

    @Test
    fun `searchPlaylists matches name`() {
        assertTrue(MockData.searchPlaylists("focus").any { it.id == "p4" })
    }
}

package com.apple.music.tv

import com.apple.music.tv.data.MockData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [MockData] to verify the catalogue contents and lookup helpers.
 *
 * These tests run on the JVM (no Android framework required).
 */
class MockDataTest {

    // ── recentlyPlayed ────────────────────────────────────────────────────────

    @Test
    fun `recentlyPlayed contains at least one track`() {
        assertTrue(MockData.recentlyPlayed.isNotEmpty())
    }

    @Test
    fun `all recentlyPlayed tracks have non-blank titles and artists`() {
        MockData.recentlyPlayed.forEach { track ->
            assertTrue("Title should not be blank for ${track.id}", track.title.isNotBlank())
            assertTrue("Artist should not be blank for ${track.id}", track.artist.isNotBlank())
        }
    }

    @Test
    fun `all recentlyPlayed tracks have positive duration`() {
        MockData.recentlyPlayed.forEach { track ->
            assertTrue("Duration should be positive for ${track.id}", track.durationMs > 0)
        }
    }

    @Test
    fun `all recentlyPlayed artwork URLs are non-blank`() {
        MockData.recentlyPlayed.forEach { track ->
            assertTrue("Artwork URL should not be blank for ${track.id}", track.artworkUrl.isNotBlank())
        }
    }

    @Test
    fun `recentlyPlayed track IDs are unique`() {
        val ids = MockData.recentlyPlayed.map { it.id }
        assertEquals("Track IDs must be unique", ids.size, ids.toSet().size)
    }

    // ── playlists ─────────────────────────────────────────────────────────────

    @Test
    fun `playlists contains at least one playlist`() {
        assertTrue(MockData.playlists.isNotEmpty())
    }

    @Test
    fun `all playlists have non-blank names`() {
        MockData.playlists.forEach { playlist ->
            assertTrue("Playlist name should not be blank for ${playlist.id}", playlist.name.isNotBlank())
        }
    }

    @Test
    fun `all playlists have positive track count`() {
        MockData.playlists.forEach { playlist ->
            assertTrue("Track count should be positive for ${playlist.id}", playlist.trackCount > 0)
        }
    }

    @Test
    fun `playlist IDs are unique`() {
        val ids = MockData.playlists.map { it.id }
        assertEquals("Playlist IDs must be unique", ids.size, ids.toSet().size)
    }

    // ── getTrack ──────────────────────────────────────────────────────────────

    @Test
    fun `getTrack returns correct track for known id`() {
        val track = MockData.getTrack("1")
        assertNotNull(track)
        assertEquals("1", track?.id)
        assertEquals("Blinding Lights", track?.title)
    }

    @Test
    fun `getTrack returns null for unknown id`() {
        assertNull(MockData.getTrack("nonexistent_id_xyz"))
    }

    // ── getLyrics ─────────────────────────────────────────────────────────────

    @Test
    fun `getLyrics returns non-empty list for track 1`() {
        val lyrics = MockData.getLyrics("1")
        assertTrue(lyrics.isNotEmpty())
    }

    @Test
    fun `getLyrics returns empty list for track with no lyrics`() {
        val lyrics = MockData.getLyrics("2") // track 2 has no mock lyrics
        assertTrue(lyrics.isEmpty())
    }

    @Test
    fun `getLyrics returns empty list for unknown track id`() {
        assertTrue(MockData.getLyrics("unknown_xyz").isEmpty())
    }

    @Test
    fun `lyric timestamps are non-negative and in ascending order`() {
        val lyrics = MockData.getLyrics("1")
        assertTrue(lyrics.isNotEmpty())
        var previous = -1L
        lyrics.forEach { line ->
            assertTrue("Timestamp should be non-negative", line.timeMs >= 0)
            assertTrue("Timestamps should be ascending", line.timeMs >= previous)
            previous = line.timeMs
        }
    }
}

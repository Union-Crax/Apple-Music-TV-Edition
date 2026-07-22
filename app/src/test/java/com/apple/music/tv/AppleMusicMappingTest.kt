package com.apple.music.tv

import com.apple.music.tv.data.remote.ItunesTrackDto
import com.apple.music.tv.data.remote.upscaleArtwork
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for the iTunes → domain mapping. No Android framework required.
 */
class AppleMusicMappingTest {

    private fun sampleDto() = ItunesTrackDto(
        wrapperType = "track",
        trackId = 123L,
        trackName = "As It Was",
        artistName = "Harry Styles",
        collectionName = "Harry's House",
        artworkUrl100 = "https://example.com/a/b/100x100bb.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 167_000L,
        primaryGenreName = "Pop",
    )

    @Test
    fun `upscaleArtwork rewrites the size segment`() {
        val url = "https://example.com/a/b/100x100bb.jpg"
        assertEquals("https://example.com/a/b/600x600bb.jpg", url.upscaleArtwork())
        assertEquals("https://example.com/a/b/1200x1200bb.jpg", url.upscaleArtwork(1200))
    }

    @Test
    fun `toTrack maps every field and upscales artwork`() {
        val track = sampleDto().toTrack()
        assertEquals("123", track.id)
        assertEquals("As It Was", track.title)
        assertEquals("Harry Styles", track.artist)
        assertEquals("Harry's House", track.album)
        assertEquals(167_000L, track.durationMs)
        assertEquals("https://example.com/preview.m4a", track.previewUrl)
        assertEquals("Pop", track.genre)
        assertTrue("Artwork should be upscaled to 600x600", track.artworkUrl.contains("600x600"))
        assertTrue("Track with a preview URL should be playable", track.isPlayable)
    }

    @Test
    fun `isPlayableSong requires track wrapper id name and preview`() {
        assertTrue(sampleDto().isPlayableSong())
        assertFalse(sampleDto().copy(previewUrl = null).isPlayableSong())
        assertFalse(sampleDto().copy(trackId = null).isPlayableSong())
        assertFalse(sampleDto().copy(trackName = "").isPlayableSong())
        assertFalse(sampleDto().copy(wrapperType = "collection").isPlayableSong())
    }

    @Test
    fun `toTrack falls back gracefully on missing optional fields`() {
        val minimal = ItunesTrackDto(
            wrapperType = "track",
            trackId = 9L,
            trackName = "Untitled",
            artistName = null,
            collectionName = null,
            artworkUrl100 = null,
            previewUrl = null,
            trackTimeMillis = null,
        )
        val track = minimal.toTrack()
        assertEquals("Unknown Artist", track.artist)
        assertEquals("Untitled", track.album)
        assertEquals(30_000L, track.durationMs)
        assertFalse(track.isPlayable)
    }
}

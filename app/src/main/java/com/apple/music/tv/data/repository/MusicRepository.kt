package com.apple.music.tv.data.repository

import com.apple.music.tv.data.model.LyricLine
import com.apple.music.tv.data.model.Playlist
import com.apple.music.tv.data.model.Track

/**
 * Single source of truth for catalogue content.
 *
 * Implementations fetch real data from Apple's catalogue and gracefully fall back to
 * bundled content when offline, so callers never have to worry about the transport.
 */
interface MusicRepository {

    /** Fetches a shelf of songs matching [term] (a genre, mood, or curated query). */
    suspend fun getShelf(term: String, limit: Int = 24): List<Track>

    /** Full-text catalogue search for the Search screen. */
    suspend fun search(query: String, limit: Int = 25): List<Track>

    /** The set of curated mixes shown on the dashboard. */
    fun featuredPlaylists(): List<Playlist>

    /** Loads the tracks that make up [playlist] (used as a playback queue). */
    suspend fun getPlaylistTracks(playlist: Playlist): List<Track>

    /** Synchronized lyrics for [track], or an empty list when unavailable. */
    fun getLyrics(track: Track): List<LyricLine>
}

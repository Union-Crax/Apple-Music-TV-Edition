package com.apple.music.tv.data.repository

import android.util.Log
import com.apple.music.tv.data.MockData
import com.apple.music.tv.data.model.LyricLine
import com.apple.music.tv.data.model.Playlist
import com.apple.music.tv.data.model.Track
import com.apple.music.tv.data.remote.AppleMusicApi
import com.apple.music.tv.data.remote.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Default [MusicRepository] backed by Apple's public iTunes catalogue.
 *
 * Every network call is wrapped so a failure (offline, timeout, rate-limit) degrades
 * gracefully to bundled [MockData] instead of throwing — the UI always has something
 * to show. All network work runs on [Dispatchers.IO].
 */
class AppleMusicRepository(
    private val api: AppleMusicApi = NetworkModule.api,
) : MusicRepository {

    override suspend fun getShelf(term: String, limit: Int): List<Track> =
        withContext(Dispatchers.IO) {
            runCatching {
                api.search(term = term, limit = limit)
                    .results
                    .filter { it.isPlayableSong() }
                    .map { it.toTrack() }
                    .distinctBy { it.id }
            }.getOrElse { error ->
                Log.w(TAG, "getShelf('$term') failed, using fallback: ${error.message}")
                MockData.recentlyPlayed
            }.ifEmpty { MockData.recentlyPlayed }
        }

    override suspend fun search(query: String, limit: Int): List<Track> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) return@withContext emptyList()
            runCatching {
                api.search(term = query, limit = limit)
                    .results
                    .filter { it.isPlayableSong() }
                    .map { it.toTrack() }
                    .distinctBy { it.id }
            }.getOrElse { error ->
                Log.w(TAG, "search('$query') failed: ${error.message}")
                emptyList()
            }
        }

    override fun featuredPlaylists(): List<Playlist> = MockData.playlists

    override suspend fun getPlaylistTracks(playlist: Playlist): List<Track> =
        getShelf(term = playlist.term, limit = 30)

    override fun getLyrics(track: Track): List<LyricLine> =
        MockData.getLyricsForTitle(track.title)

    private companion object {
        const val TAG = "AppleMusicRepository"
    }
}

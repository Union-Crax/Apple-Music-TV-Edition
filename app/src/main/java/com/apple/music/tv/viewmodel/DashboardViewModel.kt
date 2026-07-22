package com.apple.music.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.music.tv.data.model.Playlist
import com.apple.music.tv.data.model.Track
import com.apple.music.tv.data.repository.MusicRepository
import com.apple.music.tv.di.ServiceLocator
import com.apple.music.tv.playback.PlayerController
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** A horizontally-scrolling row of tracks on the dashboard. */
data class Shelf(val title: String, val tracks: List<Track>)

/** Immutable UI state for the dashboard. */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val hero: Track? = null,
    val shelves: List<Shelf> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val errorMessage: String? = null,
) {
    /** All tracks in row order — used to build a continuous "play everything" queue. */
    val allTracks: List<Track> get() = shelves.flatMap { it.tracks }
}

/**
 * Loads the home dashboard's content from the [MusicRepository].
 *
 * Each shelf is fetched concurrently; the first shelf's first track becomes the hero
 * banner. Failures degrade gracefully because the repository itself falls back to
 * bundled data, so [DashboardUiState.errorMessage] is only set when everything is empty.
 */
class DashboardViewModel(
    private val repository: MusicRepository = ServiceLocator.repository,
    private val player: PlayerController = ServiceLocator.player,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() = load()

    /** Starts playback of [tracks] beginning at [startIndex] (a shelf/hero selection). */
    fun play(tracks: List<Track>, startIndex: Int) {
        player.playQueue(tracks, startIndex)
    }

    /** Loads a playlist's tracks and begins playing them as a queue. */
    fun playPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            val tracks = repository.getPlaylistTracks(playlist)
            if (tracks.isNotEmpty()) player.playQueue(tracks, 0)
        }
    }

    private fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val shelves = SHELF_DEFINITIONS.map { (title, term) ->
                async { Shelf(title, repository.getShelf(term = term, limit = 20)) }
            }.awaitAll().filter { it.tracks.isNotEmpty() }

            _uiState.value = DashboardUiState(
                isLoading = false,
                hero = shelves.firstOrNull()?.tracks?.firstOrNull(),
                shelves = shelves,
                playlists = repository.featuredPlaylists(),
                errorMessage = if (shelves.isEmpty()) {
                    "Couldn't load music. Check your connection and try again."
                } else {
                    null
                },
            )
        }
    }

    private companion object {
        /** Ordered (title → Apple Music search term) shelves shown on the home screen. */
        val SHELF_DEFINITIONS = listOf(
            "Top Charts" to "top hits 2024",
            "New Releases" to "new music",
            "Pop Right Now" to "pop hits",
            "Hip-Hop & R&B" to "hip hop rnb",
            "Chill Mix" to "chill acoustic",
            "Throwback Classics" to "greatest hits classics",
        )
    }
}

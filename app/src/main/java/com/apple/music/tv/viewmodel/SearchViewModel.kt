package com.apple.music.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.music.tv.data.model.Track
import com.apple.music.tv.data.repository.MusicRepository
import com.apple.music.tv.di.ServiceLocator
import com.apple.music.tv.playback.PlayerController
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** UI state for the Search screen. */
data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Track> = emptyList(),
    val hasSearched: Boolean = false,
)

/**
 * Drives the Search screen: debounces the query, runs the catalogue search, and
 * exposes results. Suggested "quick search" chips are surfaced via [suggestions].
 */
@OptIn(FlowPreview::class)
class SearchViewModel(
    private val repository: MusicRepository = ServiceLocator.repository,
    private val player: PlayerController = ServiceLocator.player,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    val suggestions: List<String> = listOf(
        "Taylor Swift", "The Weeknd", "Drake", "Billie Eilish",
        "Bad Bunny", "Dua Lipa", "Kendrick Lamar", "SZA",
    )

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(350)
                .distinctUntilChanged()
                .collect { runSearch(it) }
        }
    }

    /** Called on every keystroke; the actual request is debounced. */
    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        queryFlow.value = query
    }

    /** Search for [term] (e.g. tapping a suggestion chip). */
    fun searchNow(term: String) {
        _uiState.value = _uiState.value.copy(query = term)
        queryFlow.value = term
    }

    /** Plays the current results starting at [startIndex]. */
    fun play(startIndex: Int) {
        player.playQueue(_uiState.value.results, startIndex)
    }

    private suspend fun runSearch(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                results = emptyList(),
                hasSearched = false,
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, hasSearched = true)
        val results = repository.search(query = query, limit = 30)
        _uiState.value = _uiState.value.copy(isLoading = false, results = results)
    }
}

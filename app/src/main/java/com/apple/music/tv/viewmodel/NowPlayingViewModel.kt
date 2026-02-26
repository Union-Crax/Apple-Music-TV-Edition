package com.apple.music.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.music.tv.data.MockData
import com.apple.music.tv.data.model.LyricLine
import com.apple.music.tv.data.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel for the Now Playing screen.
 *
 * Manages mock playback state: the currently loaded track, a running timer that
 * advances [currentPositionMs], and a play / pause toggle.  No real audio
 * playback is wired up in this starter implementation.
 */
class NowPlayingViewModel : ViewModel() {

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics: StateFlow<List<LyricLine>> = _lyrics.asStateFlow()

    private var playbackJob: Job? = null

    /**
     * Loads the track identified by [trackId] from [MockData] and starts playback
     * from the beginning.
     */
    fun loadTrack(trackId: String) {
        val track = MockData.getTrack(trackId) ?: MockData.recentlyPlayed.first()
        _currentTrack.value = track
        _currentPositionMs.value = 0L
        _lyrics.value = MockData.getLyrics(trackId)
        startPlayback(track.durationMs)
    }

    /** Toggles between playing and paused states. */
    fun togglePlayPause() {
        val track = _currentTrack.value ?: return
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback(track.durationMs)
        }
    }

    /** Seeks to the given absolute position (clamped to track duration). */
    fun seekTo(positionMs: Long) {
        val duration = _currentTrack.value?.durationMs ?: return
        _currentPositionMs.value = positionMs.coerceIn(0L, duration)
    }

    private fun startPlayback(durationMs: Long) {
        playbackJob?.cancel()
        _isPlaying.value = true
        playbackJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_MS)
                val newPosition = _currentPositionMs.value + TICK_MS
                if (newPosition >= durationMs) {
                    // Loop back to the beginning
                    _currentPositionMs.value = 0L
                } else {
                    _currentPositionMs.value = newPosition
                }
            }
        }
    }

    private fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }

    companion object {
        /** Timer resolution: advance playback position every 100 ms. */
        private const val TICK_MS = 100L
    }
}

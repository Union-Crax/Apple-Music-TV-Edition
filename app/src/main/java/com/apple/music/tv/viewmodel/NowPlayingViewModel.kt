package com.apple.music.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.music.tv.data.model.LyricLine
import com.apple.music.tv.data.model.Track
import com.apple.music.tv.data.repository.MusicRepository
import com.apple.music.tv.di.ServiceLocator
import com.apple.music.tv.playback.PlayerController
import com.apple.music.tv.playback.RepeatMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Now Playing screen.
 *
 * This is a thin adapter over the process-wide [PlayerController]: it re-exposes the
 * player's real playback state (position, buffering, queue, shuffle/repeat) and derives
 * synchronized lyrics for whatever track is currently playing. No playback state is
 * duplicated here, so Now Playing, the mini-player, and any other screen stay in sync.
 */
class NowPlayingViewModel(
    private val player: PlayerController = ServiceLocator.player,
    private val repository: MusicRepository = ServiceLocator.repository,
) : ViewModel() {

    val currentTrack: StateFlow<Track?> = player.currentTrack
    val isPlaying: StateFlow<Boolean> = player.isPlaying
    val isBuffering: StateFlow<Boolean> = player.isBuffering
    val positionMs: StateFlow<Long> = player.positionMs
    val durationMs: StateFlow<Long> = player.durationMs
    val queue: StateFlow<List<Track>> = player.queue
    val currentIndex: StateFlow<Int> = player.currentIndex
    val shuffle: StateFlow<Boolean> = player.shuffle
    val repeatMode: StateFlow<RepeatMode> = player.repeatMode

    /** Synchronized lyrics for the currently playing track (empty when unavailable). */
    val lyrics: StateFlow<List<LyricLine>> = player.currentTrack
        .map { track -> track?.let { repository.getLyrics(it) } ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun togglePlayPause() = player.togglePlayPause()
    fun next() = player.next()
    fun previous() = player.previous()
    fun seekTo(positionMs: Long) = player.seekTo(positionMs)
    fun seekBy(deltaMs: Long) = player.seekBy(deltaMs)
    fun toggleShuffle() = player.toggleShuffle()
    fun cycleRepeat() = player.cycleRepeatMode()
    fun playIndex(index: Int) = player.playIndex(index)
}

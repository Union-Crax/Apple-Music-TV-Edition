package com.apple.music.tv.playback

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.music.tv.audio.ToneAudioEngine
import com.apple.music.tv.data.MockData
import com.apple.music.tv.data.model.LyricLine
import com.apple.music.tv.data.model.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/** How the queue repeats. */
enum class RepeatMode { OFF, ALL, ONE }

/**
 * Single source of truth for everything playback-related.
 *
 * It is scoped to the [com.apple.music.tv.MainActivity] so the same instance is
 * shared by the browse shell (mini Now Playing bar) and the full Now Playing
 * screen. It owns the [ToneAudioEngine], the queue, and the running position
 * clock, and exposes immutable [StateFlow]s for the UI to observe.
 */
class PlayerViewModel : ViewModel() {

    private val engine = ToneAudioEngine()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _index = MutableStateFlow(-1)
    val index: StateFlow<Int> = _index.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _shuffle = MutableStateFlow(false)
    val shuffle: StateFlow<Boolean> = _shuffle.asStateFlow()

    private val _repeat = MutableStateFlow(RepeatMode.OFF)
    val repeat: StateFlow<RepeatMode> = _repeat.asStateFlow()

    private val _volume = MutableStateFlow(0.7f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    /** The currently loaded track, or `null` if nothing has been played yet. */
    val currentTrack: StateFlow<Track?> =
        combine(_queue, _index) { q, i -> q.getOrNull(i) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Lyrics for the current track (empty when none). */
    val lyrics: StateFlow<List<LyricLine>> =
        combine(_queue, _index) { q, i -> q.getOrNull(i)?.let { MockData.getLyrics(it.id) } ?: emptyList() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var lastTickAt = 0L

    init {
        engine.setVolume(_volume.value)
        // A single position clock that advances while playing.
        viewModelScope.launch {
            lastTickAt = SystemClock.elapsedRealtime()
            while (isActive) {
                delay(POSITION_TICK_MS)
                val now = SystemClock.elapsedRealtime()
                val elapsed = now - lastTickAt
                lastTickAt = now
                if (!_isPlaying.value) continue
                val track = currentTrack.value ?: continue
                val next = _positionMs.value + elapsed
                when {
                    track.isLive -> _positionMs.value = next % track.durationMs.coerceAtLeast(1L)
                    next >= track.durationMs -> advance(auto = true)
                    else -> _positionMs.value = next
                }
            }
        }
    }

    // ── Commands ────────────────────────────────────────────────────────────────

    /** Replace the queue with [tracks] and start at [startIndex]. */
    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        _queue.value = tracks
        loadIndex(startIndex.coerceIn(0, tracks.lastIndex), restart = true)
    }

    /** Convenience for playing a single track in its own one-item queue. */
    fun playTrack(track: Track) = playQueue(listOf(track), 0)

    /** If [track] is already in the current queue, jump to it; otherwise start fresh. */
    fun playFrom(tracks: List<Track>, track: Track) {
        val i = tracks.indexOfFirst { it.id == track.id }.takeIf { it >= 0 } ?: 0
        playQueue(tracks, i)
    }

    fun togglePlay() {
        if (currentTrack.value == null) {
            // Nothing loaded yet → start a sensible default.
            playQueue(MockData.recentlyPlayed, 0)
            return
        }
        if (_isPlaying.value) {
            _isPlaying.value = false
            engine.pause()
        } else {
            _isPlaying.value = true
            engine.play()
        }
    }

    fun next() = advance(auto = false)

    fun previous() {
        val q = _queue.value
        if (q.isEmpty()) return
        if (_positionMs.value > RESTART_THRESHOLD_MS) {
            seekTo(0L)
            return
        }
        val target = when {
            _shuffle.value && q.size > 1 -> randomOther(_index.value, q.size)
            _index.value > 0 -> _index.value - 1
            else -> q.lastIndex
        }
        loadIndex(target, restart = true)
    }

    fun seekTo(positionMs: Long) {
        val dur = currentTrack.value?.durationMs ?: return
        _positionMs.value = positionMs.coerceIn(0L, dur)
        lastTickAt = SystemClock.elapsedRealtime()
    }

    fun toggleShuffle() { _shuffle.value = !_shuffle.value }

    fun cycleRepeat() {
        _repeat.value = when (_repeat.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun setVolume(v: Float) {
        _volume.value = v.coerceIn(0f, 1f)
        engine.setVolume(_volume.value)
    }

    /** Pause without releasing the engine (e.g. when the activity stops). */
    fun pauseForBackground() {
        if (_isPlaying.value) {
            _isPlaying.value = false
            engine.pause()
        }
    }

    // ── Internals ───────────────────────────────────────────────────────────────

    private fun advance(auto: Boolean) {
        val q = _queue.value
        if (q.isEmpty()) return

        if (auto && _repeat.value == RepeatMode.ONE) {
            seekTo(0L)
            return
        }
        val current = _index.value
        val target: Int = when {
            _shuffle.value && q.size > 1 -> randomOther(current, q.size)
            current + 1 < q.size -> current + 1
            _repeat.value == RepeatMode.ALL -> 0
            auto -> {
                // Reached the natural end of the queue.
                _isPlaying.value = false
                engine.pause()
                _positionMs.value = currentTrack.value?.durationMs ?: 0L
                return
            }
            else -> 0
        }
        loadIndex(target, restart = true)
    }

    private fun loadIndex(i: Int, restart: Boolean) {
        _index.value = i
        if (restart) _positionMs.value = 0L
        lastTickAt = SystemClock.elapsedRealtime()
        val track = _queue.value.getOrNull(i) ?: return
        engine.setMood(track.mood)
        _isPlaying.value = true
        engine.play()
    }

    private fun randomOther(current: Int, size: Int): Int {
        if (size <= 1) return 0
        var r = current
        while (r == current) r = Random.nextInt(size)
        return r
    }

    override fun onCleared() {
        super.onCleared()
        engine.release()
    }

    companion object {
        private const val POSITION_TICK_MS = 200L
        private const val RESTART_THRESHOLD_MS = 3_000L
    }
}

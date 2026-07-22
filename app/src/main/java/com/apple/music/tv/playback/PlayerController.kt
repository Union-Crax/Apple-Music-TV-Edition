package com.apple.music.tv.playback

import android.content.Context
import android.net.Uri
import androidx.annotation.MainThread
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.apple.music.tv.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Repeat behaviour exposed to the UI, mirroring ExoPlayer's repeat modes. */
enum class RepeatMode { OFF, ALL, ONE }

/**
 * Thin, lifecycle-friendly wrapper around a single [ExoPlayer] instance that plays the
 * real 30-second Apple Music preview streams.
 *
 * A single instance lives for the whole process (see
 * [com.apple.music.tv.di.ServiceLocator]) so playback continues seamlessly as the user
 * moves between the dashboard, search, and Now Playing screens. All state the UI needs
 * is surfaced through cold-safe [StateFlow]s that mirror the player's callbacks.
 *
 * Every public method must be called from the main thread (ExoPlayer's requirement);
 * Compose already invokes them from the main thread.
 */
@OptIn(UnstableApi::class)
class PlayerController(context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()

    // ── Observable state ────────────────────────────────────────────────────────

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _shuffle = MutableStateFlow(false)
    val shuffle: StateFlow<Boolean> = _shuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _isBuffering.value = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    _durationMs.value = player.duration.coerceAtLeast(0L)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncCurrentTrack()
            }
        })

        // Poll playback position so the progress bar and lyrics stay in sync.
        scope.launch {
            while (true) {
                if (player.isPlaying) {
                    _positionMs.value = player.currentPosition.coerceAtLeast(0L)
                    val dur = player.duration
                    if (dur > 0L) _durationMs.value = dur
                }
                delay(POSITION_POLL_MS)
            }
        }
    }

    // ── Commands ────────────────────────────────────────────────────────────────

    /**
     * Replaces the queue with [tracks] (only playable ones are kept) and starts
     * playback at the track that was originally at [startIndex].
     */
    @MainThread
    fun playQueue(tracks: List<Track>, startIndex: Int) {
        val playable = tracks.filter { it.isPlayable }
        if (playable.isEmpty()) return

        // Translate the requested index into the filtered list.
        val requested = tracks.getOrNull(startIndex)
        val resolvedIndex = playable.indexOfFirst { it.id == requested?.id }.coerceAtLeast(0)

        _queue.value = playable
        player.setMediaItems(playable.map { it.toMediaItem() }, resolvedIndex, 0L)
        player.prepare()
        player.playWhenReady = true
        _currentIndex.value = resolvedIndex
        _currentTrack.value = playable[resolvedIndex]
        _positionMs.value = 0L
    }

    @MainThread
    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    @MainThread
    fun next() {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
        else player.seekTo(0, 0L)
    }

    @MainThread
    fun previous() {
        // Match the familiar behaviour: restart the track unless we're near the start.
        if (player.currentPosition > RESTART_THRESHOLD_MS || !player.hasPreviousMediaItem()) {
            player.seekTo(player.currentMediaItemIndex, 0L)
        } else {
            player.seekToPreviousMediaItem()
        }
    }

    @MainThread
    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L)))
        _positionMs.value = positionMs
    }

    /** Seeks by [deltaMs] relative to the current position (D-pad left/right scrub). */
    @MainThread
    fun seekBy(deltaMs: Long) = seekTo(player.currentPosition + deltaMs)

    @MainThread
    fun toggleShuffle() {
        val enabled = !player.shuffleModeEnabled
        player.shuffleModeEnabled = enabled
        _shuffle.value = enabled
    }

    @MainThread
    fun cycleRepeatMode() {
        val next = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        player.repeatMode = when (next) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        _repeatMode.value = next
    }

    /** Jumps to an arbitrary [index] in the current queue (used by the up-next list). */
    @MainThread
    fun playIndex(index: Int) {
        if (index in _queue.value.indices) {
            player.seekTo(index, 0L)
            player.play()
        }
    }

    fun release() {
        player.release()
    }

    // ── Internals ───────────────────────────────────────────────────────────────

    private fun syncCurrentTrack() {
        val idx = player.currentMediaItemIndex
        _currentIndex.value = idx
        _currentTrack.value = _queue.value.getOrNull(idx)
        _positionMs.value = 0L
    }

    private fun Track.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId(id)
            .setUri(previewUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(runCatching { Uri.parse(artworkUrl) }.getOrNull())
                    .build(),
            )
            .build()

    private companion object {
        const val POSITION_POLL_MS = 250L
        const val RESTART_THRESHOLD_MS = 3_000L
    }
}

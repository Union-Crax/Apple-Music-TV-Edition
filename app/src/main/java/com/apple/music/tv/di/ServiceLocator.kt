package com.apple.music.tv.di

import android.content.Context
import com.apple.music.tv.data.repository.AppleMusicRepository
import com.apple.music.tv.data.repository.MusicRepository
import com.apple.music.tv.playback.PlayerController

/**
 * Ultra-light manual dependency container.
 *
 * The app deliberately avoids a DI framework (Hilt/Dagger) to stay approachable. A
 * single process-wide [PlayerController] and [MusicRepository] are created once from
 * [com.apple.music.tv.AppleMusicTvApp] and shared by every ViewModel.
 */
object ServiceLocator {

    @Volatile
    private var _repository: MusicRepository? = null

    @Volatile
    private var _player: PlayerController? = null

    val repository: MusicRepository
        get() = _repository ?: error("ServiceLocator.init() not called")

    val player: PlayerController
        get() = _player ?: error("ServiceLocator.init() not called")

    /** Idempotently initialises the singletons; safe to call once from Application. */
    @Synchronized
    fun init(context: Context) {
        if (_repository == null) _repository = AppleMusicRepository()
        if (_player == null) _player = PlayerController(context.applicationContext)
    }
}

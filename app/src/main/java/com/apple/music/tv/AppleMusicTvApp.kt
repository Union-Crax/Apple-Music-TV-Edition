package com.apple.music.tv

import android.app.Application
import com.apple.music.tv.di.ServiceLocator

/**
 * Application entry point.
 *
 * Initialises the process-wide [ServiceLocator] (repository + ExoPlayer-backed
 * player controller) before any Activity or ViewModel is created.
 */
class AppleMusicTvApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}

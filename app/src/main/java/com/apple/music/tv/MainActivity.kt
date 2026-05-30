package com.apple.music.tv

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.apple.music.tv.playback.PlayerViewModel
import com.apple.music.tv.ui.AppRoot
import com.apple.music.tv.ui.theme.AppleMusicTVTheme

/**
 * Single-activity entry point.
 *
 * The [PlayerViewModel] is created at activity scope so a single playback engine
 * and queue are shared across the whole UI. Hardware media keys (and TV remote
 * transport keys) are routed straight to it via [dispatchKeyEvent].
 */
class MainActivity : ComponentActivity() {

    private val player: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppleMusicTVTheme {
                AppRoot(player)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Don't keep generating audio when the app is no longer visible.
        player.pauseForBackground()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                KeyEvent.KEYCODE_MEDIA_PLAY,
                KeyEvent.KEYCODE_MEDIA_PAUSE -> { player.togglePlay(); return true }
                KeyEvent.KEYCODE_MEDIA_NEXT -> { player.next(); return true }
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> { player.previous(); return true }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}

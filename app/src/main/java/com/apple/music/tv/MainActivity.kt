package com.apple.music.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.apple.music.tv.navigation.AppNavigation
import com.apple.music.tv.ui.theme.AppleMusicTVTheme

/**
 * Single-activity entry point for the Apple Music TV app.
 *
 * All navigation and UI is handled inside the Jetpack Compose composition tree
 * rooted at [AppNavigation].  The activity itself is kept minimal to avoid
 * coupling lifecycle concerns to the UI layer.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppleMusicTVTheme {
                AppNavigation()
            }
        }
    }
}

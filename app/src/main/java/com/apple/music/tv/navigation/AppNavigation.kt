package com.apple.music.tv.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apple.music.tv.ui.screens.MainScaffold
import com.apple.music.tv.ui.screens.NowPlayingScreen

/**
 * Root [NavHost] wiring the home shell (Home + Search tabs) to the full-screen
 * Now Playing destination.
 *
 * The nav-controller is created here so it is scoped to the whole graph and survives
 * configuration changes together with the activity.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable(Screen.Home.route) {
            MainScaffold(
                onOpenNowPlaying = { navController.navigate(Screen.NowPlaying.route) },
            )
        }

        composable(Screen.NowPlaying.route) {
            NowPlayingScreen(onBack = { navController.popBackStack() })
        }
    }
}

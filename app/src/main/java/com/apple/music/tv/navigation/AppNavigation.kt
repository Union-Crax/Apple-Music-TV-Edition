package com.apple.music.tv.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apple.music.tv.ui.screens.DashboardScreen
import com.apple.music.tv.ui.screens.NowPlayingScreen

/**
 * Root [NavHost] that wires together the Dashboard and Now Playing destinations.
 *
 * The nav-controller is created here so it is scoped to the full navigation graph
 * (i.e. survives configuration changes together with the activity).
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onTrackClick = { trackId ->
                    navController.navigate(Screen.NowPlaying.createRoute(trackId))
                },
            )
        }

        composable(
            route = Screen.NowPlaying.route,
            arguments = listOf(
                navArgument(Screen.NowPlaying.ARG_TRACK_ID) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments
                ?.getString(Screen.NowPlaying.ARG_TRACK_ID)
                .orEmpty()

            NowPlayingScreen(
                trackId = trackId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

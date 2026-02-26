package com.apple.music.tv.navigation

/** All navigable destinations in the app. */
sealed class Screen(val route: String) {

    /** Main dashboard showing Recently Played and Playlists grids. */
    object Dashboard : Screen("dashboard")

    /** Full-screen Now Playing view for a specific track. */
    object NowPlaying : Screen("now_playing/{trackId}") {
        const val ARG_TRACK_ID = "trackId"

        /** Builds the concrete navigation route for a given [trackId]. */
        fun createRoute(trackId: String): String = "now_playing/$trackId"
    }
}

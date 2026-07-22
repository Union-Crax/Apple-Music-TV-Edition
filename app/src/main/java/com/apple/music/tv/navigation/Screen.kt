package com.apple.music.tv.navigation

/** All navigable destinations in the app. */
sealed class Screen(val route: String) {

    /** Root shell hosting the Home / Search tabs and the persistent mini-player. */
    object Home : Screen("home")

    /**
     * Full-screen Now Playing view.
     *
     * The screen reflects whatever the shared player is currently playing, so it needs
     * no arguments — selecting a track elsewhere sets the queue before navigating here.
     */
    object NowPlaying : Screen("now_playing")
}

/** Top-level tabs shown in the home navigation bar. */
enum class HomeTab(val label: String) {
    HOME("Home"),
    SEARCH("Search"),
}

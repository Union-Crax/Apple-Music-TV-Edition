package com.apple.music.tv.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apple.music.tv.navigation.HomeTab
import com.apple.music.tv.ui.components.MiniPlayer
import com.apple.music.tv.ui.components.TopNavBar
import com.apple.music.tv.ui.theme.BackgroundDark
import com.apple.music.tv.viewmodel.NowPlayingViewModel

/**
 * Root shell for the app's home experience.
 *
 * Lays out (top → bottom): the [TopNavBar] with Home / Search tabs, the selected tab's
 * content, and a persistent [MiniPlayer] that slides up whenever something is playing.
 * The mini-player and Now Playing screen share the same underlying [NowPlayingViewModel]
 * state, so playback stays perfectly in sync across the app.
 */
@Composable
fun MainScaffold(
    onOpenNowPlaying: () -> Unit,
    playerViewModel: NowPlayingViewModel = viewModel(),
) {
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    val currentTrack by playerViewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
    val positionMs by playerViewModel.positionMs.collectAsStateWithLifecycle()
    val durationMs by playerViewModel.durationMs.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
    ) {
        TopNavBar(selected = selectedTab, onSelect = { selectedTab = it })

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                HomeTab.HOME -> DashboardScreen(onOpenNowPlaying = onOpenNowPlaying)
                HomeTab.SEARCH -> SearchScreen(onOpenNowPlaying = onOpenNowPlaying)
            }
        }

        AnimatedVisibility(
            visible = currentTrack != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            currentTrack?.let { track ->
                MiniPlayer(
                    track = track,
                    isPlaying = isPlaying,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    onExpand = onOpenNowPlaying,
                    onTogglePlay = playerViewModel::togglePlayPause,
                    onNext = playerViewModel::next,
                )
            }
        }
    }
}

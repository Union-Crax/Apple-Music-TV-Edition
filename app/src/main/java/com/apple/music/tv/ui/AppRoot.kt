package com.apple.music.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apple.music.tv.data.MockData
import com.apple.music.tv.playback.PlayerViewModel
import com.apple.music.tv.ui.components.FocusableItem
import com.apple.music.tv.ui.components.NowPlayingBar
import com.apple.music.tv.ui.screens.BrowseScreen
import com.apple.music.tv.ui.screens.NowPlayingScreen
import com.apple.music.tv.ui.screens.SearchScreen
import com.apple.music.tv.ui.theme.AppleMusicRed
import com.apple.music.tv.ui.theme.Black
import com.apple.music.tv.ui.theme.ContentBackground
import com.apple.music.tv.ui.theme.Elevated
import com.apple.music.tv.ui.theme.SidebarBackground
import com.apple.music.tv.ui.theme.TextPrimary
import com.apple.music.tv.ui.theme.TextSecondary

/** Primary destinations, shown in the left navigation rail. */
enum class Section(val label: String, val icon: ImageVector) {
    LISTEN_NOW("Listen Now", Icons.Rounded.PlayCircle),
    BROWSE("Browse", Icons.Rounded.GridView),
    RADIO("Radio", Icons.Rounded.Radio),
    LIBRARY("Library", Icons.Rounded.LibraryMusic),
    SEARCH("Search", Icons.Rounded.Search),
}

/**
 * Root of the experience: a persistent Apple Music–style left sidebar, the active
 * section's content, and a pinned Now Playing bar. The immersive full-screen Now
 * Playing view slides up over everything when expanded.
 *
 * Playback state lives in the activity-scoped [player], so audio continues
 * seamlessly as the user moves between browsing and the full player.
 */
@Composable
fun AppRoot(player: PlayerViewModel) {
    var section by remember { mutableStateOf(Section.LISTEN_NOW) }
    var fullScreen by remember { mutableStateOf(false) }

    val currentTrack by player.currentTrack.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Black)) {

        Row(modifier = Modifier.fillMaxSize()) {

            Sidebar(
                selected = section,
                onSelect = { section = it },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(ContentBackground),
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (section) {
                        Section.SEARCH -> SearchScreen(player = player)
                        else -> BrowseScreen(
                            section = section,
                            player = player,
                            onOpenNowPlaying = { fullScreen = true },
                        )
                    }
                }

                if (currentTrack != null) {
                    NowPlayingBar(
                        player = player,
                        onExpand = { fullScreen = true },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = fullScreen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            BackHandler(enabled = true) { fullScreen = false }
            NowPlayingScreen(
                player = player,
                onCollapse = { fullScreen = false },
            )
        }
    }
}

// ── Sidebar ─────────────────────────────────────────────────────────────────────

private val SidebarWidth = 248.dp

@Composable
private fun Sidebar(
    selected: Section,
    onSelect: (Section) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(SidebarWidth)
            .fillMaxHeight()
            .background(SidebarBackground)
            .padding(horizontal = 14.dp, vertical = 22.dp),
    ) {
        // Brand
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, bottom = 26.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        Brush.linearGradient(listOf(AppleMusicRed, Color(0xFFFF5E7E))),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text("Music", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }

        Section.values().forEach { item ->
            NavRailItem(
                section = item,
                selected = item == selected,
                onClick = { onSelect(item) },
            )
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(26.dp))
        Text(
            text = "PLAYLISTS",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
        )
        MockData.playlists.take(5).forEach { pl ->
            PlaylistRailItem(name = pl.name, colorStart = pl.colorStart, colorEnd = pl.colorEnd)
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun NavRailItem(
    section: Section,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FocusableItem(onClick = onClick, modifier = Modifier.fillMaxWidth()) { focused ->
        val bg = when {
            focused -> AppleMusicRed
            selected -> Elevated
            else -> Color.Transparent
        }
        val tint = when {
            focused -> Color.White
            selected -> AppleMusicRed
            else -> TextSecondary
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(bg, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 11.dp),
        ) {
            Icon(section.icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            Text(
                text = section.label,
                color = if (focused) Color.White else TextPrimary,
                fontSize = 17.sp,
                fontWeight = if (selected || focused) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun PlaylistRailItem(name: String, colorStart: Long, colorEnd: Long) {
    FocusableItem(onClick = { }, modifier = Modifier.fillMaxWidth()) { focused ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(if (focused) Elevated else Color.Transparent, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(colorStart), Color(colorEnd))),
                        RoundedCornerShape(5.dp),
                    ),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = name,
                color = if (focused) TextPrimary else TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

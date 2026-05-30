package com.apple.music.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import com.apple.music.tv.data.MockData
import com.apple.music.tv.data.model.Playlist
import com.apple.music.tv.data.model.Track
import com.apple.music.tv.playback.PlayerViewModel
import com.apple.music.tv.ui.Section
import com.apple.music.tv.ui.components.Artwork
import com.apple.music.tv.ui.components.FocusableItem
import com.apple.music.tv.ui.components.PlaylistCard
import com.apple.music.tv.ui.components.TrackCard
import com.apple.music.tv.ui.theme.TextPrimary
import com.apple.music.tv.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val EdgePad = 40.dp

/**
 * The browsing surface for every non-search [Section]. Renders a hero banner
 * (Listen Now only) followed by a vertical stack of horizontally scrolling
 * shelves, exactly like Apple Music's 10-foot UI.
 */
@Composable
fun BrowseScreen(
    section: Section,
    player: PlayerViewModel,
    onOpenNowPlaying: () -> Unit,
) {
    TvLazyColumn(
        contentPadding = PaddingValues(top = 28.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        item { Header(section) }

        when (section) {
            Section.LISTEN_NOW -> {
                item {
                    Hero(
                        playlist = MockData.featured,
                        onPlay = {
                            player.playQueue(MockData.tracksFor(MockData.featured))
                            onOpenNowPlaying()
                        },
                    )
                }
                playlistShelf("Top Picks for You", MockData.topPicks, player, onOpenNowPlaying)
                trackShelf("Recently Played", MockData.recentlyPlayed, player, onOpenNowPlaying)
                playlistShelf("Made for You", MockData.madeForYou, player, onOpenNowPlaying)
            }
            Section.BROWSE -> {
                trackShelf("New Releases", MockData.newReleases, player, onOpenNowPlaying)
                trackShelf("Hits Right Now", MockData.browseHits, player, onOpenNowPlaying)
                playlistShelf("Made for You", MockData.madeForYou, player, onOpenNowPlaying)
            }
            Section.RADIO -> {
                trackShelf("Apple Music Radio", MockData.stations, player, onOpenNowPlaying)
                playlistShelf("Stations by Genre", MockData.genreStations, player, onOpenNowPlaying)
            }
            Section.LIBRARY -> {
                trackShelf("Recently Added", MockData.libraryRecentlyAdded, player, onOpenNowPlaying)
                playlistShelf("Your Playlists", MockData.libraryPlaylists, player, onOpenNowPlaying)
            }
            Section.SEARCH -> Unit // handled by SearchScreen
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun Header(section: Section) {
    Column(modifier = Modifier.padding(horizontal = EdgePad)) {
        if (section == Section.LISTEN_NOW) {
            val today = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }
            Text(
                text = today.uppercase(Locale.getDefault()),
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
        }
        Text(
            text = section.label,
            color = TextPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────

@Composable
private fun Hero(playlist: Playlist, onPlay: () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = EdgePad)) {
        FocusableItem(onClick = onPlay, modifier = Modifier.fillMaxWidth()) { focused ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (focused) 304.dp else 296.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(playlist.colorStart), Color(playlist.colorEnd)))),
            ) {
                // Legibility scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(304.dp)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.55f),
                            ),
                        ),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(28.dp),
                ) {
                    Text(
                        text = "FEATURED PLAYLIST",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = playlist.name,
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "The best new songs, updated every day.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 16.sp,
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                if (focused) Color.White else Color.White.copy(alpha = 0.92f),
                                CircleShape,
                            )
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Play", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Shelves ─────────────────────────────────────────────────────────────────────

private fun androidx.tv.foundation.lazy.list.TvLazyListScope.trackShelf(
    title: String,
    tracks: List<Track>,
    player: PlayerViewModel,
    onOpenNowPlaying: () -> Unit,
) {
    if (tracks.isEmpty()) return
    item { ShelfTitle(title) }
    item {
        TvLazyRow(
            contentPadding = PaddingValues(horizontal = EdgePad),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(tracks) { track ->
                TrackCard(
                    track = track,
                    onClick = {
                        player.playFrom(tracks, track)
                        onOpenNowPlaying()
                    },
                )
            }
        }
    }
}

private fun androidx.tv.foundation.lazy.list.TvLazyListScope.playlistShelf(
    title: String,
    playlists: List<Playlist>,
    player: PlayerViewModel,
    onOpenNowPlaying: () -> Unit,
) {
    if (playlists.isEmpty()) return
    item { ShelfTitle(title) }
    item {
        TvLazyRow(
            contentPadding = PaddingValues(horizontal = EdgePad),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(playlists) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    onClick = {
                        player.playQueue(MockData.tracksFor(playlist))
                        onOpenNowPlaying()
                    },
                )
            }
        }
    }
}

@Composable
private fun ShelfTitle(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 23.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = EdgePad),
    )
}

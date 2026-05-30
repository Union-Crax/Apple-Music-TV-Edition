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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import com.apple.music.tv.data.MockData
import com.apple.music.tv.playback.PlayerViewModel
import com.apple.music.tv.ui.components.PlaylistCard
import com.apple.music.tv.ui.components.TrackCard
import com.apple.music.tv.ui.theme.AppleMusicRed
import com.apple.music.tv.ui.theme.CardSurface
import com.apple.music.tv.ui.theme.Elevated
import com.apple.music.tv.ui.theme.TextPrimary
import com.apple.music.tv.ui.theme.TextSecondary
import com.apple.music.tv.ui.theme.TextTertiary

private val EdgePad = 40.dp

/**
 * Search across the catalogue. The text field uses the platform IME (works with a
 * connected keyboard or the on-screen TV remote keyboard); results update live.
 */
@Composable
fun SearchScreen(player: PlayerViewModel) {
    var query by remember { mutableStateOf("") }

    val trackResults = remember(query) { MockData.searchTracks(query) }
    val playlistResults = remember(query) { MockData.searchPlaylists(query) }

    TvLazyColumn(
        contentPadding = PaddingValues(top = 28.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        item {
            Text(
                text = "Search",
                color = TextPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = EdgePad),
            )
        }

        item { SearchField(query = query, onQueryChange = { query = it }) }

        when {
            query.isBlank() -> item { Hint("Find your favorite songs, artists, and playlists.") }
            trackResults.isEmpty() && playlistResults.isEmpty() ->
                item { Hint("No results for “$query”.") }
            else -> {
                if (playlistResults.isNotEmpty()) {
                    item { ResultHeader("Playlists") }
                    item {
                        TvLazyRow(
                            contentPadding = PaddingValues(horizontal = EdgePad),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            items(playlistResults) { pl ->
                                PlaylistCard(
                                    playlist = pl,
                                    onClick = { player.playQueue(MockData.tracksFor(pl)) },
                                )
                            }
                        }
                    }
                }
                if (trackResults.isNotEmpty()) {
                    item { ResultHeader("Songs") }
                    item {
                        TvLazyRow(
                            contentPadding = PaddingValues(horizontal = EdgePad),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            items(trackResults) { track ->
                                TrackCard(
                                    track = track,
                                    onClick = { player.playFrom(trackResults, track) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(horizontal = EdgePad)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .background(if (focused) CardSurface else Elevated, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                tint = if (focused) AppleMusicRed else TextSecondary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("Artists, Songs, Playlists", color = TextTertiary, fontSize = 18.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 18.sp),
                    cursorBrush = SolidColor(AppleMusicRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused },
                )
            }
        }
    }
}

@Composable
private fun ResultHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = EdgePad),
    )
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 17.sp,
        modifier = Modifier.padding(horizontal = EdgePad, vertical = 40.dp),
    )
}

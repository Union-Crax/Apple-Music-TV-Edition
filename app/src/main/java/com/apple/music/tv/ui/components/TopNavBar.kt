package com.apple.music.tv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Text
import com.apple.music.tv.navigation.HomeTab
import com.apple.music.tv.ui.theme.AppleMusicRed

/**
 * Top navigation bar for the home shell.
 *
 * Shows the app wordmark and a focusable pill for each [HomeTab]. The currently
 * selected tab is tinted with the Apple Music accent; the focused tab gets the standard
 * TV focus treatment so D-pad navigation is obvious from across the room.
 */
@Composable
fun TopNavBar(
    selected: HomeTab,
    onSelect: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Wordmark
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            tint = AppleMusicRed,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Music",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
        )

        Spacer(Modifier.width(48.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NavTab(
                label = HomeTab.HOME.label,
                icon = Icons.Filled.Home,
                selected = selected == HomeTab.HOME,
                onClick = { onSelect(HomeTab.HOME) },
            )
            NavTab(
                label = HomeTab.SEARCH.label,
                icon = Icons.Filled.Search,
                selected = selected == HomeTab.SEARCH,
                onClick = { onSelect(HomeTab.SEARCH) },
            )
        }
    }
}

@Composable
private fun NavTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val container = if (selected) AppleMusicRed.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.08f)
    val content = if (selected) Color.White else Color.White.copy(alpha = 0.75f)

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = container,
            contentColor = content,
            focusedContainerColor = if (selected) AppleMusicRed else Color.White.copy(alpha = 0.25f),
            focusedContentColor = Color.White,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text = label, style = MaterialTheme.typography.titleSmall)
        }
    }
}

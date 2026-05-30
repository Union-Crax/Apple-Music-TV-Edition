package com.apple.music.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark Material 3 colour scheme tuned to Apple Music's pure-black ambient palette.
 *
 * TV apps are almost always viewed in a dim room, so a dark-only theme is intentional.
 */
private val DarkColorScheme = darkColorScheme(
    primary = AppleMusicRed,
    onPrimary = TextPrimary,
    primaryContainer = AppleMusicPink,
    onPrimaryContainer = TextPrimary,
    background = Black,
    onBackground = TextPrimary,
    surface = ContentBackground,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    secondary = AppleMusicPink,
    onSecondary = TextPrimary,
)

/**
 * Top-level Compose theme wrapper. Wrap the root composable with this to apply the
 * Apple Music TV colour scheme and TV-optimised typography everywhere.
 */
@Composable
fun AppleMusicTVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppleMusicTypography,
        content = content,
    )
}

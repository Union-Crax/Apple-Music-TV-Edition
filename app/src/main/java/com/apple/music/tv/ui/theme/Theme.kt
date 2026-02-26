package com.apple.music.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark Material 3 colour scheme inspired by Apple Music's ambient dark palette.
 *
 * TV apps are almost always viewed in a dark room, so a dark-only theme is intentional.
 */
private val DarkColorScheme = darkColorScheme(
    primary = AppleMusicRed,
    onPrimary = OnSurfacePrimary,
    primaryContainer = AppleMusicPink,
    onPrimaryContainer = OnSurfacePrimary,
    background = BackgroundDark,
    onBackground = OnSurfacePrimary,
    surface = SurfaceDark,
    onSurface = OnSurfacePrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceSecondary,
    secondary = AppleMusicPink,
    onSecondary = OnSurfacePrimary,
)

/**
 * Top-level Compose theme wrapper.
 *
 * Wrap your root composable with this to apply the Apple Music TV colour scheme
 * and TV-optimised typography throughout the entire composition tree.
 */
@Composable
fun AppleMusicTVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppleMusicTypography,
        content = content,
    )
}

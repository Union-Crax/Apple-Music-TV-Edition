package com.apple.music.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme as TvMaterialTheme
import androidx.tv.material3.darkColorScheme as tvDarkColorScheme

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
 * Matching colour scheme for the `androidx.tv.material3` widgets (Surface, Card, Button,
 * Text, Icon) so both design systems render with the same palette.
 */
private val TvDarkColorScheme = tvDarkColorScheme(
    primary = AppleMusicRed,
    onPrimary = OnSurfacePrimary,
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
 * The app deliberately uses both `androidx.compose.material3` (for the custom typography
 * scale) and `androidx.tv.material3` (for D-pad-aware Surface/Card/Button focus handling),
 * so we provide a themed environment for each. Wrap the whole composition tree with this.
 */
@Composable
fun AppleMusicTVTheme(content: @Composable () -> Unit) {
    TvMaterialTheme(colorScheme = TvDarkColorScheme) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = AppleMusicTypography,
            content = content,
        )
    }
}

package com.apple.music.tv.ui.theme

import androidx.compose.ui.graphics.Color

// ── Apple Music brand ───────────────────────────────────────────────────────────
val AppleMusicRed = Color(0xFFFA2D48)
val AppleMusicPink = Color(0xFFFF5E7E)

// ── Surfaces (Apple Music dark / "10-foot" UI is essentially pure black) ─────────
val Black = Color(0xFF000000)
val ContentBackground = Color(0xFF0D0D0F)
val SidebarBackground = Color(0xFF141416)
val CardSurface = Color(0xFF1A1A1C)
val Elevated = Color(0x14FFFFFF)        // ~8% white
val ElevatedHover = Color(0x1FFFFFFF)   // ~12% white
val Hairline = Color(0x17FFFFFF)        // subtle separators

// ── Text (matches Apple's label opacities) ──────────────────────────────────────
val TextPrimary = Color(0xFFF5F5F7)
val TextSecondary = Color(0x99EBEBF5)   // ~60%
val TextTertiary = Color(0x52EBEBF5)    // ~32%

// Kept for backwards compatibility with earlier code/tests.
val BackgroundDark = Black
val SurfaceDark = CardSurface
val SurfaceVariantDark = Color(0xFF2C2C2E)
val OnSurfacePrimary = TextPrimary
val OnSurfaceSecondary = TextSecondary
val OnSurfaceTertiary = TextTertiary

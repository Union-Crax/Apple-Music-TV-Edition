package com.apple.music.tv.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Procedurally generated, Apple Music–style gradient cover art.
 *
 * Real album artwork would need a network and licensing, so every item gets a
 * deterministic gradient + soft abstract "blobs" derived from [seed]. The result
 * looks designed (think Apple's own playlist covers) and renders instantly offline.
 *
 * @param label Optional title drawn over the art (used for playlists / stations).
 * @param badge Optional small tag at the bottom (e.g. "PLAYLIST", "LIVE").
 */
@Composable
fun Artwork(
    colorStart: Long,
    colorEnd: Long,
    seed: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    label: String? = null,
    badge: String? = null,
    labelSize: TextUnit = 22.sp,
) {
    val c1 = Color(colorStart)
    val c2 = Color(colorEnd)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.linearGradient(listOf(c1, c2))),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val s = size.minDimension
            val h = seed.hashCode()

            // A few translucent circles add organic texture.
            for (i in 0 until 3) {
                val cx = size.width * seededFloat(h, i * 2)
                val cy = size.height * seededFloat(h, i * 2 + 1)
                val r = s * (0.30f + 0.34f * seededFloat(h, i + 7))
                val light = seededFloat(h, i + 11) > 0.5f
                drawCircle(
                    color = if (light) Color.White else Color.Black,
                    radius = r,
                    center = Offset(cx, cy),
                    alpha = 0.08f + 0.10f * seededFloat(h, i + 5),
                )
            }
            // Top-left sheen for a glossy, lit feel.
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.7f, size.height * 0.7f),
                ),
            )
        }

        if (label != null) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = labelSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp,
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.35f), blurRadius = 16f),
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 14.dp),
            )
        }

        if (badge != null) {
            Text(
                text = badge.uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.4.sp,
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
            )
        }
    }
}

/** Deterministic pseudo-random float in [0,1) from an integer [seed] and index [i]. */
private fun seededFloat(seed: Int, i: Int): Float {
    var x = (seed * 73856093) xor (i * 19349663)
    x = x xor (x ushr 13)
    x *= 1274126177
    x = x xor (x ushr 16)
    return ((x ushr 8) and 0xFFFF) / 65536f
}

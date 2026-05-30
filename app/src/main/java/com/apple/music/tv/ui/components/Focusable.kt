package com.apple.music.tv.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

/**
 * The single focus/click primitive used throughout the app.
 *
 * Built only on Compose foundation so behaviour is identical and predictable on
 * every device: [clickable] makes the node focusable and turns the D-pad CENTER /
 * ENTER key into a click, while the current focus state is handed to [content] so
 * each call site can render its own TV focus treatment (scale, ring, fill, …).
 *
 * Placed inside a `TvLazyRow`/`TvLazyColumn`, a focused item is automatically
 * scrolled into view by the TV foundation list.
 */
@Composable
fun FocusableItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    enabled: Boolean = true,
    content: @Composable (focused: Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }

    val base = if (focusRequester != null) modifier.focusRequester(focusRequester) else modifier

    androidx.compose.foundation.layout.Box(
        modifier = base
            .onFocusChanged { focused = it.isFocused }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
    ) {
        content(focused)
    }
}

/** Convenience for screens that want to grab initial focus on first composition. */
@Composable
fun rememberFocusRequesterSafely(): FocusRequester = remember { FocusRequester() }

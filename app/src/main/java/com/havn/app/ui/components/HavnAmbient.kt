package com.havn.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The ambient field behind immersive screens.
 *
 * Swiss: the ground is flat. The field is the canvas itself — no blooms, no
 * drift. [intensity] and [animated] are kept for call-site compatibility.
 */
@Composable
fun HavnAmbientField(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    animated: Boolean = true,
) {
    val colors = com.havn.app.ui.theme.HavnTheme.colors

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(colors.canvas)
    }
}

package com.havn.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * The ambient field behind immersive screens.
 *
 * This replaces the WebView/WebGL shader the app previously mounted behind the
 * home screen. That version spun up a second renderer process and drove a
 * fragment shader at display refresh for what is, visually, two soft colour
 * blooms — it cost real battery, held scrolling back, and was hard-coded to
 * light-mode ivory so dark mode got a bright haze over it.
 *
 * Here the same effect is three radial gradients drifting on long, prime-ish
 * cycles so the loop never visibly repeats. Cost is one Canvas layer; it reads
 * from theme tokens, so dark mode gets its own deep, cooler atmosphere.
 */
@Composable
fun HavnAmbientField(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    animated: Boolean = true,
) {
    val colors = com.havn.app.ui.theme.HavnTheme.colors

    val transition = rememberInfiniteTransition(label = "ambient")
    val drift by if (animated) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(38_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "ambientDrift",
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0.6f) }
    }

    // Dark mode needs a lower ceiling: the same alpha that reads as a whisper
    // on paper reads as a smear on ink.
    val ceiling = if (colors.isDark) 0.5f else 0.85f

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Bloom one — sage, upper field, slow orbit.
        val x1 = w * (0.22f + 0.10f * cos(drift))
        val y1 = h * (0.16f + 0.06f * sin(drift * 0.7f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.ambientA.copy(alpha = ceiling * intensity),
                    colors.ambientA.copy(alpha = 0f),
                ),
                center = Offset(x1, y1),
                radius = w * 0.95f,
            ),
            radius = w * 0.95f,
            center = Offset(x1, y1),
        )

        // Bloom two — clay, lower right, counter-rotating so the two never
        // lock into a visible rhythm.
        val x2 = w * (0.86f - 0.12f * sin(drift * 0.53f))
        val y2 = h * (0.74f + 0.08f * cos(drift * 0.41f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.ambientB.copy(alpha = ceiling * 0.72f * intensity),
                    colors.ambientB.copy(alpha = 0f),
                ),
                center = Offset(x2, y2),
                radius = w * 0.78f,
            ),
            radius = w * 0.78f,
            center = Offset(x2, y2),
        )

        // A wash back to the canvas at the bottom so content that scrolls under
        // the field always lands on a settled ground.
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    colors.canvas.copy(alpha = 0.55f),
                    colors.canvas,
                ),
                startY = h * 0.45f,
                endY = h,
            )
        )
    }
}

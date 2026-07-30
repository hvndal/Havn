package com.havn.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin
import kotlin.math.cos

/**
 * Warm animated canvas background — Hävn palette slow waves
 * Matches the WebGL shader aesthetic from the stitch design system
 */
@Composable
fun HavnShaderBackground(
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    val transition = rememberInfiniteTransition(label = "shader")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI * 10).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "time",
    )

    Canvas(modifier = modifier) {
        drawWarmShader(time)
    }
}

private fun DrawScope.drawWarmShader(time: Float) {
    val w = size.width
    val h = size.height
    val stepX = w / 24f
    val stepY = h / 24f

    // Ivory base
    drawRect(Color(0xFFFBF9F5))

    // Subtle warm sand waves
    for (row in 0..24) {
        for (col in 0..24) {
            val uvX = col / 24f
            val uvY = row / 24f
            val wave1 = sin(uvX * 2f + time * 0.07f) * 0.5f + 0.5f
            val wave2 = cos(uvY * 3f - time * 0.05f) * 0.5f + 0.5f
            val noise = wave1 * wave2

            // Blend ivory → sand
            val r = 0.98f + (0.94f - 0.98f) * noise * 0.25f
            val g = 0.97f + (0.92f - 0.97f) * noise * 0.25f
            val b = 0.96f + (0.89f - 0.96f) * noise * 0.25f

            // Vignette
            val vx = uvX - 0.5f
            val vy = uvY - 0.5f
            val vign = 1f - (vx * vx + vy * vy) * 0.4f

            drawRect(
                color = Color(r * vign, g * vign, b * vign, 0.6f),
                topLeft = Offset(col * stepX, row * stepY),
                size = androidx.compose.ui.geometry.Size(stepX + 1f, stepY + 1f),
            )
        }
    }
}

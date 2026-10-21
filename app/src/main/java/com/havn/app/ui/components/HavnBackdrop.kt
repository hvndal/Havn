package com.havn.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.havn.app.ui.theme.HavnColorTokens
import com.havn.app.ui.theme.HavnDarkTokens
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType
import com.havn.app.ui.theme.LocalHavnColors
import com.havn.app.ui.theme.rememberReduceMotion
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
//  B A C K D R O P
//
//  The language Today set, shared by every screen: a slow generative field
//  (warm near-black, a fine dot grid, two drifting lights, one of them the
//  signal orange) with paper groups floating on it. Text that sits on the
//  field is light in both themes; text inside a group uses the user's theme.
// ─────────────────────────────────────────────────────────────────────────────

/** The theme colours groups use, captured before the backdrop overrides them. */
private val LocalHavnGroupColors = staticCompositionLocalOf<HavnColorTokens?> { null }

/**
 * The field itself. [progress] (0–1) strengthens the orange light, so a
 * screen can warm as the day fills. Under reduced motion the lights hold.
 */
@Composable
fun HavnBackdrop(progress: Float = 0f, modifier: Modifier = Modifier) {
    val reduceMotion = rememberReduceMotion()
    val glow by animateFloatAsState(progress, tween(1200, easing = HavnMotion.Decisive), label = "glow")
    val t = if (reduceMotion) {
        0.3f
    } else {
        val time by rememberInfiniteTransition(label = "backdrop").animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(40_000, easing = LinearEasing)),
            label = "backdrop-time",
        )
        time
    }
    Canvas(modifier.fillMaxSize()) {
        drawRect(Color(0xFF0E0E0B))
        val w = size.width
        val h = size.height
        val grey = Offset(w * (0.25f + 0.12f * sin(t)), h * (0.18f + 0.06f * cos(t * 2)))
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFF6E6B60).copy(alpha = 0.55f), Color.Transparent), grey, w * 0.9f),
            radius = w * 0.9f,
            center = grey,
        )
        val orange = Offset(w * (0.8f + 0.1f * cos(t)), h * (0.42f + 0.08f * sin(t * 2)))
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFC23A05).copy(alpha = 0.22f + 0.28f * glow), Color.Transparent), orange, w * 0.75f,
            ),
            radius = w * 0.75f,
            center = orange,
        )
        val pitch = 24.dp.toPx()
        val dot = Color(0xFFF2F1EC).copy(alpha = 0.07f)
        val r = 1.dp.toPx()
        var y = pitch / 2
        while (y < h) {
            var x = pitch / 2
            while (x < w) {
                drawCircle(dot, radius = r, center = Offset(x, y))
                x += pitch
            }
            y += pitch
        }
    }
}

/**
 * Puts a whole screen on the backdrop. Everything inside reads the night
 * palette with a transparent canvas, so titles, labels and loose controls
 * are light on the field; [HavnGroup] switches back to the user's theme.
 */
@Composable
fun HavnBackdropScreen(
    progress: Float = 0f,
    content: @Composable BoxScope.() -> Unit,
) {
    val base = HavnTheme.colors
    Box(Modifier.fillMaxSize()) {
        HavnBackdrop(progress)
        CompositionLocalProvider(
            LocalHavnGroupColors provides base,
            LocalHavnColors provides HavnDarkTokens.copy(
                canvas = Color.Transparent,
                surface = Color.Transparent,
                surfaceSunken = Color(0xFFF2F1EC).copy(alpha = 0.08f),
                surfacePressed = Color(0xFFF2F1EC).copy(alpha = 0.14f),
                hairline = Color(0xFFF2F1EC).copy(alpha = 0.12f),
            ),
        ) {
            Box(Modifier.fillMaxSize(), content = content)
        }
    }
}

/** A rounded paper group floating on the backdrop, in the user's own theme. */
@Composable
fun HavnGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val group = LocalHavnGroupColors.current ?: HavnTheme.colors
    CompositionLocalProvider(LocalHavnColors provides group) {
        Column(
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(group.surfaceRaised.copy(alpha = if (group.isDark) 0.92f else 0.97f)),
            content = content,
        )
    }
}

/** "Morning  1/3": the label above a group, light on the backdrop. */
@Composable
fun HavnGroupHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    val colors = HavnTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) Text(trailing, style = HavnType.Clock, color = colors.textSecondary)
    }
}

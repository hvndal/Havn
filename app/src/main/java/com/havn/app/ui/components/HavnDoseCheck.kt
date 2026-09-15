package com.havn.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.domain.model.DoseStatus

/**
 * The control for marking a dose taken — the single most-used interaction in
 * the app, so it gets the most attention.
 *
 * The tick is *drawn*, not faded in: the stroke traces along its own path in
 * ~180ms while the fill expands behind it. That reads as the app writing the
 * dose down, which is exactly what just happened. A cross-fading icon reads as
 * a state variable changing.
 *
 * The ring at rest is a hairline, not a heavy outline, so an untaken dose does
 * not shout at the user all day.
 */
@Composable
fun HavnDoseCheck(
    status: DoseStatus,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    accent: Color = HavnTheme.colors.accent,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    val colors = HavnTheme.colors
    val taken = status == DoseStatus.TAKEN
    val skipped = status == DoseStatus.SKIPPED

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val fillProgress by animateFloatAsState(
        targetValue = if (taken) 1f else 0f,
        animationSpec = HavnMotion.celebrate(),
        label = "checkFill",
    )
    val strokeProgress = remember { Animatable(if (taken) 1f else 0f) }
    LaunchedEffect(taken) {
        if (taken) {
            strokeProgress.animateTo(1f, HavnMotion.enter())
        } else {
            // Leaving is faster than arriving. Undo should feel immediate.
            strokeProgress.animateTo(0f, HavnMotion.exit())
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.86f else 1f,
        animationSpec = HavnMotion.press(),
        label = "checkPress",
    )

    val ringColor by animateColorAsState(
        targetValue = when {
            taken -> accent
            skipped -> colors.textDisabled
            else -> colors.hairlineStrong
        },
        animationSpec = HavnMotion.standard(),
        label = "checkRing",
    )

    // A 26dp glyph in a 44dp box: the visual weight stays quiet while the tap
    // target meets the accessibility minimum.
    Box(
        modifier = modifier
            .size(44.dp)
            .toggleable(
                value = taken,
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = {
                    haptics.performHapticFeedback(
                        if (taken) HapticFeedbackType.TextHandleMove
                        else HapticFeedbackType.LongPress
                    )
                    onToggle()
                },
            )
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
        ) {
            val d = this.size.minDimension
            val ring = 1.5.dp.toPx()
            val center = Offset(d / 2f, d / 2f)

            // Resting ring
            drawCircle(
                color = ringColor,
                radius = d / 2f - ring / 2f,
                center = center,
                style = Stroke(width = ring),
            )

            // Fill grows from the centre as the dose is confirmed
            if (fillProgress > 0.01f) {
                drawCircle(
                    color = accent,
                    radius = (d / 2f) * fillProgress,
                    center = center,
                )
            }

            // Skipped reads as a deliberate strike, not a failure
            if (skipped) {
                val inset = d * 0.3f
                drawLine(
                    color = colors.textDisabled,
                    start = Offset(inset, d / 2f),
                    end = Offset(d - inset, d / 2f),
                    strokeWidth = ring,
                    cap = StrokeCap.Round,
                )
            }

            // The tick, traced along its path
            if (strokeProgress.value > 0.01f) {
                val tick = Path().apply {
                    moveTo(d * 0.28f, d * 0.52f)
                    lineTo(d * 0.44f, d * 0.68f)
                    lineTo(d * 0.73f, d * 0.34f)
                }
                val measure = PathMeasure().apply { setPath(tick, false) }
                val drawn = Path()
                measure.getSegment(0f, measure.length * strokeProgress.value, drawn, true)
                drawPath(
                    path = drawn,
                    color = colors.onAccent,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
    }
}


/**
 * A ring that traces the day's completion. Used once per screen, at size — it
 * is the one piece of data visualisation on Today, so it can afford to be big.
 */
@Composable
fun HavnProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp,
    trackColor: Color = HavnTheme.colors.hairline,
    color: Color = HavnTheme.colors.accent,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = HavnMotion.glide(),
        label = "ringProgress",
    )

    Canvas(modifier = modifier.clip(CircleShape)) {
        val stroke = strokeWidth.toPx()
        val inset = stroke / 2f
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = androidx.compose.ui.geometry.Size(
                size.width - stroke,
                size.height - stroke,
            ),
            style = Stroke(width = stroke),
        )
        if (animated > 0f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(
                    size.width - stroke,
                    size.height - stroke,
                ),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}

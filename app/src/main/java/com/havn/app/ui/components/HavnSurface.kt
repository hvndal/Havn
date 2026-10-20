package com.havn.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme

/**
 * The one surface primitive in the app.
 *
 * Depth is a fill and a hairline, never a shadow: [elevation] is kept for
 * call-site compatibility and draws nothing.
 */
@Composable
fun HavnSurface(
    modifier: Modifier = Modifier,
    color: Color = HavnTheme.colors.surface,
    shape: Shape = RectangleShape,
    elevation: Dp = HavnTheme.elevation.rest,
    border: BorderStroke? = BorderStroke(1.dp, HavnTheme.colors.hairline),
    contentPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(color)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

/**
 * A surface that responds to touch. The fill shifts and the whole card takes a
 * small step back — the scale is deliberately gentler than a button's, because
 * large surfaces moving far reads as a rendering fault rather than feedback.
 */
@Composable
fun HavnInteractiveSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = HavnTheme.colors.surface,
    pressedColor: Color = HavnTheme.colors.surfacePressed,
    shape: Shape = RectangleShape,
    elevation: Dp = HavnTheme.elevation.rest,
    border: BorderStroke? = BorderStroke(1.dp, HavnTheme.colors.hairline),
    contentPadding: Dp = 0.dp,
    enabled: Boolean = true,
    scaleDown: Float = 0.985f,
    onClickLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptics = rememberHavnHaptics()

    val fill by animateColorAsState(
        targetValue = if (pressed) pressedColor else color,
        animationSpec = HavnMotion.quick(),
        label = "surfaceFill",
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) scaleDown else 1f,
        animationSpec = HavnMotion.press(),
        label = "surfaceScale",
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(fill)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel = onClickLabel,
                onClick = {
                    onClick()
                },
            )
            .padding(contentPadding),
        content = content,
    )
}

/**
 * A recessed well — inputs, track grooves, quiet insets. Sunken surfaces carry
 * no shadow; the fill being *below* its ground is the whole signal.
 */
@Composable
fun HavnWell(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    border: BorderStroke? = null,
    contentPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(HavnTheme.colors.surfaceSunken)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

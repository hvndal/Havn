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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
 * Depth is built from three cooperating signals rather than a heavy border:
 * a fill one step above its ground, a hairline at ~4% contrast, and a soft
 * shadow tinted by the theme. In dark mode the shadow nearly vanishes (there
 * is nothing darker than the ground to cast onto), so the hairline carries
 * separation instead — which is why both are always present.
 */
@Composable
fun HavnSurface(
    modifier: Modifier = Modifier,
    color: Color = HavnTheme.colors.surface,
    shape: Shape = RoundedCornerShape(HavnTheme.radius.lg),
    elevation: Dp = HavnTheme.elevation.rest,
    border: BorderStroke? = BorderStroke(1.dp, HavnTheme.colors.hairline),
    contentPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HavnTheme.colors
    Column(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = colors.shadowTint.copy(alpha = if (colors.isDark) 0.9f else 0.10f),
                spotColor = colors.shadowTint.copy(alpha = if (colors.isDark) 0.9f else 0.08f),
            )
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
    shape: Shape = RoundedCornerShape(HavnTheme.radius.lg),
    elevation: Dp = HavnTheme.elevation.rest,
    border: BorderStroke? = BorderStroke(1.dp, HavnTheme.colors.hairline),
    contentPadding: Dp = 0.dp,
    enabled: Boolean = true,
    scaleDown: Float = 0.985f,
    onClickLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HavnTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

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
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = colors.shadowTint.copy(alpha = if (colors.isDark) 0.9f else 0.10f),
                spotColor = colors.shadowTint.copy(alpha = if (colors.isDark) 0.9f else 0.08f),
            )
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
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
    shape: Shape = RoundedCornerShape(HavnTheme.radius.md),
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

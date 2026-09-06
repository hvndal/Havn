package com.havn.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.havn.app.ui.theme.Sage
import com.havn.app.ui.theme.SurfaceHighest

/**
 * Adds a subtle, tactile spring press scale micro-interaction to any clickable component.
 *
 * @param targetScale Scale target when pressed (defaults to 0.96f for buttons/cards).
 * @param interactionSource Interaction source to collect pressed states from.
 */
fun Modifier.pressScale(
    targetScale: Float = 0.96f,
    interactionSource: InteractionSource? = null,
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        val target = if (isPressed) targetScale else 1f
        val springSpec: AnimationSpec<Float> = if (isPressed) {
            spring(stiffness = Spring.StiffnessHigh, dampingRatio = Spring.DampingRatioMediumBouncy)
        } else {
            spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy)
        }
        scaleAnim.animateTo(target, springSpec)
    }

    this.scale(scaleAnim.value)
}

/**
 * Adds an animated border that transitions smoothly between unfocused and focused states.
 */
fun Modifier.animatedFocusBorder(
    interactionSource: InteractionSource,
    shape: Shape = RoundedCornerShape(16.dp),
    unfocusedColor: Color = SurfaceHighest,
    focusedColor: Color = Sage,
    borderWidth: Dp = 1.dp,
): Modifier = composed {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColorAnim = remember { androidx.compose.animation.Animatable(unfocusedColor) }

    LaunchedEffect(isFocused) {
        borderColorAnim.animateTo(
            targetValue = if (isFocused) focusedColor else unfocusedColor,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
    }

    this.border(
        width = borderWidth,
        color = borderColorAnim.value,
        shape = shape
    )
}

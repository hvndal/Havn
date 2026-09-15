package com.havn.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import com.havn.app.ui.theme.HavnMotion

// ─────────────────────────────────────────────────────────────────────────────
//  P R E S S
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The house press behaviour: the surface takes a small step back under the
 * finger and springs home on release. No ripple — ripple belongs to Material,
 * and it muddies the warm surfaces this app is built from.
 *
 * Scale is tuned by surface size: a 44dp button can afford 0.94, a full-width
 * hero cannot (it reads as a glitch), which is why [scaleDown] is a parameter
 * rather than a constant.
 */
fun Modifier.havnPress(
    scaleDown: Float = 0.96f,
    enabled: Boolean = true,
    haptic: Boolean = true,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onClick: () -> Unit,
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = HavnMotion.press(),
        label = "havnPressScale",
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
            onClickLabel = onClickLabel,
            onClick = {
                if (haptic) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                onClick()
            },
        )
}

/**
 * Press feedback expressed as opacity instead of scale — for elements where
 * scaling would disturb a layout (list rows flush against neighbours, inline
 * text actions).
 */
fun Modifier.havnPressFade(
    enabled: Boolean = true,
    haptic: Boolean = true,
    role: Role? = Role.Button,
    onClick: () -> Unit,
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val alpha by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.6f else 1f,
        animationSpec = HavnMotion.press(),
        label = "havnPressAlpha",
    )

    this
        .graphicsLayer { this.alpha = alpha }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = {
                if (haptic) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                onClick()
            },
        )
}

/** Confirmation weight — for irreversible or celebratory actions. */
@Composable
fun rememberHavnHaptics(): HavnHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { HavnHaptics(feedback) }
}

class HavnHaptics(private val feedback: HapticFeedback) {
    /** A light tick — selection, toggle, tab change. */
    fun tick() = feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    /** A fuller thud — dose logged, profile created, destructive confirm. */
    fun confirm() = feedback.performHapticFeedback(HapticFeedbackType.LongPress)
}


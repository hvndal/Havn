package com.havn.app.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.ui.platform.LocalContext
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
    val hapticFeedback = rememberHavnHaptics()

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
                    hapticFeedback.click()
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
    val hapticFeedback = rememberHavnHaptics()

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
                    hapticFeedback.click()
                }
                onClick()
            },
        )
}

/** Confirmation weight — for irreversible, celebratory or precision instrument actions. */
@Composable
fun rememberHavnHaptics(): HavnHaptics {
    val context = LocalContext.current
    val feedback = LocalHapticFeedback.current
    return remember(context, feedback) { HavnHaptics(feedback, context) }
}

/**
 * Multi-stage hardware actuator haptics tailored for a precision Nordic medication instrument.
 *
 * Uses Android 11+ [VibrationEffect.Composition] primitives (TICK, CLICK, THUD) with
 * graceful fallback to Android 10 predefined effects and classic Compose haptic feedback.
 */
class HavnHaptics(
    private val feedback: HapticFeedback,
    private val context: Context? = null,
) {
    constructor(context: Context, feedback: HapticFeedback) : this(feedback, context)

    private val vibrator: Vibrator? = runCatching {
        if (context == null) null
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    /**
     * Stage 1: Mechanical tick.
     * Subtle micro-detent for tabs, segment switching, odometer rolls, and swipe thresholds.
     */
    fun tick() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && vibrator?.hasVibrator() == true) {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_LOW_TICK)) {
                    vibrator.vibrate(
                        VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.45f)
                            .compose()
                    )
                    return
                } else if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK)) {
                    vibrator.vibrate(
                        VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.4f)
                            .compose()
                    )
                    return
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }.onFailure {
            feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /**
     * Stage 2: Crisp switch click.
     * For buttons, row presses, and picker selections.
     */
    fun click() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && vibrator?.hasVibrator() == true) {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                    vibrator.vibrate(
                        VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.65f)
                            .compose()
                    )
                    return
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }.onFailure {
            feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /**
     * Stage 3: Physical mechanical latch / confirm.
     * For marking a dose taken or confirming important actions.
     * Dual-phase: a subtle preparatory pulse followed by a solid latch.
     */
    fun latch() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && vibrator?.hasVibrator() == true) {
                if (vibrator.areAllPrimitivesSupported(
                        VibrationEffect.Composition.PRIMITIVE_CLICK,
                        VibrationEffect.Composition.PRIMITIVE_THUD
                    )
                ) {
                    vibrator.vibrate(
                        VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.5f)
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.85f, 35)
                            .compose()
                    )
                    return
                } else if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD)) {
                    vibrator.vibrate(
                        VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.9f)
                            .compose()
                    )
                    return
                } else if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                    vibrator.vibrate(
                        VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f)
                            .compose()
                    )
                    return
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                feedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }.onFailure {
            feedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /** Alias for latch to maintain compatibility with existing callers. */
    fun confirm() = latch()

    /**
     * Stage 4: Spring release / undo.
     * For unchecking a dose or resetting a state.
     */
    fun release() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && vibrator?.hasVibrator() == true) {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_LOW_TICK)) {
                    vibrator.vibrate(
                        VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.35f)
                            .compose()
                    )
                    return
                }
            }
            feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }.onFailure {
            feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /**
     * Stage 5: Day complete celebration.
     * Quiet double-tap chord when all doses for the day are finished.
     */
    fun celebrate() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && vibrator?.hasVibrator() == true) {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                    vibrator.vibrate(
                        VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.45f)
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f, 50)
                            .compose()
                    )
                    return
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else {
                feedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }.onFailure {
            feedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}



package com.havn.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.havn.app.R
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType

/**
 * Tactile Swipe-to-Log gesture container for medication dose rows.
 *
 * Implements Scandinavian-restrained physical sliding action:
 *  - Swipe Right (Start → End): Log as Taken (or Undo if already taken).
 *  - Swipe Left (End → Start): Mark as Skipped (or Undo if already skipped).
 *  - Analog Detent: Mechanical micro-tick on threshold cross; latch/confirm on release.
 *  - Dynamic Icon Parallax & Scale: Subtle scaling and hairline cues without jarring shifts.
 *  - Non-destructive Spring-back: Row effortlessly settles back to resting position.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HavnSwipeToLogRow(
    isTaken: Boolean,
    isSkipped: Boolean,
    onTake: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = HavnTheme.colors
    val haptics = rememberHavnHaptics()

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> (totalDistance * 0.32f).coerceAtLeast(80f) },
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (isTaken) {
                        haptics.release()
                        onUndo()
                    } else {
                        haptics.latch()
                        onTake()
                    }
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    if (isSkipped) {
                        haptics.release()
                        onUndo()
                    } else {
                        haptics.release()
                        onSkip()
                    }
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    var previousArmed by remember { mutableStateOf(false) }

    // Detent tick when crossing activation threshold in either direction
    LaunchedEffect(dismissState.targetValue) {
        val armed = dismissState.targetValue != SwipeToDismissBoxValue.Settled
        if (armed != previousArmed) {
            haptics.tick()
            previousArmed = armed
        }
    }

    val isArmed = dismissState.targetValue != SwipeToDismissBoxValue.Settled
    val direction = dismissState.dismissDirection
    val isSwipingStart = direction == SwipeToDismissBoxValue.StartToEnd
    val isSwipingEnd = direction == SwipeToDismissBoxValue.EndToStart
    val isSliding = direction != SwipeToDismissBoxValue.Settled

    // Spring scaling for active icon when threshold is breached
    val actionScale by animateFloatAsState(
        targetValue = if (isArmed) 1.15f else 0.95f,
        animationSpec = HavnMotion.press(),
        label = "swipeActionScale",
    )

    val rowShape = RoundedCornerShape(HavnTheme.radius.md)

    // Accessible actions for TalkBack screen readers
    val takeLabel = if (isTaken) "Undo dose taken" else "Log dose taken"
    val skipLabel = if (isSkipped) "Undo dose skip" else "Skip dose"

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.semantics {
            customActions = listOf(
                CustomAccessibilityAction(takeLabel) {
                    if (isTaken) onUndo() else onTake()
                    true
                },
                CustomAccessibilityAction(skipLabel) {
                    if (isSkipped) onUndo() else onSkip()
                    true
                },
            )
        },
        enableDismissFromStartToEnd = enabled,
        enableDismissFromEndToStart = enabled,
        backgroundContent = {
            // Colors & tones
            val startBg = when {
                isTaken -> colors.textDisabled.copy(alpha = if (isArmed) 0.16f else 0.08f)
                else -> colors.accent.copy(alpha = if (isArmed) 0.20f else 0.09f)
            }
            val endBg = when {
                isSkipped -> colors.textDisabled.copy(alpha = if (isArmed) 0.16f else 0.08f)
                else -> colors.warning.copy(alpha = if (isArmed) 0.20f else 0.09f)
            }

            val targetBg = when {
                isSwipingStart -> startBg
                isSwipingEnd -> endBg
                else -> Color.Transparent
            }

            val animatedBg by animateColorAsState(
                targetValue = targetBg,
                animationSpec = HavnMotion.quick(),
                label = "swipeBgColor",
            )

            val borderColor: Color = when {
                isArmed && isSwipingStart -> if (isTaken) colors.hairlineStrong else colors.accent.copy(alpha = 0.45f)
                isArmed && isSwipingEnd -> if (isSkipped) colors.hairlineStrong else colors.warning.copy(alpha = 0.45f)
                else -> Color.Transparent
            }

            val currentOffset = runCatching { dismissState.requireOffset() }.getOrDefault(0f)
            val parallaxPx = (currentOffset * 0.06f).coerceIn(-20f, 20f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(rowShape)
                    .background(animatedBg)
                    .then(
                        if (isArmed) Modifier.border(width = 1.dp, color = borderColor, shape = rowShape) else Modifier
                    )
                    .padding(horizontal = HavnTheme.spacing.lg),
                contentAlignment = if (isSwipingStart) Alignment.CenterStart else Alignment.CenterEnd,
            ) {
                if (isSwipingStart) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .graphicsLayer { translationX = parallaxPx.coerceAtLeast(0f) }
                            .scale(actionScale),
                    ) {
                        val iconRes = if (isTaken) R.drawable.ic_undo else R.drawable.ic_check
                        val iconTint = if (isTaken) colors.textSecondary else colors.accent
                        val labelText = if (isTaken) "UNDO" else "TAKEN"

                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = labelText,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(HavnTheme.spacing.xs))
                        Text(
                            text = labelText,
                            style = HavnType.EyebrowQuiet,
                            color = iconTint,
                        )
                    }
                } else if (isSwipingEnd) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .graphicsLayer { translationX = parallaxPx.coerceAtMost(0f) }
                            .scale(actionScale),
                    ) {
                        val iconRes = if (isSkipped) R.drawable.ic_undo else R.drawable.ic_skip
                        val iconTint = if (isSkipped) colors.textSecondary else colors.warning
                        val labelText = if (isSkipped) "UNDO" else "SKIPPED"

                        Text(
                            text = labelText,
                            style = HavnType.EyebrowQuiet,
                            color = iconTint,
                        )
                        Spacer(Modifier.width(HavnTheme.spacing.xs))
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = labelText,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(rowShape)
                .background(colors.canvas)
                .then(
                    if (isSliding) {
                        Modifier.border(
                            width = 1.dp,
                            color = colors.hairlineStrong.copy(alpha = 0.5f),
                            shape = rowShape,
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            content()
        }
    }
}

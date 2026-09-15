package com.havn.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme

enum class HavnButtonTone {
    /** The one action this screen exists for. Filled accent. */
    Primary,
    /** A real but secondary action. Hairline outline, no fill. */
    Secondary,
    /** Tertiary / dismissive. Text only. */
    Ghost,
    /** Destructive confirm. Used sparingly and never as a screen's default. */
    Danger,
}

enum class HavnButtonSize(
    internal val height: Dp,
    internal val horizontalPadding: Dp,
) {
    Large(56.dp, 28.dp),
    Medium(44.dp, 20.dp),
    Small(36.dp, 14.dp),
}

/**
 * Every button in Hävn. One component so that press physics, disabled
 * treatment, minimum touch target and haptics can never drift apart.
 *
 * Heights start at 36dp and the tap target is never below that — small chips
 * still clear the 36dp accessibility floor because the row padding is inside
 * the clickable area, not outside it.
 */
@Composable
fun HavnButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: HavnButtonTone = HavnButtonTone.Primary,
    size: HavnButtonSize = HavnButtonSize.Large,
    enabled: Boolean = true,
    loading: Boolean = false,
    fillWidth: Boolean = true,
    leadingIcon: Painter? = null,
) {
    val colors = HavnTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    val active = enabled && !loading

    val container: Color
    val content: Color
    val borderColor: Color

    when (tone) {
        HavnButtonTone.Primary -> {
            container = if (pressed) colors.accentPressed else colors.accent
            content = colors.onAccent
            borderColor = Color.Transparent
        }
        HavnButtonTone.Secondary -> {
            container = if (pressed) colors.surfacePressed else Color.Transparent
            content = colors.textPrimary
            borderColor = colors.hairlineStrong
        }
        HavnButtonTone.Ghost -> {
            container = if (pressed) colors.surfacePressed else Color.Transparent
            content = colors.textSecondary
            borderColor = Color.Transparent
        }
        HavnButtonTone.Danger -> {
            container = if (pressed) colors.dangerSoft else Color.Transparent
            content = colors.danger
            borderColor = colors.danger.copy(alpha = 0.35f)
        }
    }

    val animatedContainer by animateColorAsState(
        targetValue = if (active) container else colors.surfaceSunken,
        animationSpec = HavnMotion.quick(),
        label = "btnContainer",
    )
    val animatedContent by animateColorAsState(
        targetValue = if (active) content else colors.textDisabled,
        animationSpec = HavnMotion.quick(),
        label = "btnContent",
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed && active) 0.965f else 1f,
        animationSpec = HavnMotion.press(),
        label = "btnScale",
    )

    val shape: Shape = RoundedCornerShape(HavnTheme.radius.pill)

    Row(
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .height(size.height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                // Only the filled tone casts — an outline button that drops a
                // shadow looks like a mistake.
                if (tone == HavnButtonTone.Primary && active) {
                    Modifier.shadow(
                        elevation = if (pressed) 2.dp else 10.dp,
                        shape = shape,
                        ambientColor = colors.accent.copy(alpha = 0.35f),
                        spotColor = colors.accent.copy(alpha = 0.28f),
                    )
                } else Modifier
            )
            .clip(shape)
            .background(animatedContainer)
            .then(
                if (borderColor != Color.Transparent && active) {
                    Modifier.border(1.dp, borderColor, shape)
                } else Modifier
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = active,
                role = Role.Button,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            )
            .padding(horizontal = size.horizontalPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            HavnPulseDots(color = animatedContent)
        } else {
            if (leadingIcon != null) {
                Icon(
                    painter = leadingIcon,
                    contentDescription = null,
                    tint = animatedContent,
                    modifier = Modifier.size(if (size == HavnButtonSize.Small) 14.dp else 18.dp),
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            }
            Text(
                text = text,
                style = when (size) {
                    HavnButtonSize.Large -> MaterialTheme.typography.labelLarge
                    HavnButtonSize.Medium -> MaterialTheme.typography.labelMedium
                    HavnButtonSize.Small -> MaterialTheme.typography.labelSmall
                },
                color = animatedContent,
            )
        }
    }
}

/**
 * Circular icon button. [tone] Primary gives it an accent fill; otherwise it
 * sits quietly on the surface.
 */
@Composable
fun HavnIconButton(
    icon: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: HavnButtonTone = HavnButtonTone.Secondary,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    enabled: Boolean = true,
) {
    val colors = HavnTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val container = when (tone) {
        HavnButtonTone.Primary -> if (pressed) colors.accentPressed else colors.accent
        HavnButtonTone.Danger -> if (pressed) colors.dangerSoft else colors.surfaceSunken
        else -> if (pressed) colors.surfacePressed else colors.surfaceSunken
    }
    val content = when (tone) {
        HavnButtonTone.Primary -> colors.onAccent
        HavnButtonTone.Danger -> colors.danger
        else -> colors.textSecondary
    }

    val animatedContainer by animateColorAsState(container, HavnMotion.quick(), label = "iconBtnBg")
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.9f else 1f,
        animationSpec = HavnMotion.press(),
        label = "iconBtnScale",
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(animatedContainer)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = if (enabled) content else colors.textDisabled,
            modifier = Modifier.size(iconSize),
        )
    }
}

/**
 * A small pill-shaped action that sits inline with text — "Manage", "See all".
 * Deliberately low-contrast so it never competes with a primary button.
 */
@Composable
fun HavnTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = HavnTheme.colors.textSecondary,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.55f else 1f,
        animationSpec = HavnMotion.press(),
        label = "textActionAlpha",
    )

    Row(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(HavnTheme.radius.pill))
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            )
            .defaultMinSize(minHeight = 36.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = color)
        if (trailing != null) {
            CompositionLocalProvider(LocalContentColor provides color) { trailing() }
        }
    }
}

package com.havn.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType

// ─────────────────────────────────────────────────────────────────────────────
//  E M P T Y
// ─────────────────────────────────────────────────────────────────────────────

/**
 * An empty state is a page of the product, not an apology. It gets the same
 * editorial treatment as any other screen: a serif line that says something
 * true, one supporting sentence, and at most one action.
 */
@Composable
fun HavnEmptyState(
    headline: String,
    body: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    action: (@Composable () -> Unit)? = null,
    illustration: (@Composable () -> Unit)? = null,
) {
    val colors = HavnTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HavnTheme.spacing.sm, vertical = HavnTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (illustration != null) {
            illustration()
            Spacer(Modifier.height(HavnTheme.spacing.xl))
        }
        if (eyebrow != null) {
            Text(
                text = eyebrow.uppercase(),
                style = HavnType.Eyebrow,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(HavnTheme.spacing.md))
        }
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(HavnTheme.spacing.sm))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.82f),
        )
        if (action != null) {
            Spacer(Modifier.height(HavnTheme.spacing.xl))
            action()
        }
    }
}

/**
 * A quiet, slowly breathing mark used inside empty states. Drawn rather than
 * illustrated so it inherits the theme and costs nothing.
 */
@Composable
fun HavnBreathingMark(
    modifier: Modifier = Modifier,
    size: Dp = 76.dp,
) {
    val colors = HavnTheme.colors
    val transition = rememberInfiniteTransition(label = "breathe")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathePhase",
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size * (0.72f + phase * 0.28f))
                .clip(CircleShape)
                .background(colors.accent.copy(alpha = 0.06f + phase * 0.05f))
        )
        Box(
            modifier = Modifier
                .size(size * 0.34f)
                .clip(CircleShape)
                .background(colors.accent.copy(alpha = 0.45f + phase * 0.25f))
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  E R R O R
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HavnErrorState(
    headline: String,
    body: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val colors = HavnTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(HavnTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.dangerSoft),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.danger)
            )
        }
        Spacer(Modifier.height(HavnTheme.spacing.lg))
        Text(
            text = headline,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(HavnTheme.spacing.xs))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(Modifier.height(HavnTheme.spacing.lg))
            HavnButton(
                text = "Try again",
                onClick = onRetry,
                tone = HavnButtonTone.Secondary,
                size = HavnButtonSize.Medium,
                fillWidth = false,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  L O A D I N G
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Skeletons, not spinners. A spinner says "wait"; a skeleton says "here is the
 * shape of what is arriving", which makes the same wait feel shorter.
 *
 * The sheen sweeps rather than pulses — a pulse at list scale reads as the
 * screen flashing.
 */
@Composable
fun HavnSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    cornerRadius: Dp = 6.dp,
    widthFraction: Float = 1f,
) {
    val colors = HavnTheme.colors
    val transition = rememberInfiniteTransition(label = "skeleton")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeletonSweep",
    )

    val base = colors.surfaceSunken
    val sheen = if (colors.isDark) {
        colors.surfaceRaised.copy(alpha = 0.7f)
    } else {
        colors.surfaceRaised
    }

    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(base)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, sheen, Color.Transparent),
                    startX = -300f + progress * 1100f,
                    endX = 0f + progress * 1100f,
                )
            )
    )
}

/** The shape of a medication row, used while today's doses resolve. */
@Composable
fun HavnDoseRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HavnTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(HavnTheme.colors.surfaceSunken)
        )
        Spacer(Modifier.width(HavnTheme.spacing.lg))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(HavnTheme.spacing.sm),
        ) {
            HavnSkeleton(height = 15.dp, widthFraction = 0.55f)
            HavnSkeleton(height = 11.dp, widthFraction = 0.3f)
        }
        Spacer(Modifier.width(HavnTheme.spacing.lg))
        HavnSkeleton(height = 12.dp, widthFraction = 0.16f)
    }
}

/**
 * Three dots easing in and out of view. Used only inside buttons, where a
 * skeleton makes no sense.
 */
@Composable
fun HavnPulseDots(
    color: Color,
    modifier: Modifier = Modifier,
    dotSize: Dp = 6.dp,
) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = InfiniteRepeatableSpec(
                    animation = tween(560, delayMillis = index * 140, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$index",
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
        }
    }
}

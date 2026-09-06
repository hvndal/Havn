package com.havn.app.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.havn.app.ui.theme.SurfaceHigh
import com.havn.app.ui.theme.SurfaceLow
import com.havn.app.ui.theme.SurfaceMid

/**
 * Checks if the system reduced motion setting is enabled.
 */
@Composable
fun isReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Applies responsive, subtle button press feedback scaling.
 */
fun Modifier.havnPressFeedback(
    pressedScale: Float = 0.96f,
    onClick: (() -> Unit)? = null,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val reducedMotion = isReducedMotionEnabled()

    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (!reducedMotion) {
            scaleAnim.animateTo(
                targetValue = if (isPressed) pressedScale else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
        } else {
            scaleAnim.snapTo(1f)
        }
    }

    this
        .scale(scaleAnim.value)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )
}

/**
 * Shimmer modifier for skeleton loading states.
 */
fun Modifier.shimmerLoading(
    enabled: Boolean = true,
): Modifier = composed {
    if (!enabled) return@composed this

    val reducedMotion = isReducedMotionEnabled()
    if (reducedMotion) {
        return@composed this.background(SurfaceMid)
    }

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        SurfaceLow,
        SurfaceHigh,
        SurfaceLow,
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )

    this.background(brush)
}

/**
 * Staggered entrance animation wrapper.
 */
@Composable
fun StaggeredFadeIn(
    index: Int = 0,
    delayPerItemMillis: Int = 40,
    durationMillis: Int = 300,
    content: @Composable () -> Unit,
) {
    val reducedMotion = isReducedMotionEnabled()
    if (reducedMotion) {
        content()
        return
    }

    val alpha = remember { Animatable(0f) }
    val translateY = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        val totalDelay = (index * delayPerItemMillis).toLong()
        kotlinx.coroutines.delay(totalDelay)
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
        )
    }

    LaunchedEffect(Unit) {
        val totalDelay = (index * delayPerItemMillis).toLong()
        kotlinx.coroutines.delay(totalDelay)
        translateY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = translateY.value
        }
    ) {
        content()
    }
}

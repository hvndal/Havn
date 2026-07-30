package com.havn.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Editorial Staggered Letter Text Reveal Animation
 * Animates each character sliding up with a gentle fade-in delay.
 */
@Composable
fun StaggeredTextReveal(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    letterDelayMs: Long = 60L,
    initialDelayMs: Long = 200L,
) {
    val charList = remember(text) { text.map { it.toString() } }
    val anims = remember(text) {
        charList.map { Animatable(0f) }
    }

    LaunchedEffect(text) {
        delay(initialDelayMs)
        anims.forEachIndexed { i, anim ->
            kotlinx.coroutines.launch {
                delay(i * letterDelayMs)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = EaseOutCubic,
                    )
                )
            }
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        charList.forEachIndexed { index, char ->
            val progress = anims[index].value
            val yOffset = (1f - progress) * 20f
            Text(
                text = char,
                style = style,
                color = color,
                modifier = Modifier
                    .alpha(progress)
                    .offset(y = yOffset.dp),
            )
        }
    }
}

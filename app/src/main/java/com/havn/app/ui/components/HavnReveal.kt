package com.havn.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import com.havn.app.ui.theme.HavnMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The house entrance: content rises a short distance while fading in.
 *
 * Travel is deliberately small (12dp by default). Long-travel entrances are the
 * single clearest tell of an over-animated app — they read as the UI being
 * assembled in front of you rather than already being there.
 */
fun Modifier.havnReveal(
    progress: Float,
    travelDp: Float = 12f,
): Modifier = this.graphicsLayer {
    alpha = progress
    translationY = (1f - progress) * travelDp * density
}

/**
 * Drives a reveal on first composition. [delayMs] staggers siblings.
 *
 * Returns a plain Float so callers can apply it to whatever property suits —
 * offset, alpha, scale — rather than being locked to one animation shape.
 */
@Composable
fun rememberRevealProgress(
    key: Any? = Unit,
    delayMs: Int = 0,
    enabled: Boolean = true,
): Float {
    val anim = remember(key) { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(key, enabled) {
        if (!enabled) {
            anim.snapTo(1f)
            return@LaunchedEffect
        }
        if (delayMs > 0) delay(delayMs.toLong())
        anim.animateTo(1f, animationSpec = HavnMotion.enter())
    }
    return anim.value
}

/**
 * Staggered reveal for the few genuinely editorial moments — onboarding
 * greetings and the like.
 *
 * Staggers by *word*, not by character. The previous version laid out one
 * `Text` per character, which destroyed kerning between every pair of letters
 * and made TalkBack announce the phrase one letter at a time. Word granularity
 * keeps each word's typography intact and keeps the announcement legible,
 * while still giving the line a sense of being written.
 */
@Composable
fun HavnTextReveal(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    delayMs: Int = 120,
    enabled: Boolean = true,
) {
    val words = remember(text) { text.split(" ") }
    val anims = remember(text) { words.map { Animatable(if (enabled) 0f else 1f) } }

    LaunchedEffect(text, enabled) {
        if (!enabled) {
            anims.forEach { it.snapTo(1f) }
            return@LaunchedEffect
        }
        delay(delayMs.toLong())
        anims.forEachIndexed { index, anim ->
            launch {
                delay(index * 70L)
                anim.animateTo(1f, animationSpec = HavnMotion.enter())
            }
        }
    }

    // Word-level stagger keeps kerning intact inside each word while still
    // giving the phrase a sense of being written.
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        words.forEachIndexed { index, word ->
            val p = anims[index].value
            Text(
                text = if (index == words.lastIndex) word else "$word ",
                style = style,
                color = color,
                modifier = Modifier.graphicsLayer {
                    alpha = p
                    translationY = (1f - p) * 14f * this.density
                },
            )
        }
    }
}


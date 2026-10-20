package com.havn.app.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

// ─────────────────────────────────────────────────────────────────────────────
//  M O T I O N
//
//  Swiss (design/swiss/HANDOFF.md):
//   1. One curve — Decisive, cubic-bezier(0.2, 0, 0, 1). Fast start, clean stop.
//   2. No springs, no bounce, no overshoot. Things cut into place.
//   3. Three durations: State 140 ms, Nav 220 ms, Sheet 260 ms.
//   4. Stagger 28 ms between siblings; 12 ms per cell in a grid wave.
//   5. Reduce motion on: decorative motion stops, state changes still show.
// ─────────────────────────────────────────────────────────────────────────────

object HavnMotion {

    // ── Duration ─────────────────────────────────────────────────────────────
    const val Instant = 90      // micro flips inside a control
    const val Quick = 140       // State: checks, toggles, colour changes
    const val Standard = 220    // Nav: screen cuts, list reveals
    const val Considered = 260  // Sheet: sheets, large surfaces
    const val Ambient = 1400    // slow, time-driven drift only

    const val Stagger = 28
    const val CellWave = 12

    // ── Easing ───────────────────────────────────────────────────────────────
    /** The house curve. Everything uses it. */
    val Decisive: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val Enter: Easing = Decisive
    val Exit: Easing = Decisive
    val Smooth: Easing = Decisive

    // ── Former springs, now plain tweens on the house curve ─────────────────
    /** Press feedback. */
    fun <T> press(): FiniteAnimationSpec<T> = tween(Quick, easing = Decisive)

    /** Elements settling into place — rows, reveals. */
    fun <T> settle(): FiniteAnimationSpec<T> = tween(Standard, easing = Decisive)

    /** Larger travel: sheets, hero transitions. */
    fun <T> glide(): FiniteAnimationSpec<T> = tween(Considered, easing = Decisive)

    /** A completed dose: the same crisp cut as any other state change. */
    fun <T> celebrate(): FiniteAnimationSpec<T> = tween(Quick, easing = Decisive)

    // ── Tweens ───────────────────────────────────────────────────────────────
    fun <T> quick(): FiniteAnimationSpec<T> = tween(Quick, easing = Decisive)
    fun <T> standard(): FiniteAnimationSpec<T> = tween(Standard, easing = Decisive)
    fun <T> enter(delayMs: Int = 0): FiniteAnimationSpec<T> =
        tween(Standard, delayMillis = delayMs, easing = Decisive)
    fun <T> exit(): FiniteAnimationSpec<T> = tween(Quick, easing = Decisive)

    /** Theme switch: a cut, not a slow dissolve. */
    fun <T> theme(): FiniteAnimationSpec<T> = tween(Standard, easing = Decisive)

    /** Spec that jumps straight to the end — for reduced motion. */
    fun <T> none(): FiniteAnimationSpec<T> = snap()

    /** Stagger delay for the nth item in a revealing list, capped so long
     *  lists never make the user wait on an animation. */
    fun staggerDelay(index: Int, step: Int = Stagger, max: Int = 280): Int =
        (index * step).coerceAtMost(max)
}

/**
 * True when the user has turned animations off (Developer options or
 * Accessibility → Remove animations both set the animator scale to 0).
 * Decorative and looping motion should stop; state changes still show.
 */
fun Context.isReduceMotionOn(): Boolean =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) { context.isReduceMotionOn() }
}

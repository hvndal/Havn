package com.havn.app.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

// ─────────────────────────────────────────────────────────────────────────────
//  M O T I O N
//
//  Principles, in order:
//   1. Motion clarifies where something came from. It is never decoration.
//   2. Springs for anything a finger touches — a finger expects mass.
//   3. Tweens for anything the system does on its own (fades, colour shifts).
//   4. Fast. Nothing an impatient user waits on exceeds ~280ms.
//   5. Damping stays high (0.75–0.9). Bounce reads as toy, not instrument.
// ─────────────────────────────────────────────────────────────────────────────

object HavnMotion {

    // ── Duration ─────────────────────────────────────────────────────────────
    const val Instant = 90      // state flips: check, toggle tick
    const val Quick = 160       // press feedback, small fades
    const val Standard = 240    // the default — enters, colour, cross-fades
    const val Considered = 380  // large surfaces, hero reveals
    const val Ambient = 1400    // background drift, breathing

    // ── Easing ───────────────────────────────────────────────────────────────
    /** Decelerate — things arriving on screen. Confident, never floaty. */
    val Enter: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    /** Accelerate — things leaving. Gets out of the way quickly. */
    val Exit: Easing = CubicBezierEasing(0.5f, 0f, 0.9f, 0.2f)
    /** Symmetric — colour and opacity changes with no directional meaning. */
    val Smooth: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    // ── Springs ──────────────────────────────────────────────────────────────
    /** The press response. Snappy, settles without visible overshoot. */
    fun <T> press(): AnimationSpec<T> = spring(
        dampingRatio = 0.82f,
        stiffness = 900f,
    )

    /** Elements settling into place — cards, sheets, reveals. */
    fun <T> settle(): AnimationSpec<T> = spring(
        dampingRatio = 0.86f,
        stiffness = 380f,
    )

    /** Larger travel: sheets, hero transitions. Slightly softer landing. */
    fun <T> glide(): AnimationSpec<T> = spring(
        dampingRatio = 0.9f,
        stiffness = 220f,
    )

    /** The one place a little life is allowed: a completed dose. */
    fun <T> celebrate(): AnimationSpec<T> = spring(
        dampingRatio = 0.55f,
        stiffness = 700f,
    )

    // ── Tweens ───────────────────────────────────────────────────────────────
    fun <T> quick(): FiniteAnimationSpec<T> = tween(Quick, easing = Smooth)
    fun <T> standard(): FiniteAnimationSpec<T> = tween(Standard, easing = Smooth)
    fun <T> enter(delayMs: Int = 0): FiniteAnimationSpec<T> =
        tween(Considered, delayMillis = delayMs, easing = Enter)
    fun <T> exit(): FiniteAnimationSpec<T> = tween(Quick, easing = Exit)

    /** Theme cross-fade — slow enough to read as a dissolve, not a flash. */
    fun <T> theme(): FiniteAnimationSpec<T> = tween(420, easing = Smooth)

    /** Stagger delay for the nth item in a revealing list, capped so long
     *  lists never make the user wait on an animation. */
    fun staggerDelay(index: Int, step: Int = 45, max: Int = 360): Int =
        (index * step).coerceAtMost(max)
}



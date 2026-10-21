package com.havn.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
//  S E M A N T I C   T O K E N S
//
//  Screens describe *intent* ("this is a raised surface", "this is secondary
//  text") and the active theme decides the pigment. That indirection is what
//  makes a genuine dark art direction possible instead of an inversion.
// ─────────────────────────────────────────────────────────────────────────────

@Immutable
data class HavnColorTokens(
    val isDark: Boolean,

    // Ground
    val canvas: Color,          // the page itself
    val surface: Color,         // a sheet resting on the canvas
    val surfaceRaised: Color,   // a sheet above that — sheets, menus, dialogs
    val surfaceSunken: Color,   // wells: inputs, track grooves, insets
    val surfacePressed: Color,  // momentary press state

    // Line
    val hairline: Color,        // 1px separation, barely there
    val hairlineStrong: Color,  // the 2 dp ink rule: section start, what's next

    // Type
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,

    // Brand
    val accent: Color,          // ink — filled controls, checks, selection
    val accentPressed: Color,
    val onAccent: Color,        // type that sits *on* an accent fill
    val accentSoft: Color,      // pale accent fill
    val onAccentSoft: Color,
    val accentMuted: Color,     // de-emphasised brand, e.g. inactive glyphs

    // Signal — the one orange. The next thing to do, or something missed.
    val signal: Color,
    val signalPressed: Color,
    val onSignal: Color,        // type that sits on a signal fill
    val signalSoft: Color,
    val onSignalSoft: Color,

    // Signals
    val positive: Color,
    val positiveSoft: Color,
    val onPositiveSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val onWarningSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val onDangerSoft: Color,

    // Atmosphere
    val ambientA: Color,        // ambient field gradient stop
    val ambientB: Color,
    val scrim: Color,           // behind modals
    val shadowTint: Color,      // ambient/spot shadow colour
    val glow: Color,            // dark-mode accent bloom

    // Medication accents
    val medSage: Color,
    val medClay: Color,
    val medAmber: Color,
    val medSlate: Color,
    val medSand: Color,

    // The organiser's second material — the warm half of the icon's shell.
    val linen: Color,
)

/** Light: paper, ink, one orange. */
val HavnLightTokens = HavnColorTokens(
    isDark = false,

    canvas = Paper,
    surface = Paper,
    surfaceRaised = PaperRaised,
    surfaceSunken = PaperSunken,
    surfacePressed = PaperPressed,

    hairline = Rule,
    hairlineStrong = Ink,

    textPrimary = Ink,
    textSecondary = Ink2,
    textTertiary = Ink3Text,
    textDisabled = Ink3,

    // The "accent" is ink. Filled buttons, checks and selection are ink on
    // paper; orange is reserved for [signal].
    accent = Ink,
    accentPressed = Ink2,
    onAccent = Paper,
    accentSoft = PaperSunken,
    onAccentSoft = Ink,
    accentMuted = Ink3,

    signal = Signal,
    signalPressed = SignalPressed,
    onSignal = Paper,
    signalSoft = SignalSoft,
    onSignalSoft = SignalInk,

    // Taken is solid ink. Missed and destructive are the one orange.
    positive = Ink,
    positiveSoft = PaperSunken,
    onPositiveSoft = Ink,
    warning = Signal,
    warningSoft = SignalSoft,
    onWarningSoft = SignalInk,
    danger = Signal,
    dangerSoft = SignalSoft,
    onDangerSoft = SignalInk,

    // Flat ground: the ambient field resolves to the page itself, and there is
    // no shadow or glow anywhere.
    ambientA = Paper,
    ambientB = Paper,
    scrim = Color(0x6614140E),
    shadowTint = Color.Transparent,
    glow = Color.Transparent,

    // Per-medication colour collapses to an ink ladder (no hues besides orange).
    medSage = Ink,
    medClay = Ink2,
    medAmber = Color(0xFF3A3B33),
    medSlate = Ink3Text,
    medSand = Ink3,

    linen = PaperSunken,
)

/**
 * Dark: warm near-black with pale ink. The orange is lifted (#FF5A1F) so it
 * reads as light on the dark page; text sits on it in night paper.
 */
val HavnDarkTokens = HavnColorTokens(
    isDark = true,

    canvas = NightPaper,
    surface = NightPaper,
    surfaceRaised = NightPaperRaised,
    surfaceSunken = NightPaperSunken,
    surfacePressed = NightPaperPressed,

    hairline = NightRule,
    hairlineStrong = NightInk,

    textPrimary = NightInk,
    textSecondary = NightInk2,
    textTertiary = NightInk3Text,
    textDisabled = NightInk3,

    accent = NightInk,
    accentPressed = NightInk2,
    onAccent = NightPaper,
    accentSoft = NightPaperRaised,
    onAccentSoft = NightInk,
    accentMuted = NightInk3,

    signal = NightSignal,
    signalPressed = NightSignalPressed,
    onSignal = NightPaper,
    signalSoft = NightSignalSoft,
    onSignalSoft = NightSignal,

    positive = NightInk,
    positiveSoft = NightPaperRaised,
    onPositiveSoft = NightInk,
    warning = NightSignal,
    warningSoft = NightSignalSoft,
    onWarningSoft = NightSignal,
    danger = NightSignal,
    dangerSoft = NightSignalSoft,
    onDangerSoft = NightSignal,

    ambientA = NightPaper,
    ambientB = NightPaper,
    scrim = Color(0xAA000000),
    shadowTint = Color.Transparent,
    glow = Color.Transparent,

    medSage = NightInk,
    medClay = NightInk2,
    medAmber = Color(0xFFC4C5BB),
    medSlate = NightInk3Text,
    medSand = NightInk3,

    linen = NightPaperRaised,
)

// ─────────────────────────────────────────────────────────────────────────────
//  S P A C E
//
//  A single 4pt-derived ladder. Every gap in the app comes from here, which is
//  what makes the rhythm feel deliberate rather than improvised.
// ─────────────────────────────────────────────────────────────────────────────

@Immutable
data class HavnSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
    val huge: Dp = 64.dp,

    /** The page's side margin. Everything full-width aligns to this. */
    val gutter: Dp = 20.dp,
    /** Vertical rhythm between major editorial sections. */
    val section: Dp = 44.dp,
)

/**
 * Soft, standard corners: rounded groups and pill controls, the language set
 * by Today. Swiss stays in the type, the palette and the single orange.
 */
@Immutable
data class HavnRadius(
    val xs: Dp = 6.dp,
    val sm: Dp = 10.dp,
    val md: Dp = 14.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 28.dp,
    val pill: Dp = 999.dp,
)

/**
 * Swiss: nothing floats. Depth is a rule or a second paper tone, never a
 * shadow, so every step resolves to 0 dp.
 */
@Immutable
data class HavnElevation(
    val none: Dp = 0.dp,
    val rest: Dp = 0.dp,
    val raised: Dp = 0.dp,
    val floating: Dp = 0.dp,
    val overlay: Dp = 0.dp,
)

val LocalHavnColors = staticCompositionLocalOf { HavnLightTokens }
val LocalHavnSpacing = staticCompositionLocalOf { HavnSpacing() }
val LocalHavnRadius = staticCompositionLocalOf { HavnRadius() }
val LocalHavnElevation = staticCompositionLocalOf { HavnElevation() }

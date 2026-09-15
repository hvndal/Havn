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
    val hairlineStrong: Color,  // deliberate division

    // Type
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,

    // Brand
    val accent: Color,          // sage, corrected per theme
    val accentPressed: Color,
    val onAccent: Color,        // type that sits *on* an accent fill
    val accentSoft: Color,      // pale accent fill
    val onAccentSoft: Color,
    val accentMuted: Color,     // de-emphasised brand, e.g. inactive glyphs

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
)

/** Light: warm paper, ink, and a grounded sage. */
val HavnLightTokens = HavnColorTokens(
    isDark = false,

    canvas = Paper05,
    surface = Paper00,
    surfaceRaised = PureWhite,
    surfaceSunken = Paper10,
    surfacePressed = Paper20,

    hairline = Paper20,
    hairlineStrong = Paper30,

    textPrimary = Ink00,
    textSecondary = Ink40,
    textTertiary = Ink60,
    textDisabled = Ink80,

    accent = Sage,
    accentPressed = SageDeep,
    onAccent = PureWhite,
    accentSoft = SageSoft,
    onAccentSoft = SageInk,
    accentMuted = SageMuted,

    positive = Sage,
    positiveSoft = SageSoft,
    onPositiveSoft = SageInk,
    warning = Amber,
    warningSoft = AmberSoft,
    onWarningSoft = Color(0xFF3B2D07),
    danger = Danger,
    dangerSoft = DangerSoft,
    onDangerSoft = Color(0xFF52140F),

    ambientA = Color(0xFFE6EDE2),
    ambientB = Color(0xFFF7E9DE),
    scrim = Color(0x6615170F),
    shadowTint = Color(0xFF3F4439),
    glow = Color(0x00000000), // light mode earns depth from shadow, not glow

    medSage = Sage,
    medClay = Clay,
    medAmber = Amber,
    medSlate = AccentSlate,
    medSand = AccentSand,
)

/**
 * Dark: warm green-black. Brand hues are *lifted* — the light-mode sage
 * (#516351) has too little luminance to read as an accent on ink, so the dark
 * theme substitutes a pale sage and flips which tone carries the type.
 */
val HavnDarkTokens = HavnColorTokens(
    isDark = true,

    canvas = Forest05,
    surface = Forest10,
    surfaceRaised = Forest20,
    surfaceSunken = Forest00,
    surfacePressed = Forest30,

    hairline = Forest20,
    hairlineStrong = Forest40,

    textPrimary = Bone00,
    textSecondary = Bone40,
    textTertiary = Bone60,
    textDisabled = Bone80,

    accent = SageLift,
    accentPressed = SageLiftDim,
    onAccent = Color(0xFF17230F),
    accentSoft = SageFillDark,
    onAccentSoft = SageLift,
    accentMuted = SageLiftDim,

    positive = SageLift,
    positiveSoft = SageFillDark,
    onPositiveSoft = SageLift,
    warning = AmberLift,
    warningSoft = AmberFillDark,
    onWarningSoft = AmberLift,
    danger = DangerLift,
    dangerSoft = DangerFillDark,
    onDangerSoft = DangerLift,

    ambientA = Color(0xFF1B2A1E),
    ambientB = Color(0xFF2A211B),
    scrim = Color(0xAA000000),
    shadowTint = Color(0xFF000000),
    glow = Color(0x26A8C0A4),

    medSage = SageLift,
    medClay = ClayLift,
    medAmber = AmberLift,
    medSlate = AccentSlateLift,
    medSand = AccentSandLift,
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
    val gutter: Dp = 24.dp,
    /** Vertical rhythm between major editorial sections. */
    val section: Dp = 44.dp,
)

@Immutable
data class HavnRadius(
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 22.dp,
    val xl: Dp = 28.dp,
    val xxl: Dp = 36.dp,
    val pill: Dp = 999.dp,
)

/**
 * Elevation is expressed as shadow *distance*; the colour comes from
 * [HavnColorTokens.shadowTint] so dark mode gets deep, near-black shadow while
 * light mode gets a soft warm one.
 */
@Immutable
data class HavnElevation(
    val none: Dp = 0.dp,
    val rest: Dp = 2.dp,
    val raised: Dp = 8.dp,
    val floating: Dp = 16.dp,
    val overlay: Dp = 28.dp,
)

val LocalHavnColors = staticCompositionLocalOf { HavnLightTokens }
val LocalHavnSpacing = staticCompositionLocalOf { HavnSpacing() }
val LocalHavnRadius = staticCompositionLocalOf { HavnRadius() }
val LocalHavnElevation = staticCompositionLocalOf { HavnElevation() }

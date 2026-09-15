package com.havn.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
//  H Ä V N   —   P A L E T T E
//
//  Warm minimalism. Paper, sage, clay, amber.
//  This file holds *raw pigments* only. Screens must never reference these
//  directly — they consume semantic tokens from HavnTokens.kt instead, so that
//  light and dark are two deliberate art directions rather than one inverted.
// ─────────────────────────────────────────────────────────────────────────────

// ── Paper (light canvas ladder) ──────────────────────────────────────────────
// A warm, slightly desaturated paper stock. Each step is a *small* lift so
// surfaces separate by hairline and shadow rather than by obvious grey jumps.
val Paper00 = Color(0xFFFCFBF8) // raised sheet — cards, sheets
val Paper05 = Color(0xFFF9F7F3) // canvas
val Paper10 = Color(0xFFF3F1EB) // recessed wells, inputs
val Paper20 = Color(0xFFEBE8E1) // pressed / selected fill
val Paper30 = Color(0xFFDFDBD2) // strong fill
val Paper40 = Color(0xFFCBC6BB) // hairline on tinted ground

// ── Ink (light foreground ladder) ────────────────────────────────────────────
val Ink00 = Color(0xFF15170F) // primary text — warm near-black, not pure
val Ink40 = Color(0xFF3F4439) // secondary text
val Ink60 = Color(0xFF6C7166) // tertiary text / metadata
val Ink80 = Color(0xFFA0A499) // disabled, placeholder

// ── Forest (dark canvas ladder) ──────────────────────────────────────────────
// Dark mode is its own art direction: a warm green-black, like ink on slate.
// Never pure #000 — that kills the depth these surfaces are built from.
val Forest00 = Color(0xFF0D0F0C) // deepest — scrim, behind sheets
val Forest05 = Color(0xFF121410) // canvas
val Forest10 = Color(0xFF191C17) // raised sheet — cards
val Forest20 = Color(0xFF21241E) // elevated / hover
val Forest30 = Color(0xFF2B2F27) // pressed, strong fill
val Forest40 = Color(0xFF3A3F35) // hairline on dark

// ── Bone (dark foreground ladder) ────────────────────────────────────────────
val Bone00 = Color(0xFFEDEBE4) // primary text on dark
val Bone40 = Color(0xFFBFC3B7) // secondary
val Bone60 = Color(0xFF8B9084) // tertiary
val Bone80 = Color(0xFF5E6359) // disabled

// ── Sage — the brand ─────────────────────────────────────────────────────────
val Sage         = Color(0xFF516351) // core brand (light accents)
val SageDeep     = Color(0xFF32402F) // pressed accent, dark text on pale sage
val SageInk      = Color(0xFF1C2A1C) // text on light sage fills
val SageSoft     = Color(0xFFE2EBDE) // pale fill, light mode
val SageMuted    = Color(0xFF8DA08C) // mid tone, works in both themes

// Dark-mode brand: sage lifts and desaturates so it reads as *light* on ink.
// A 0xFF516351 accent on Forest05 is unreadable; this is the corrected tone.
val SageLift     = Color(0xFFA8C0A4) // accent on dark
val SageLiftDim  = Color(0xFF7D9479) // secondary accent on dark
val SageFillDark = Color(0xFF232E21) // pale-sage equivalent on dark

// ── Clay (secondary) ─────────────────────────────────────────────────────────
val Clay         = Color(0xFF9A5637)
val ClaySoft     = Color(0xFFF6E0D4)
val ClayLift     = Color(0xFFE8A480) // on dark
val ClayFillDark = Color(0xFF33211A)

// ── Amber (tertiary / warning) ───────────────────────────────────────────────
val Amber        = Color(0xFF8A6B22)
val AmberSoft    = Color(0xFFF7E8C4)
val AmberLift    = Color(0xFFD9BA74) // on dark
val AmberFillDark = Color(0xFF2E2614)

// ── Signal ───────────────────────────────────────────────────────────────────
val Danger       = Color(0xFFA33228)
val DangerSoft   = Color(0xFFF8DFDB)
val DangerLift   = Color(0xFFE99A90)
val DangerFillDark = Color(0xFF331B18)

// ── Medication accents (shared, tuned per theme in HavnTokens) ───────────────
val AccentSlate  = Color(0xFF7E929A)
val AccentSand   = Color(0xFFA89878)
val AccentSlateLift = Color(0xFFA8BEC6)
val AccentSandLift  = Color(0xFFCFBE9C)

val PureWhite = Color(0xFFFFFFFF)

// ─────────────────────────────────────────────────────────────────────────────
//  Material 3 schemes
//
//  These exist so stock M3 components (Switch, ModalBottomSheet, TimePicker,
//  Snackbar…) inherit Hävn's identity without being restyled one by one.
//  Hävn's own surfaces read from HavnTokens instead.
// ─────────────────────────────────────────────────────────────────────────────

val HavnLightColors = lightColorScheme(
    primary = Sage,
    onPrimary = PureWhite,
    primaryContainer = SageSoft,
    onPrimaryContainer = SageInk,
    inversePrimary = SageLift,
    secondary = Clay,
    onSecondary = PureWhite,
    secondaryContainer = ClaySoft,
    onSecondaryContainer = Color(0xFF4A2414),
    tertiary = Amber,
    onTertiary = PureWhite,
    tertiaryContainer = AmberSoft,
    onTertiaryContainer = Color(0xFF3B2D07),
    error = Danger,
    onError = PureWhite,
    errorContainer = DangerSoft,
    onErrorContainer = Color(0xFF52140F),
    background = Paper05,
    onBackground = Ink00,
    surface = Paper05,
    onSurface = Ink00,
    surfaceVariant = Paper10,
    onSurfaceVariant = Ink40,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = Paper00,
    surfaceContainer = Paper05,
    surfaceContainerHigh = Paper10,
    surfaceContainerHighest = Paper20,
    outline = Ink80,
    outlineVariant = Paper30,
    scrim = Color(0xFF15170F),
    inverseSurface = Forest10,
    inverseOnSurface = Bone00,
    surfaceTint = Color.Transparent, // Hävn expresses depth with shadow, not tint
)

val HavnDarkColors = darkColorScheme(
    primary = SageLift,
    onPrimary = Color(0xFF17230F),
    primaryContainer = SageFillDark,
    onPrimaryContainer = SageLift,
    inversePrimary = Sage,
    secondary = ClayLift,
    onSecondary = Color(0xFF2E1408),
    secondaryContainer = ClayFillDark,
    onSecondaryContainer = ClayLift,
    tertiary = AmberLift,
    onTertiary = Color(0xFF241B05),
    tertiaryContainer = AmberFillDark,
    onTertiaryContainer = AmberLift,
    error = DangerLift,
    onError = Color(0xFF330A07),
    errorContainer = DangerFillDark,
    onErrorContainer = DangerLift,
    background = Forest05,
    onBackground = Bone00,
    surface = Forest05,
    onSurface = Bone00,
    surfaceVariant = Forest20,
    onSurfaceVariant = Bone40,
    surfaceContainerLowest = Forest00,
    surfaceContainerLow = Forest05,
    surfaceContainer = Forest10,
    surfaceContainerHigh = Forest20,
    surfaceContainerHighest = Forest30,
    outline = Bone80,
    outlineVariant = Forest40,
    scrim = Color(0xFF000000),
    inverseSurface = Paper00,
    inverseOnSurface = Ink00,
    surfaceTint = Color.Transparent,
)

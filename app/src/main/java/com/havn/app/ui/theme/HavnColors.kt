package com.havn.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
//  H Ä V N   —   P A L E T T E
//
//  Swiss. Paper, ink, one orange. (design/swiss/HANDOFF.md)
//  This file holds *raw pigments* only. Screens must never reference these
//  directly — they consume semantic tokens from HavnTokens.kt instead, so that
//  light and dark are two deliberate art directions rather than one inverted.
//
//  Contrast, measured (WCAG 2.x):
//   • Ink on Paper 16.3:1 · Ink2 on Paper 6.1:1 · Ink3Text on Paper 5.0:1
//   • Signal on Paper 4.8:1 · Paper on Signal 4.8:1
//   • Ink3 is 2.9:1 — rules, ticks and empty cells only, never text.
// ─────────────────────────────────────────────────────────────────────────────

// ── Paper (light ground) ─────────────────────────────────────────────────────
val Paper        = Color(0xFFF2F1EC) // the page
val PaperRaised  = Color(0xFFFBFAF7) // sheets and dialogs above the page
val PaperSunken  = Color(0xFFEAE9E3) // the second paper tone: wells, inputs
val PaperPressed = Color(0xFFE1E0D9) // momentary press

// ── Ink (light foreground) ───────────────────────────────────────────────────
val Ink      = Color(0xFF14140E) // primary text, 2 dp rules, filled checks
val Ink2     = Color(0xFF5A5B50) // secondary text
val Ink3Text = Color(0xFF64655A) // tertiary text — the lightest grey that is still text
val Ink3     = Color(0xFF8E8F85) // non-text only: ticks, empty cells, disabled
val Rule     = Color(0xFFD3D2C9) // 1 dp hairline

// ── Night paper (dark ground) ────────────────────────────────────────────────
// Its own art direction: warm near-black, never #000.
val NightPaper        = Color(0xFF121210)
val NightPaperRaised  = Color(0xFF1A1A17)
val NightPaperSunken  = Color(0xFF0C0C0A)
val NightPaperPressed = Color(0xFF24241F)

// ── Night ink (dark foreground) ──────────────────────────────────────────────
val NightInk      = Color(0xFFEDECE4)
val NightInk2     = Color(0xFF9C9D93)
val NightInk3Text = Color(0xFF86877D)
val NightInk3     = Color(0xFF6A6B62)
val NightRule     = Color(0xFF2A2A26)

// ── Signal — the one orange ──────────────────────────────────────────────────
// One orange element on screen at a time: the next thing to do, or something
// missed. Lifted on dark so it reads as light rather than a smudge.
val Signal          = Color(0xFFC23A05)
val SignalPressed   = Color(0xFF9E2F04)
val SignalSoft      = Color(0xFFF5E2D7)
val SignalInk       = Color(0xFF8A2A04) // text on SignalSoft
val NightSignal        = Color(0xFFFF5A1F)
val NightSignalPressed = Color(0xFFE04A14)
val NightSignalSoft    = Color(0xFF2A1A12)

// ─────────────────────────────────────────────────────────────────────────────
//  Material 3 schemes
//
//  These exist so stock M3 components (Switch, ModalBottomSheet, TimePicker,
//  Snackbar…) inherit Hävn's identity without being restyled one by one.
//  Hävn's own surfaces read from HavnTokens instead. Primary is ink, not
//  orange: orange is reserved for one element per screen.
// ─────────────────────────────────────────────────────────────────────────────

val HavnLightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    primaryContainer = PaperSunken,
    onPrimaryContainer = Ink,
    inversePrimary = NightInk,
    secondary = Ink2,
    onSecondary = Paper,
    secondaryContainer = PaperSunken,
    onSecondaryContainer = Ink,
    tertiary = Signal,
    onTertiary = Paper,
    tertiaryContainer = SignalSoft,
    onTertiaryContainer = SignalInk,
    error = Signal,
    onError = Paper,
    errorContainer = SignalSoft,
    onErrorContainer = SignalInk,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperSunken,
    onSurfaceVariant = Ink2,
    surfaceContainerLowest = PaperRaised,
    surfaceContainerLow = PaperRaised,
    surfaceContainer = Paper,
    surfaceContainerHigh = PaperSunken,
    surfaceContainerHighest = PaperPressed,
    outline = Ink3,
    outlineVariant = Rule,
    scrim = Ink,
    inverseSurface = NightPaperRaised,
    inverseOnSurface = NightInk,
    surfaceTint = Color.Transparent, // flat: no tonal elevation
)

val HavnDarkColors = darkColorScheme(
    primary = NightInk,
    onPrimary = NightPaper,
    primaryContainer = NightPaperRaised,
    onPrimaryContainer = NightInk,
    inversePrimary = Ink,
    secondary = NightInk2,
    onSecondary = NightPaper,
    secondaryContainer = NightPaperRaised,
    onSecondaryContainer = NightInk,
    tertiary = NightSignal,
    onTertiary = NightPaper,
    tertiaryContainer = NightSignalSoft,
    onTertiaryContainer = NightSignal,
    error = NightSignal,
    onError = NightPaper,
    errorContainer = NightSignalSoft,
    onErrorContainer = NightSignal,
    background = NightPaper,
    onBackground = NightInk,
    surface = NightPaper,
    onSurface = NightInk,
    surfaceVariant = NightPaperRaised,
    onSurfaceVariant = NightInk2,
    surfaceContainerLowest = NightPaperSunken,
    surfaceContainerLow = NightPaper,
    surfaceContainer = NightPaperRaised,
    surfaceContainerHigh = NightPaperRaised,
    surfaceContainerHighest = NightPaperPressed,
    outline = NightInk3,
    outlineVariant = NightRule,
    scrim = Color(0xFF000000),
    inverseSurface = PaperRaised,
    inverseOnSurface = Ink,
    surfaceTint = Color.Transparent,
)

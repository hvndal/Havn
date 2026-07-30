package com.havn.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Hävn Design System — Warm Minimalism ──────────────────────────────────────
// Palette inspired by Muji, Kinfolk, Scandinavian industrial design

// Surface tiers
val WarmIvory        = Color(0xFFFBF9F5) // primary background canvas
val SurfaceLow       = Color(0xFFF5F3EF)
val SurfaceMid       = Color(0xFFEFEEEA)
val SurfaceHigh      = Color(0xFFEAE8E4)
val SurfaceHighest   = Color(0xFFE4E2DE)
val SurfaceDim       = Color(0xFFDBDAD6)
val White            = Color(0xFFFFFFFF)

// Text
val Charcoal         = Color(0xFF1B1C1A)
val CharcoalMid      = Color(0xFF434842)
val StoneGrey        = Color(0xFF747872)
val StoneLight       = Color(0xFFC3C8C0)

// Primary — Muted Sage
val Sage             = Color(0xFF516351)
val SageLight        = Color(0xFF8DA08C)
val SageLighter      = Color(0xFFB8CCB6)
val SagePale         = Color(0xFFD4E8D2)
val OnSage           = Color(0xFFFFFFFF)
val SageDark         = Color(0xFF263727)
val SageMid          = Color(0xFF3A4B3B)
val SageDeep         = Color(0xFF0F1F11)

// Secondary — Terracotta
val Terracotta       = Color(0xFF8A4F33)
val TerracottaLight  = Color(0xFFFEB28F)
val TerracottaPale   = Color(0xFFFFDBCC)
val OnTerracotta     = Color(0xFFFFFFFF)

// Tertiary — Butter / Amber
val ButterAmber      = Color(0xFF735B23)
val ButterLight      = Color(0xFFB49759)
val ButterPale       = Color(0xFFFFDF9C)
val ButterDim        = Color(0xFFE2C381)
val OnButter         = Color(0xFFFFFFFF)

// Dark surface equivalents
val DarkSurface      = Color(0xFF12130F)
val DarkSurfaceMid   = Color(0xFF1C1D1A)
val DarkSurfaceHigh  = Color(0xFF252622)
val DarkSurfaceHighest = Color(0xFF30312E)
val OnDarkSurface    = Color(0xFFE4E2DE)
val OnDarkSurfaceVar = Color(0xFFC4C9BF)

// Error
val ErrorRed         = Color(0xFFBA1A1A)
val ErrorContainer   = Color(0xFFFFDAD6)

// Light color scheme (primary)
val HavnLightColors = lightColorScheme(
    primary = Sage,
    onPrimary = OnSage,
    primaryContainer = SageLight,
    onPrimaryContainer = SageDark,
    secondary = Terracotta,
    onSecondary = OnTerracotta,
    secondaryContainer = TerracottaPale,
    onSecondaryContainer = Color(0xFF794227),
    tertiary = ButterAmber,
    onTertiary = OnButter,
    tertiaryContainer = ButterLight,
    onTertiaryContainer = Color(0xFF423000),
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFF93000A),
    background = WarmIvory,
    onBackground = Charcoal,
    surface = WarmIvory,
    onSurface = Charcoal,
    surfaceVariant = SurfaceHighest,
    onSurfaceVariant = CharcoalMid,
    outline = StoneGrey,
    outlineVariant = StoneLight,
    inverseSurface = DarkSurfaceHighest,
    inverseOnSurface = Color(0xFFF2F0ED),
    inversePrimary = SageLighter,
    surfaceTint = Sage,
)

val HavnDarkColors = darkColorScheme(
    primary = SageLighter,
    onPrimary = SageDeep,
    primaryContainer = SageMid,
    onPrimaryContainer = SagePale,
    secondary = TerracottaLight,
    onSecondary = Color(0xFF351000),
    secondaryContainer = Color(0xFF6D391E),
    onSecondaryContainer = TerracottaPale,
    tertiary = ButterDim,
    onTertiary = Color(0xFF251A00),
    tertiaryContainer = Color(0xFF59440D),
    onTertiaryContainer = ButterPale,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = ErrorContainer,
    background = DarkSurface,
    onBackground = OnDarkSurface,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceHighest,
    onSurfaceVariant = OnDarkSurfaceVar,
    outline = Color(0xFF8E9389),
    outlineVariant = DarkSurfaceHighest,
    inverseSurface = OnDarkSurface,
    inverseOnSurface = DarkSurface,
    inversePrimary = Sage,
    surfaceTint = SageLighter,
)

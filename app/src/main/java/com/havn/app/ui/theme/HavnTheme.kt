package com.havn.app.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** The user's theme choice. Persisted in [com.havn.app.data.prefs.UserPreferences]. */
enum class ThemeMode { LIGHT, DARK, SYSTEM;
    companion object {
        fun from(raw: String?): ThemeMode = when (raw?.uppercase()) {
            "LIGHT" -> LIGHT
            "DARK" -> DARK
            else -> SYSTEM
        }
    }
}

/**
 * Accessor object so screens read tokens as `HavnTheme.colors.textSecondary`,
 * mirroring how `MaterialTheme.colorScheme` reads. Keeps call sites short and
 * makes it obvious when something reaches for a raw pigment instead.
 */
object HavnTheme {
    val colors: HavnColorTokens
        @Composable @ReadOnlyComposable get() = LocalHavnColors.current
    val spacing: HavnSpacing
        @Composable @ReadOnlyComposable get() = LocalHavnSpacing.current
    val radius: HavnRadius
        @Composable @ReadOnlyComposable get() = LocalHavnRadius.current
    val elevation: HavnElevation
        @Composable @ReadOnlyComposable get() = LocalHavnElevation.current
}

val HavnShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HavnTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val target = if (darkTheme) HavnDarkTokens else HavnLightTokens

    // Every token is animated, so flipping the theme dissolves across the whole
    // app at once rather than snapping. The cost is one colour animation per
    // token — trivial next to a recomposition, and it is the difference between
    // "a setting changed" and "the app changed its mind".
    val colors = target.animated()

    val materialScheme = if (darkTheme) HavnDarkColors else HavnLightColors
    val animatedScheme = materialScheme.animated(darkTheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        // Edge-to-edge with transparent bars: content runs under the status and
        // navigation bars, and only the icon tint flips with the theme. Setting
        // a solid statusBarColor (as this previously did) is deprecated on 35
        // and produces a visible band above immersive headers.
        DisposableEffect(darkTheme) {
            val activity = view.context as? ComponentActivity
            activity?.enableEdgeToEdge(
                statusBarStyle = if (darkTheme) {
                    SystemBarStyle.dark(Color.Transparent.toArgb())
                } else {
                    SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb())
                },
                navigationBarStyle = if (darkTheme) {
                    SystemBarStyle.dark(Color.Transparent.toArgb())
                } else {
                    SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb())
                },
            )
            onDispose { }
        }
    }

    CompositionLocalProvider(
        LocalHavnColors provides colors,
        LocalHavnSpacing provides remember { HavnSpacing() },
        LocalHavnRadius provides remember { HavnRadius() },
        LocalHavnElevation provides remember { HavnElevation() },
    ) {
        MaterialTheme(
            colorScheme = animatedScheme,
            typography = HavnTypography,
            shapes = HavnShapes,
            content = content,
        )
    }
}

@Composable
private fun HavnColorTokens.animated(): HavnColorTokens {
    @Composable
    fun c(value: Color, label: String) =
        animateColorAsState(value, HavnMotion.theme(), label = label).value

    return copy(
        canvas = c(canvas, "canvas"),
        surface = c(surface, "surface"),
        surfaceRaised = c(surfaceRaised, "surfaceRaised"),
        surfaceSunken = c(surfaceSunken, "surfaceSunken"),
        surfacePressed = c(surfacePressed, "surfacePressed"),
        hairline = c(hairline, "hairline"),
        hairlineStrong = c(hairlineStrong, "hairlineStrong"),
        textPrimary = c(textPrimary, "textPrimary"),
        textSecondary = c(textSecondary, "textSecondary"),
        textTertiary = c(textTertiary, "textTertiary"),
        textDisabled = c(textDisabled, "textDisabled"),
        accent = c(accent, "accent"),
        accentPressed = c(accentPressed, "accentPressed"),
        onAccent = c(onAccent, "onAccent"),
        accentSoft = c(accentSoft, "accentSoft"),
        onAccentSoft = c(onAccentSoft, "onAccentSoft"),
        accentMuted = c(accentMuted, "accentMuted"),
        positive = c(positive, "positive"),
        positiveSoft = c(positiveSoft, "positiveSoft"),
        onPositiveSoft = c(onPositiveSoft, "onPositiveSoft"),
        warning = c(warning, "warning"),
        warningSoft = c(warningSoft, "warningSoft"),
        onWarningSoft = c(onWarningSoft, "onWarningSoft"),
        danger = c(danger, "danger"),
        dangerSoft = c(dangerSoft, "dangerSoft"),
        onDangerSoft = c(onDangerSoft, "onDangerSoft"),
        ambientA = c(ambientA, "ambientA"),
        ambientB = c(ambientB, "ambientB"),
        shadowTint = c(shadowTint, "shadowTint"),
        glow = c(glow, "glow"),
        medSage = c(medSage, "medSage"),
        medClay = c(medClay, "medClay"),
        medAmber = c(medAmber, "medAmber"),
        medSlate = c(medSlate, "medSlate"),
        medSand = c(medSand, "medSand"),
    )
}

/**
 * Only the surfaces M3 actually paints are animated here. Animating all ~30
 * roles would add a lot of state for colours the user never sees move.
 */
@Composable
private fun ColorScheme.animated(darkTheme: Boolean): ColorScheme {
    @Composable
    fun c(value: Color, label: String) =
        animateColorAsState(value, HavnMotion.theme(), label = label).value

    return copy(
        background = c(background, "m3bg"),
        onBackground = c(onBackground, "m3onBg"),
        surface = c(surface, "m3surface"),
        onSurface = c(onSurface, "m3onSurface"),
        surfaceVariant = c(surfaceVariant, "m3surfaceVariant"),
        onSurfaceVariant = c(onSurfaceVariant, "m3onSurfaceVariant"),
        surfaceContainer = c(surfaceContainer, "m3container"),
        surfaceContainerHigh = c(surfaceContainerHigh, "m3containerHigh"),
        surfaceContainerLow = c(surfaceContainerLow, "m3containerLow"),
        primary = c(primary, "m3primary"),
        onPrimary = c(onPrimary, "m3onPrimary"),
        outline = c(outline, "m3outline"),
        outlineVariant = c(outlineVariant, "m3outlineVariant"),
    )
}

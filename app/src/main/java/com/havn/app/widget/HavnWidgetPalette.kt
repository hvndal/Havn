package com.havn.app.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider

/**
 * The widget's palette.
 *
 * Every colour in the widget used to be a hard-coded light-mode literal, so on
 * a dark home screen it sat there as a bright ivory panel among dark widgets.
 *
 * These are day/night providers, which the launcher resolves against its own
 * configuration. A Glance widget renders in the launcher's process, so it
 * cannot read the app's in-app theme preference — following the system theme
 * is both the only option and the right behaviour for home-screen content,
 * which has to sit beside other widgets.
 *
 * Values mirror HavnColors.kt (paper, ink, one orange) so the widget and the
 * app read as one product.
 */
object WidgetPalette {

    // Ground
    val canvas = ColorProvider(day = Color(0xFFF2F1EC), night = Color(0xFF121210))
    val surface = ColorProvider(day = Color(0xFFF2F1EC), night = Color(0xFF121210))
    val surfaceSunken = ColorProvider(day = Color(0xFFEAE9E3), night = Color(0xFF0C0C0A))
    val hairline = ColorProvider(day = Color(0xFFD3D2C9), night = Color(0xFF2A2A26))

    // Type
    val textPrimary = ColorProvider(day = Color(0xFF14140E), night = Color(0xFFEDECE4))
    val textSecondary = ColorProvider(day = Color(0xFF5A5B50), night = Color(0xFF9C9D93))
    val textTertiary = ColorProvider(day = Color(0xFF64655A), night = Color(0xFF86877D))

    // Ink is the accent; the one orange is reserved for the next dose.
    val accent = ColorProvider(day = Color(0xFF14140E), night = Color(0xFFEDECE4))
    val accentDeep = ColorProvider(day = Color(0xFF14140E), night = Color(0xFFEDECE4))
    val onAccent = ColorProvider(day = Color(0xFFF2F1EC), night = Color(0xFF121210))
    val accentSoft = ColorProvider(day = Color(0xFFEAE9E3), night = Color(0xFF1A1A17))
    val onAccentSoft = ColorProvider(day = Color(0xFF14140E), night = Color(0xFFEDECE4))
    val signal = ColorProvider(day = Color(0xFFC23A05), night = Color(0xFFFF5A1F))

    /** Row treatment for a dose already taken. */
    val takenRow = ColorProvider(day = Color(0xFFF2F1EC), night = Color(0xFF121210))
    val takenBubble = ColorProvider(day = Color(0xFF14140E), night = Color(0xFFEDECE4))
    val idleBubble = ColorProvider(day = Color(0xFFEAE9E3), night = Color(0xFF1A1A17))
}

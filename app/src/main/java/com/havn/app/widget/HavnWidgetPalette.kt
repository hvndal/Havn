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
 * Values mirror HavnColors.kt so the widget and the app read as one product.
 */
object WidgetPalette {

    // Ground
    val canvas = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF121410))
    val surface = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF191C17))
    val surfaceSunken = ColorProvider(day = Color(0xFFF5F6F3), night = Color(0xFF0D0F0C))
    val hairline = ColorProvider(day = Color(0xFFECEDE9), night = Color(0xFF21241E))

    // Type
    val textPrimary = ColorProvider(day = Color(0xFF1B241A), night = Color(0xFFEDEBE4))
    val textSecondary = ColorProvider(day = Color(0xFF454D44), night = Color(0xFFBFC3B7))
    val textTertiary = ColorProvider(day = Color(0xFF737A71), night = Color(0xFF8B9084))

    // Brand — lifted on dark so it reads as an accent rather than a smudge
    val accent = ColorProvider(day = Color(0xFF5E6E5D), night = Color(0xFFA8C0A4))
    val accentDeep = ColorProvider(day = Color(0xFF3E4B3D), night = Color(0xFFA8C0A4))
    val onAccent = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF17230F))
    val accentSoft = ColorProvider(day = Color(0xFFEEF1EC), night = Color(0xFF232E21))
    val onAccentSoft = ColorProvider(day = Color(0xFF233022), night = Color(0xFFA8C0A4))

    /** Row treatment for a dose already taken. */
    val takenRow = ColorProvider(day = Color(0xFFF7F8F5), night = Color(0xFF171B15))
    val takenBubble = ColorProvider(day = Color(0xFFE3E9E1), night = Color(0xFF263023))
    val idleBubble = ColorProvider(day = Color(0xFFEEEFEB), night = Color(0xFF21241E))
}

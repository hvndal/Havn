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
    val canvas = ColorProvider(day = Color(0xFFF9F7F3), night = Color(0xFF121410))
    val surface = ColorProvider(day = Color(0xFFFCFBF8), night = Color(0xFF191C17))
    val surfaceSunken = ColorProvider(day = Color(0xFFF3F1EB), night = Color(0xFF0D0F0C))
    val hairline = ColorProvider(day = Color(0xFFEBE8E1), night = Color(0xFF21241E))

    // Type
    val textPrimary = ColorProvider(day = Color(0xFF15170F), night = Color(0xFFEDEBE4))
    val textSecondary = ColorProvider(day = Color(0xFF3F4439), night = Color(0xFFBFC3B7))
    val textTertiary = ColorProvider(day = Color(0xFF6C7166), night = Color(0xFF8B9084))

    // Brand — lifted on dark so it reads as an accent rather than a smudge
    val accent = ColorProvider(day = Color(0xFF516351), night = Color(0xFFA8C0A4))
    val accentDeep = ColorProvider(day = Color(0xFF32402F), night = Color(0xFFA8C0A4))
    val onAccent = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF17230F))
    val accentSoft = ColorProvider(day = Color(0xFFE2EBDE), night = Color(0xFF232E21))
    val onAccentSoft = ColorProvider(day = Color(0xFF1C2A1C), night = Color(0xFFA8C0A4))

    /** Row treatment for a dose already taken. */
    val takenRow = ColorProvider(day = Color(0xFFF2F6F0), night = Color(0xFF171B15))
    val takenBubble = ColorProvider(day = Color(0xFFDCE8DA), night = Color(0xFF263023))
    val idleBubble = ColorProvider(day = Color(0xFFEDE9E3), night = Color(0xFF21241E))
}

package com.havn.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.havn.app.R

// ─────────────────────────────────────────────────────────────────────────────
//  T Y P E
//
//  Two voices, used for different jobs — this pairing is what gives Hävn an
//  editorial character instead of a UI-kit one:
//
//   • Instrument Serif  — the *masthead* voice. High contrast, optical, used
//     large and sparingly: screen titles, hero numerals, the wordmark. It does
//     the work that cards and borders would otherwise have to do.
//
//   • Hanken Grotesk    — the *instrument* voice. Everything functional:
//     body, labels, data, controls. Quiet, highly legible at small sizes.
//
//  Weight discipline: nothing above SemiBold anywhere in the app. Emphasis
//  comes from scale, colour and space — not from bolder text.
// ─────────────────────────────────────────────────────────────────────────────

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val HankenGrotesk = FontFamily(
    Font(GoogleFont("Hanken Grotesk"), provider, FontWeight.Light),
    Font(GoogleFont("Hanken Grotesk"), provider, FontWeight.Normal),
    Font(GoogleFont("Hanken Grotesk"), provider, FontWeight.Medium),
    Font(GoogleFont("Hanken Grotesk"), provider, FontWeight.SemiBold),
)

/** Display serif. Single weight by design — it is never asked to be bold. */
val EditorialSerif = FontFamily(
    Font(GoogleFont("Instrument Serif"), provider, FontWeight.Normal),
    Font(GoogleFont("Instrument Serif"), provider, FontWeight.Normal, FontStyle.Italic),
)

/**
 * Trim the extra leading Android reserves above/below the first and last line.
 * Without this, large display text sits visually low in its box and no amount
 * of padding tuning makes an oversized headline optically align.
 */
private val Trim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.Both,
)

@Suppress("DEPRECATION")
private val NoFontPadding = PlatformTextStyle(includeFontPadding = false)

private fun serif(
    size: Int,
    lineHeight: Int,
    letterSpacing: Float,
    weight: FontWeight = FontWeight.Normal,
) = TextStyle(
    fontFamily = EditorialSerif,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = Trim,
)

private fun sans(
    size: Int,
    lineHeight: Int,
    letterSpacing: Float,
    weight: FontWeight = FontWeight.Normal,
) = TextStyle(
    fontFamily = HankenGrotesk,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = Trim,
)

val HavnTypography = Typography(
    // ── Display — serif, editorial. Negative tracking at scale. ──────────────
    displayLarge = serif(size = 64, lineHeight = 64, letterSpacing = -1.6f),
    displayMedium = serif(size = 48, lineHeight = 50, letterSpacing = -1.0f),
    displaySmall = serif(size = 36, lineHeight = 40, letterSpacing = -0.6f),

    // ── Headline — serif, screen titles ──────────────────────────────────────
    headlineLarge = serif(size = 32, lineHeight = 36, letterSpacing = -0.5f),
    headlineMedium = serif(size = 26, lineHeight = 31, letterSpacing = -0.3f),
    headlineSmall = sans(size = 22, lineHeight = 28, letterSpacing = -0.2f, weight = FontWeight.Medium),

    // ── Title — sans, section and component headers ──────────────────────────
    titleLarge = sans(size = 20, lineHeight = 26, letterSpacing = -0.2f, weight = FontWeight.Medium),
    titleMedium = sans(size = 17, lineHeight = 23, letterSpacing = -0.1f, weight = FontWeight.Medium),
    titleSmall = sans(size = 15, lineHeight = 20, letterSpacing = 0f, weight = FontWeight.Medium),

    // ── Body — generous leading, this is what gets read ──────────────────────
    bodyLarge = sans(size = 17, lineHeight = 26, letterSpacing = -0.1f),
    bodyMedium = sans(size = 15, lineHeight = 23, letterSpacing = 0f),
    bodySmall = sans(size = 13, lineHeight = 19, letterSpacing = 0.1f),

    // ── Label — controls and metadata ────────────────────────────────────────
    labelLarge = sans(size = 15, lineHeight = 20, letterSpacing = 0f, weight = FontWeight.Medium),
    labelMedium = sans(size = 13, lineHeight = 17, letterSpacing = 0.1f, weight = FontWeight.Medium),
    labelSmall = sans(size = 11, lineHeight = 15, letterSpacing = 0.3f, weight = FontWeight.Medium),
)

/**
 * Styles outside the Material scale that Hävn uses repeatedly.
 */
object HavnType {
    /**
     * The small uppercase rule that labels an editorial section. Wide tracking
     * is what makes it read as a *rule* rather than shouting.
     */
    val Eyebrow = sans(size = 11, lineHeight = 14, letterSpacing = 1.4f, weight = FontWeight.Medium)

    /** Same, but quieter — inside surfaces where the eyebrow shouldn't compete. */
    val EyebrowQuiet = sans(size = 10, lineHeight = 13, letterSpacing = 1.2f, weight = FontWeight.Medium)

    /** Large figures: adherence %, dose counts. Tabular so digits don't jitter. */
    val Metric = serif(size = 56, lineHeight = 56, letterSpacing = -2.0f)
    val MetricSmall = serif(size = 30, lineHeight = 32, letterSpacing = -0.8f)

    /** Times of day in lists — mono-ish alignment via tabular figures. */
    val Clock = sans(size = 14, lineHeight = 18, letterSpacing = 0.2f, weight = FontWeight.Medium)

    /** The wordmark. Tracked out, serif, never bold. */
    val Wordmark = serif(size = 22, lineHeight = 24, letterSpacing = 3.0f)
}

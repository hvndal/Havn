package com.havn.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.havn.app.R

// ─────────────────────────────────────────────────────────────────────────────
//  T Y P E
//
//  Two voices (design/swiss/HANDOFF.md):
//
//   • Archivo        — everything you read. Display sizes are Bold with tight
//     tracking (−4 to −5 %) and 0.9 leading; that oversized type is the hero
//     of every screen and does the work cards and shadows used to do.
//
//   • IBM Plex Mono  — labels, times and metadata. Small, tracked +8 %, set in
//     capitals at the call site (Compose has no text-transform).
//
//  Every Archivo style turns on tabular figures, so times, doses, counts and
//  percentages never jitter as they change. Plex Mono is monospaced, so its
//  digits line up already.
// ─────────────────────────────────────────────────────────────────────────────

// Bundled (OFL, licences in assets/licenses), not downloaded: no font swap on
// first launch and no dependency on Google Play services being present.
val Archivo = FontFamily(
    Font(R.font.archivo_regular, FontWeight.Normal),
    Font(R.font.archivo_medium, FontWeight.Medium),
    Font(R.font.archivo_semibold, FontWeight.SemiBold),
    Font(R.font.archivo_bold, FontWeight.Bold),
)

val PlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
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

private const val Tabular = "tnum"

/** Archivo. [tracking] is a fraction of the font size (−0.05 = −5 %). */
private fun archivo(
    size: Int,
    lineHeight: Int,
    tracking: Float,
    weight: FontWeight = FontWeight.Normal,
) = TextStyle(
    fontFamily = Archivo,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = (size * tracking).sp,
    fontFeatureSettings = Tabular,
    platformStyle = NoFontPadding,
    lineHeightStyle = Trim,
)

/** IBM Plex Mono, tracked +8 % by default. */
private fun mono(
    size: Int,
    lineHeight: Int,
    tracking: Float = 0.08f,
    weight: FontWeight = FontWeight.Medium,
) = TextStyle(
    fontFamily = PlexMono,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = (size * tracking).sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = Trim,
)

val HavnTypography = Typography(
    // ── Display — Archivo Bold, the hero of each screen ─────────────────────
    displayLarge = archivo(size = 64, lineHeight = 58, tracking = -0.05f, weight = FontWeight.Bold),
    displayMedium = archivo(size = 48, lineHeight = 44, tracking = -0.045f, weight = FontWeight.Bold),
    displaySmall = archivo(size = 36, lineHeight = 34, tracking = -0.04f, weight = FontWeight.Bold),

    // ── Headline — screen and section titles ────────────────────────────────
    headlineLarge = archivo(size = 34, lineHeight = 32, tracking = -0.04f, weight = FontWeight.Bold),
    headlineMedium = archivo(size = 28, lineHeight = 28, tracking = -0.035f, weight = FontWeight.Bold),
    headlineSmall = archivo(size = 22, lineHeight = 24, tracking = -0.02f, weight = FontWeight.SemiBold),

    // ── Title — rows and component headers ──────────────────────────────────
    titleLarge = archivo(size = 20, lineHeight = 24, tracking = -0.015f, weight = FontWeight.SemiBold),
    titleMedium = archivo(size = 17, lineHeight = 22, tracking = -0.01f, weight = FontWeight.SemiBold),
    titleSmall = archivo(size = 15, lineHeight = 20, tracking = -0.005f, weight = FontWeight.SemiBold),

    // ── Body — 1.4 leading, this is what gets read ──────────────────────────
    bodyLarge = archivo(size = 17, lineHeight = 24, tracking = -0.005f),
    bodyMedium = archivo(size = 15, lineHeight = 21, tracking = 0f),
    bodySmall = archivo(size = 13, lineHeight = 18, tracking = 0f),

    // ── Label — controls are Archivo; small metadata is mono ────────────────
    labelLarge = archivo(size = 15, lineHeight = 20, tracking = 0f, weight = FontWeight.SemiBold),
    labelMedium = archivo(size = 13, lineHeight = 17, tracking = 0f, weight = FontWeight.Medium),
    labelSmall = mono(size = 11, lineHeight = 14),
)

/**
 * Styles outside the Material scale that Hävn uses repeatedly.
 */
object HavnType {
    /**
     * The one giant element per screen: the next dose time, "That's
     * everything.", 94 %. Bold, −5 % tracking, 0.9 leading.
     */
    val Poster = archivo(size = 112, lineHeight = 100, tracking = -0.05f, weight = FontWeight.Bold)

    /**
     * The small uppercase mono label above a section or value. Pass text in
     * capitals; the wide tracking is what makes it read as a label, not a shout.
     */
    val Eyebrow = mono(size = 11, lineHeight = 14)

    /** Same, but quieter — inside rows where the label shouldn't compete. */
    val EyebrowQuiet = mono(size = 10, lineHeight = 13, weight = FontWeight.Normal)

    /** Large figures: adherence %, dose counts. Tabular so digits don't jitter. */
    val Metric = archivo(size = 64, lineHeight = 58, tracking = -0.05f, weight = FontWeight.Bold)
    val MetricSmall = archivo(size = 34, lineHeight = 32, tracking = -0.04f, weight = FontWeight.Bold)

    /** Times of day in lists: the mono time column (08:00 · 13:00). */
    val Clock = mono(size = 13, lineHeight = 16, tracking = 0.02f)

    /** The wordmark HÄVN: small mono capitals, tracked wide. */
    val Wordmark = mono(size = 13, lineHeight = 16, tracking = 0.16f)
}

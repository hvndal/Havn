package com.havn.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.havn.app.domain.model.DayPeriod
import com.havn.app.domain.model.DoseStatus
import com.havn.app.domain.model.MedIconType
import com.havn.app.domain.model.TodayDose
import com.havn.app.ui.screens.home.medAccent
import com.havn.app.ui.theme.HankenGrotesk
import com.havn.app.ui.theme.HavnTheme
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/**
 * The Hävn organiser — the product, drawn as the object it is.
 *
 * A four-compartment day box seen from a little above and in front, built in
 * the same two materials as the launcher icon: white lids over a linen body,
 * drawn with the icon's olive line. Each lid is hinged at the back and swings
 * open in perspective; the selected part of the day stands open to show the
 * actual doses inside — capsules, tablets, softgels — in each medication's
 * colour. Doses already taken leave a faint outline where they lay, so the box
 * empties through the day exactly like a real one.
 *
 * Everything is one Canvas: no WebView, no model file, no extra renderer.
 */
@Composable
fun HavnOrganizerView(
    doses: List<TodayDose>,
    selectedPeriod: DayPeriod,
    onSlotTapped: (DayPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periods = DayPeriod.entries
    val colors = HavnTheme.colors
    val textMeasurer = rememberTextMeasurer()
    val currentPeriod = remember { DayPeriod.current() }

    // Resolve medication colours here — the draw scope is not composable.
    val tagColors = mapOf(
        "sage" to medAccent("sage"),
        "clay" to medAccent("clay"),
        "amber" to medAccent("amber"),
        "slate" to medAccent("slate"),
        "sand" to medAccent("sand"),
    )

    // One hinge per compartment. Animatable (rather than animateFloatAsState)
    // so the selected lid swings open on first appearance too, not only when
    // the selection changes.
    val hinges = remember { periods.map { Animatable(0f) } }
    periods.forEachIndexed { i, period ->
        val target = if (period == selectedPeriod) LID_OPEN_DEGREES else 0f
        LaunchedEffect(target) {
            hinges[i].animateTo(
                target,
                spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow),
            )
        }
    }

    val description = remember(doses) {
        buildString {
            append("Pill organiser. ")
            periods.forEach { p ->
                val inPeriod = doses.filter { it.period == p }
                val taken = inPeriod.count { it.isTaken }
                append("${p.label}: ")
                append(
                    when {
                        inPeriod.isEmpty() -> "empty. "
                        taken == inPeriod.size -> "all ${inPeriod.size} taken. "
                        else -> "$taken of ${inPeriod.size} taken. "
                    }
                )
            }
        }
    }

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val g = BoxGeometry(size.width.toFloat(), size.height.toFloat())
                    val index = ((offset.x - g.left) / g.width * 4f).toInt().coerceIn(0, 3)
                    onSlotTapped(periods[index])
                }
            }
    ) {
        val g = BoxGeometry(size.width, size.height)
        val line = colors.accent.copy(alpha = if (colors.isDark) 0.75f else 1f)
        val stroke = g.stroke

        val lidFill = colors.surface
        val bodyFill = if (colors.isDark) colors.linen else lerp(colors.linen, Color.White, 0.35f)
        val wellFill = colors.surfaceSunken

        // ── Ground shadow: a soft contact pool under the box ────────────────
        val shadowCenter = Offset(size.width / 2f, g.bottom + g.depth * 0.08f)
        scale(scaleX = 1f, scaleY = 0.16f, pivot = shadowCenter) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.shadowTint.copy(alpha = if (colors.isDark) 0.55f else 0.20f),
                        colors.shadowTint.copy(alpha = 0f),
                    ),
                    center = shadowCenter,
                    radius = g.width * 0.62f,
                ),
                radius = g.width * 0.62f,
                center = shadowCenter,
            )
        }

        // ── Body: top rim + linen front, one silhouette ─────────────────────
        val silhouette = roundedPolygon(
            listOf(
                g.backLeft, g.backRight,
                Offset(g.right, g.front), Offset(g.right, g.bottom),
                Offset(g.left, g.bottom), Offset(g.left, g.front),
            ),
            radius = g.corner,
        )
        drawPath(silhouette, lidFill)

        // Front face with a gentle top-to-bottom fall-off for volume.
        val frontFace = roundedPolygon(
            listOf(
                Offset(g.left, g.front), Offset(g.right, g.front),
                Offset(g.right, g.bottom), Offset(g.left, g.bottom),
            ),
            radius = g.corner,
            roundCorners = setOf(2, 3),
        )
        drawPath(frontFace, bodyFill)
        drawPath(
            frontFace,
            Brush.verticalGradient(
                listOf(Color.Transparent, colors.shadowTint.copy(alpha = if (colors.isDark) 0.25f else 0.06f)),
                startY = g.front,
                endY = g.bottom,
            ),
        )

        // ── Wells: drawn for every compartment, revealed as lids lift ───────
        for (i in 0 until 4) {
            val well = quad(g, i, uBack = 0.14f, uFront = 0.86f, inset = 0.11f)
            drawPath(well, wellFill)
            // The back inner wall catches shadow; the floor stays clean.
            clipPath(well) {
                val top = g.yAt(0.14f)
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            colors.shadowTint.copy(alpha = if (colors.isDark) 0.45f else 0.12f),
                            Color.Transparent,
                        ),
                        startY = top,
                        endY = top + g.depth * 0.32f,
                    ),
                    topLeft = Offset(g.left, top),
                    size = Size(g.width, g.depth),
                )
            }
            drawPath(well, line.copy(alpha = 0.35f), style = Stroke(stroke * 0.7f))

            drawContents(
                g = g,
                index = i,
                doses = doses.filter { it.period == periods[i] },
                tagColors = tagColors,
                fallback = colors.medSage,
                ghost = colors.hairlineStrong,
                check = colors.accent,
                shadow = colors.shadowTint,
                isDark = colors.isDark,
            )
        }

        // ── Front-face rules and labels ─────────────────────────────────────
        for (i in 1 until 4) {
            val x = g.frontX(i / 4f)
            drawLine(
                line.copy(alpha = 0.28f),
                Offset(x, g.front + g.frontHeight * 0.18f),
                Offset(x, g.bottom - g.frontHeight * 0.18f),
                strokeWidth = stroke * 0.7f,
            )
        }
        periods.forEachIndexed { i, period ->
            val selected = period == selectedPeriod
            val label = textMeasurer.measure(
                period.label.uppercase(),
                TextStyle(
                    fontFamily = HankenGrotesk,
                    fontSize = (g.width / 36f).coerceIn(8.5f * density, 11f * density).toSp(),
                    letterSpacing = 1.4.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) colors.accent else colors.textTertiary,
                ),
            )
            val cx = g.frontX((i + 0.5f) / 4f)
            val cy = g.front + g.frontHeight * 0.56f
            drawText(label, topLeft = Offset(cx - label.size.width / 2f, cy - label.size.height / 2f))
            if (period == currentPeriod) {
                drawCircle(colors.accent, radius = stroke * 1.6f, center = Offset(cx, g.front + g.frontHeight * 0.24f))
            }
        }

        // ── Lids, back to front in paint order ──────────────────────────────
        periods.forEachIndexed { i, period ->
            val inPeriod = doses.filter { it.period == period }
            drawLid(
                g = g,
                index = i,
                angleDeg = hinges[i].value,
                top = lidFill,
                underside = wellFill,
                line = line,
                allTaken = inPeriod.isNotEmpty() && inPeriod.all { it.isTaken },
                pending = inPeriod.count { it.isPending },
                mark = colors.accent,
                onMark = colors.onAccent,
            )
        }

        // Outline last so it sits crisply over every fill.
        drawPath(silhouette, line, style = Stroke(stroke, join = StrokeJoin.Round))
        drawLine(line, Offset(g.left, g.front), Offset(g.right, g.front), strokeWidth = stroke)
    }
}

private const val LID_OPEN_DEGREES = 108f

/** Projection of the box into a canvas. All sizes derive from the canvas. */
private class BoxGeometry(w: Float, h: Float) {
    val left = w * 0.07f
    val right = w * 0.93f
    val width = right - left

    /** Perspective: the back edge is narrower than the front. */
    private val persp = width * 0.045f
    val depth = min(h * 0.22f, width * 0.24f)
    val frontHeight = min(h * 0.20f, width * 0.2f)
    /** How tall a fully raised lid draws on screen. */
    val rise = depth * 1.1f

    val back = h * 0.40f
    val front = back + depth
    val bottom = front + frontHeight

    val backLeft = Offset(left + persp, back)
    val backRight = Offset(right - persp, back)

    val corner = width * 0.03f
    val stroke = (width / 260f).coerceIn(1.6f, 4f)

    fun frontX(f: Float) = left + width * f
    fun backX(f: Float) = backLeft.x + (backRight.x - backLeft.x) * f

    /** Screen x for fraction [f] across the box at depth [u] (0 back, 1 front). */
    fun x(f: Float, u: Float) = backX(f) + (frontX(f) - backX(f)) * u
    fun yAt(u: Float) = back + depth * u
}

/** A compartment-local quad on the top face, inset from the walls. */
private fun quad(g: BoxGeometry, i: Int, uBack: Float, uFront: Float, inset: Float): Path {
    val f0 = (i + inset) / 4f
    val f1 = (i + 1 - inset) / 4f
    return roundedPolygon(
        listOf(
            Offset(g.x(f0, uBack), g.yAt(uBack)),
            Offset(g.x(f1, uBack), g.yAt(uBack)),
            Offset(g.x(f1, uFront), g.yAt(uFront)),
            Offset(g.x(f0, uFront), g.yAt(uFront)),
        ),
        radius = g.corner * 0.8f,
    )
}

private fun DrawScope.drawLid(
    g: BoxGeometry,
    index: Int,
    angleDeg: Float,
    top: Color,
    underside: Color,
    line: Color,
    allTaken: Boolean,
    pending: Int,
    mark: Color,
    onMark: Color,
) {
    val gap = 0.035f
    val f0 = (index + gap) / 4f
    val f1 = (index + 1 - gap) / 4f
    val theta = Math.toRadians(angleDeg.toDouble())
    val forward = cos(theta).toFloat()
    val up = sin(theta).toFloat()

    // A point on the lid at length s (0 hinge … 1 tip), fraction f across.
    fun p(f: Float, s: Float): Offset {
        val u = s * forward
        return Offset(g.x(f, u), g.yAt(u) - s * up * g.rise)
    }

    val hingeL = p(f0, 0f)
    val hingeR = p(f1, 0f)
    val tipR = p(f1, 1f)
    val tipL = p(f0, 1f)
    val showingTop = forward > 0f
    val thickness = g.frontHeight * 0.09f

    // The lid's front lip — visible while the lid faces the viewer.
    if (forward > -0.2f) {
        val lip = roundedPolygon(
            listOf(tipL, tipR, tipR + Offset(0f, thickness), tipL + Offset(0f, thickness)),
            radius = thickness * 0.5f,
        )
        drawPath(lip, lerp(top, line, 0.10f))
        drawPath(lip, line, style = Stroke(g.stroke * 0.8f, join = StrokeJoin.Round))
    }

    val lid = roundedPolygon(listOf(hingeL, hingeR, tipR, tipL), radius = g.corner * 0.9f)
    drawPath(lid, if (showingTop) top else underside)
    if (!showingTop) {
        // Underside reads as the inside of the lid: a little shade at the hinge.
        drawPath(
            lid,
            Brush.verticalGradient(
                listOf(line.copy(alpha = 0f), line.copy(alpha = 0.08f)),
                startY = tipL.y,
                endY = hingeL.y,
            ),
        )
    }
    drawPath(lid, line, style = Stroke(g.stroke, join = StrokeJoin.Round))

    // Lid marks fade out as the lid lifts — they belong to the closed state.
    val markAlpha = (1f - angleDeg / 35f).coerceIn(0f, 1f)
    if (markAlpha <= 0f) return
    val c = p((f0 + f1) / 2f, 0.55f)
    val r = (g.width / 4f) * 0.075f
    if (allTaken) {
        drawCircle(mark.copy(alpha = markAlpha), r * 1.25f, c)
        val tick = Path().apply {
            moveTo(c.x - r * 0.55f, c.y + r * 0.02f)
            lineTo(c.x - r * 0.12f, c.y + r * 0.42f)
            lineTo(c.x + r * 0.6f, c.y - r * 0.38f)
        }
        drawPath(tick, onMark.copy(alpha = markAlpha), style = Stroke(g.stroke * 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    } else if (pending > 0) {
        // One quiet dot per dose still waiting, capped so the lid stays calm.
        val n = min(pending, 4)
        val step = r * 0.95f
        val start = c.x - step * (n - 1) / 2f
        repeat(n) { k ->
            drawCircle(mark.copy(alpha = markAlpha * 0.85f), r * 0.28f, Offset(start + step * k, c.y))
        }
    }
}

/** Doses inside a compartment, laid out in perspective on the well floor. */
private fun DrawScope.drawContents(
    g: BoxGeometry,
    index: Int,
    doses: List<TodayDose>,
    tagColors: Map<String, Color>,
    fallback: Color,
    ghost: Color,
    check: Color,
    shadow: Color,
    isDark: Boolean,
) {
    if (doses.isEmpty()) return
    val compW = g.width / 4f

    if (doses.all { it.isTaken }) {
        // Emptied: a single olive tick on the floor of the well.
        val c = Offset(g.x((index + 0.5f) / 4f, 0.58f), g.yAt(0.58f))
        val r = compW * 0.11f
        val tick = Path().apply {
            moveTo(c.x - r, c.y)
            lineTo(c.x - r * 0.25f, c.y + r * 0.7f)
            lineTo(c.x + r * 1.05f, c.y - r * 0.65f)
        }
        drawPath(tick, check, style = Stroke(g.stroke * 1.3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        return
    }

    val shown = doses.take(6)
    val cols = if (shown.size <= 2) shown.size else 3
    val rows = (shown.size + cols - 1) / cols
    shown.forEachIndexed { k, dose ->
        val col = k % cols
        val row = k / cols
        val fx = (index + 0.24f + 0.52f * (col + 0.5f) / cols) / 4f
        val u = if (rows == 1) 0.58f else 0.40f + 0.36f * row / (rows - 1)
        val depthScale = 0.86f + 0.14f * u
        val center = Offset(g.x(fx, u), g.yAt(u))
        val size = compW * 0.40f * depthScale * if (cols == 3) 0.74f else 1f
        val seed = (dose.medication.id * 31 + dose.slot.hashCode()).toInt()
        val tilt = -38f + (abs(seed) % 50)
        val tint = tagColors[dose.medication.colorTag] ?: fallback

        if (dose.status == DoseStatus.PENDING) {
            // Contact shadow first so the pill sits on the floor.
            drawOval(
                shadow.copy(alpha = if (isDark) 0.45f else 0.14f),
                topLeft = center + Offset(-size * 0.42f, size * 0.06f),
                size = Size(size * 0.84f, size * 0.30f),
            )
            drawPill(dose.medication.iconType, center, size, tilt, tint, ghost = null, stroke = g.stroke)
        } else {
            drawPill(dose.medication.iconType, center, size, tilt, tint, ghost = ghost, stroke = g.stroke)
        }
    }
}

/**
 * One dose, by form. With [ghost] set, draws only a faint outline — the
 * impression a taken pill leaves in an emptied compartment.
 */
private fun DrawScope.drawPill(
    type: MedIconType,
    c: Offset,
    size: Float,
    tiltDeg: Float,
    tint: Color,
    ghost: Color?,
    stroke: Float,
) {
    val light = lerp(tint, Color.White, 0.78f)
    val edge = lerp(tint, Color.Black, 0.18f)
    val outline = Stroke(stroke * 0.7f)

    fun body(color: Color) = ghost ?: color
    fun line() = ghost ?: edge

    rotate(tiltDeg, pivot = c) {
        when (type) {
            MedIconType.CAPSULE -> {
                val len = size
                val wid = size * 0.42f
                val tl = Offset(c.x - len / 2f, c.y - wid / 2f)
                val r = androidx.compose.ui.geometry.CornerRadius(wid / 2f)
                if (ghost == null) {
                    drawRoundRect(light, tl, Size(len, wid), r)
                    clipRect(left = c.x - len / 2f, top = c.y - wid, right = c.x, bottom = c.y + wid) {
                        drawRoundRect(tint, tl, Size(len, wid), r)
                    }
                    // A sliver of highlight along the shell.
                    drawLine(Color.White.copy(alpha = 0.55f), Offset(tl.x + wid * 0.6f, c.y - wid * 0.22f), Offset(c.x + len * 0.3f, c.y - wid * 0.22f), strokeWidth = wid * 0.12f, cap = StrokeCap.Round)
                }
                drawRoundRect(line(), tl, Size(len, wid), r, style = outline)
            }
            MedIconType.TABLET -> {
                val rad = size * 0.32f
                if (ghost == null) drawCircle(light, rad, c)
                drawCircle(line(), rad, c, style = outline)
                drawLine(body(edge.copy(alpha = 0.5f)), Offset(c.x - rad * 0.6f, c.y), Offset(c.x + rad * 0.6f, c.y), strokeWidth = stroke * 0.6f, cap = StrokeCap.Round)
            }
            MedIconType.LIQUID -> {
                // A softgel: glossy, translucent oval.
                val s = Size(size * 0.62f, size * 0.44f)
                val tl = Offset(c.x - s.width / 2f, c.y - s.height / 2f)
                if (ghost == null) {
                    drawOval(tint.copy(alpha = 0.88f), tl, s)
                    drawOval(Color.White.copy(alpha = 0.45f), tl + Offset(s.width * 0.18f, s.height * 0.16f), Size(s.width * 0.34f, s.height * 0.26f))
                }
                drawOval(line(), tl, s, style = outline)
            }
            MedIconType.POWDER -> {
                // A sachet with a crimped top.
                val s = Size(size * 0.62f, size * 0.52f)
                val tl = Offset(c.x - s.width / 2f, c.y - s.height / 2f)
                val r = androidx.compose.ui.geometry.CornerRadius(s.width * 0.12f)
                if (ghost == null) drawRoundRect(light, tl, s, r)
                drawRoundRect(line(), tl, s, r, style = outline)
                drawLine(body(edge.copy(alpha = 0.6f)), tl + Offset(0f, s.height * 0.22f), tl + Offset(s.width, s.height * 0.22f), strokeWidth = stroke * 0.6f)
            }
            MedIconType.INJECTION -> {
                // An ampoule: slim, with a narrowed neck.
                val len = size * 0.9f
                val wid = size * 0.24f
                val tl = Offset(c.x - len / 2f, c.y - wid / 2f)
                val r = androidx.compose.ui.geometry.CornerRadius(wid / 2f)
                if (ghost == null) drawRoundRect(lerp(tint, Color.White, 0.55f), tl, Size(len, wid), r)
                drawRoundRect(line(), tl, Size(len, wid), r, style = outline)
                val neck = c.x + len * 0.18f
                drawLine(body(edge.copy(alpha = 0.6f)), Offset(neck, c.y - wid / 2f), Offset(neck, c.y + wid / 2f), strokeWidth = stroke * 0.7f)
            }
        }
    }
}

/**
 * A closed polygon with every corner (or only [roundCorners]) softened by a
 * quadratic curve of roughly [radius]. Used for every shape on the box so the
 * whole object shares one corner language with the icon.
 */
private fun roundedPolygon(
    points: List<Offset>,
    radius: Float,
    roundCorners: Set<Int>? = null,
): Path {
    val n = points.size
    val path = Path()
    for (i in 0 until n) {
        val prev = points[(i - 1 + n) % n]
        val cur = points[i]
        val next = points[(i + 1) % n]
        val rounded = roundCorners == null || i in roundCorners
        if (!rounded) {
            if (i == 0) path.moveTo(cur.x, cur.y) else path.lineTo(cur.x, cur.y)
            continue
        }
        val toPrev = prev - cur
        val toNext = next - cur
        val lp = hypot(toPrev.x, toPrev.y)
        val ln = hypot(toNext.x, toNext.y)
        val r = min(radius, min(lp, ln) / 2f)
        val a = cur + toPrev * (r / lp)
        val b = cur + toNext * (r / ln)
        if (i == 0) path.moveTo(a.x, a.y) else path.lineTo(a.x, a.y)
        path.quadraticBezierTo(cur.x, cur.y, b.x, b.y)
    }
    path.close()
    return path
}

/**
 * One dose, large, resting in a small linen tray — the same drawing the
 * organiser uses, so what you pick while adding a medication is exactly what
 * later appears in the box. Form and colour changes cross-fade.
 */
@Composable
fun HavnPillSpecimen(
    type: MedIconType,
    colorTag: String,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val tint by androidx.compose.animation.animateColorAsState(
        targetValue = medAccent(colorTag),
        animationSpec = com.havn.app.ui.theme.HavnMotion.standard(),
        label = "specimenTint",
    )
    androidx.compose.animation.Crossfade(
        targetState = type,
        animationSpec = com.havn.app.ui.theme.HavnMotion.standard(),
        label = "specimenForm",
        modifier = modifier.semantics { contentDescription = "Preview: ${type.name.lowercase()}" },
    ) { form ->
        Canvas(Modifier.fillMaxSize()) {
            val stroke = (size.minDimension / 70f).coerceAtLeast(1.5f)
            val r = size.minDimension * 0.22f
            val tray = roundedPolygon(
                listOf(
                    Offset(0f, 0f), Offset(size.width, 0f),
                    Offset(size.width, size.height), Offset(0f, size.height),
                ),
                radius = r,
            )
            drawPath(tray, if (colors.isDark) colors.linen else lerp(colors.linen, Color.White, 0.45f))
            drawPath(tray, colors.accent.copy(alpha = 0.55f), style = Stroke(stroke))
            val c = Offset(size.width / 2f, size.height / 2f)
            val s = size.minDimension * 0.62f
            drawOval(
                colors.shadowTint.copy(alpha = if (colors.isDark) 0.45f else 0.14f),
                topLeft = c + Offset(-s * 0.42f, s * 0.12f),
                size = Size(s * 0.84f, s * 0.26f),
            )
            drawPill(form, c, s, -28f, tint, ghost = null, stroke = stroke * 1.2f)
        }
    }
}

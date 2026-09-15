package com.havn.app.ui.screens.progress

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.domain.model.DoseStatus
import com.havn.app.ui.components.HavnBreathingMark
import com.havn.app.ui.components.HavnEmptyState
import com.havn.app.ui.components.HavnSegmented
import com.havn.app.ui.components.HavnSkeleton
import com.havn.app.ui.components.havnPress
import com.havn.app.ui.components.havnPressFade
import com.havn.app.ui.components.havnReveal
import com.havn.app.ui.components.rememberRevealProgress
import com.havn.app.ui.screens.home.medAccent
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Progress — trends and calendar in one place.
 *
 * Merges the old Adherence and History tabs, which showed the same 30 days of
 * dose data in two shapes and occupied two of five bottom-bar slots.
 *
 * Also removes three things that were shipping as product but were development
 * scaffolding: a "Recharts / Compose Native" chart-engine switcher, a "Room DB"
 * badge next to the headline figure, and the subtitle "30-day analytics stored
 * in Room database". None of that means anything to someone taking medication.
 * The chart is now drawn natively, which also retires a WebView.
 */
@Composable
fun ProgressScreen(
    contentPadding: PaddingValues,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HavnTheme.colors
    val gutter = HavnTheme.spacing.gutter

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = contentPadding.calculateBottomPadding() + HavnTheme.spacing.xxl,
            ),
        ) {
            item(key = "header") {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = gutter)
                        .padding(top = HavnTheme.spacing.xl),
                ) {
                    Text(
                        text = "LAST 30 DAYS",
                        style = HavnType.Eyebrow,
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.sm))
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.textPrimary,
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.xl))
            }

            if (!uiState.hasMedications && !uiState.isLoading) {
                item(key = "empty") {
                    HavnEmptyState(
                        eyebrow = "Nothing to chart",
                        headline = "No history yet",
                        body = "Once you start logging doses, thirty days of adherence " +
                            "will appear here.",
                        illustration = { HavnBreathingMark() },
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                }
                return@LazyColumn
            }

            item(key = "headline") {
                if (uiState.isLoading) {
                    Column(Modifier.padding(horizontal = gutter)) {
                        HavnSkeleton(height = 56.dp, widthFraction = 0.45f)
                        Spacer(Modifier.height(HavnTheme.spacing.lg))
                        HavnSkeleton(height = 14.dp, widthFraction = 0.7f)
                    }
                } else {
                    HeadlineStat(
                        stats = uiState.stats,
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.xxl))
            }

            item(key = "switcher") {
                HavnSegmented(
                    options = listOf("Trends", "Calendar"),
                    selectedIndex = if (uiState.view == ProgressView.TRENDS) 0 else 1,
                    onSelect = {
                        viewModel.setView(
                            if (it == 0) ProgressView.TRENDS else ProgressView.CALENDAR
                        )
                    },
                    modifier = Modifier.padding(horizontal = gutter),
                )
                Spacer(Modifier.height(HavnTheme.spacing.xxl))
            }

            item(key = "body") {
                AnimatedContent(
                    targetState = uiState.view,
                    transitionSpec = {
                        fadeIn(HavnMotion.standard()) togetherWith fadeOut(HavnMotion.exit())
                    },
                    label = "progressView",
                ) { view ->
                    when (view) {
                        ProgressView.TRENDS -> TrendsPane(uiState)
                        ProgressView.CALENDAR -> CalendarPane(
                            state = uiState,
                            onPrevious = viewModel::previousMonth,
                            onNext = viewModel::nextMonth,
                            onSelect = viewModel::selectDate,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeadlineStat(stats: ProgressStats, modifier: Modifier = Modifier) {
    val colors = HavnTheme.colors
    val percent by animateFloatAsState(
        targetValue = stats.rate * 100f,
        animationSpec = HavnMotion.glide(),
        label = "ratePercent",
    )

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${percent.toInt()}",
                style = HavnType.Metric,
                color = colors.textPrimary,
            )
            Text(
                text = "%",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textTertiary,
                modifier = Modifier.padding(bottom = 10.dp, start = 2.dp),
            )
        }
        Spacer(Modifier.height(HavnTheme.spacing.sm))
        Text(
            text = "${stats.taken} of ${stats.scheduled} doses taken",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
        )

        Spacer(Modifier.height(HavnTheme.spacing.xl))

        Row(horizontalArrangement = Arrangement.spacedBy(HavnTheme.spacing.xxl)) {
            MiniStat("Streak", "${stats.currentStreak}", "days")
            MiniStat("Best", "${stats.bestStreak}", "days")
            MiniStat("Perfect", "${stats.perfectDays}", "days")
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, unit: String) {
    val colors = HavnTheme.colors
    Column {
        Text(text = label.uppercase(), style = HavnType.EyebrowQuiet, color = colors.textTertiary)
        Spacer(Modifier.height(HavnTheme.spacing.xs))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
            )
            Text(
                text = " $unit",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
    }
}

@Composable
private fun TrendsPane(state: ProgressUiState) {
    val gutter = HavnTheme.spacing.gutter
    val colors = HavnTheme.colors

    Column {
        Text(
            text = "DAILY ADHERENCE",
            style = HavnType.Eyebrow,
            color = colors.textTertiary,
            modifier = Modifier.padding(horizontal = gutter),
        )
        Spacer(Modifier.height(HavnTheme.spacing.lg))

        AdherenceChart(
            days = state.last30,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = gutter),
        )

        Spacer(Modifier.height(HavnTheme.spacing.sm))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = gutter),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("30 days ago", "2 weeks", "Today").forEach {
                Text(it, style = HavnType.EyebrowQuiet, color = colors.textTertiary)
            }
        }

        if (state.byMedication.isNotEmpty()) {
            Spacer(Modifier.height(HavnTheme.spacing.section))
            Text(
                text = "BY MEDICATION",
                style = HavnType.Eyebrow,
                color = colors.textTertiary,
                modifier = Modifier.padding(horizontal = gutter),
            )
            Spacer(Modifier.height(HavnTheme.spacing.lg))

            state.byMedication.forEachIndexed { index, record ->
                MedicationBar(
                    record = record,
                    index = index,
                    modifier = Modifier.padding(horizontal = gutter),
                )
            }
        }
    }
}

/**
 * A 30-day area chart, drawn natively.
 *
 * The previous implementation rendered Recharts inside a WebView — a full
 * browser engine, loaded from a data URL, to draw one line. This is the same
 * curve in a Canvas: no WebView, no page load, and it inherits the theme.
 */
@Composable
private fun AdherenceChart(days: List<DayRecord>, modifier: Modifier = Modifier) {
    val colors = HavnTheme.colors
    val reveal = rememberRevealProgress(key = days.size)

    if (days.isEmpty()) {
        Box(modifier)
        return
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val step = if (days.size > 1) w / (days.size - 1) else w
        val baseline = h

        val points = days.mapIndexed { index, day ->
            val value = if (day.hasData) day.rate.coerceIn(0f, 1f) else 0f
            Offset(index * step, h - (value * h * 0.88f) - h * 0.06f)
        }

        // Gridlines at 50% and 100%, hairline weight.
        listOf(0.06f, 0.5f).forEach { fraction ->
            val y = h - (h * 0.88f * (1f - fraction)) - h * 0.06f
            drawLine(
                color = colors.hairline,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
            )
        }

        val line = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val midX = (p1.x + p2.x) / 2f
                cubicTo(midX, p1.y, midX, p2.y, p2.x, p2.y)
            }
        }

        val fill = Path().apply {
            addPath(line)
            lineTo(w, baseline)
            lineTo(0f, baseline)
            close()
        }

        // Clip the reveal horizontally so the chart draws itself in from the
        // left rather than fading up as a whole block.
        clipRect(right = w * reveal) {
            drawPath(
                path = fill,
                brush = Brush.verticalGradient(
                    listOf(colors.accent.copy(alpha = 0.22f), Color.Transparent),
                ),
            )
            drawPath(
                path = line,
                color = colors.accent,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
            )
            val last = points.last()
            drawCircle(colors.canvas, radius = 5.dp.toPx(), center = last)
            drawCircle(colors.accent, radius = 3.5.dp.toPx(), center = last)
        }
    }
}

@Composable
private fun MedicationBar(
    record: MedRecord,
    index: Int,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val accent = medAccent(record.medication.colorTag)
    val reveal = rememberRevealProgress(
        key = record.medication.id,
        delayMs = HavnMotion.staggerDelay(index),
    )
    val width by animateFloatAsState(
        targetValue = record.rate.coerceIn(0f, 1f) * reveal,
        animationSpec = HavnMotion.glide(),
        label = "medBar",
    )

    Column(modifier = modifier.padding(bottom = HavnTheme.spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = record.medication.name,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${(record.rate * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
            )
        }
        Spacer(Modifier.height(HavnTheme.spacing.sm))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(CircleShape)
                .background(colors.hairline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(width)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Calendar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CalendarPane(
    state: ProgressUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    val colors = HavnTheme.colors
    val gutter = HavnTheme.spacing.gutter
    val canGoForward = state.month < YearMonth.now()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = gutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "‹",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textSecondary,
                modifier = Modifier
                    .clip(CircleShape)
                    .havnPressFade(onClick = onPrevious)
                    .padding(horizontal = HavnTheme.spacing.md),
            )
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineMedium,
                color = if (canGoForward) colors.textSecondary else colors.textDisabled,
                modifier = Modifier
                    .clip(CircleShape)
                    .havnPressFade(enabled = canGoForward, onClick = onNext)
                    .padding(horizontal = HavnTheme.spacing.md),
            )
        }

        Spacer(Modifier.height(HavnTheme.spacing.xl))

        Row(modifier = Modifier.padding(horizontal = gutter)) {
            DayOfWeek.entries.forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = HavnType.EyebrowQuiet,
                    color = colors.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(HavnTheme.spacing.md))

        MonthGrid(
            month = state.month,
            records = state.monthRecords,
            selected = state.selectedDate,
            onSelect = onSelect,
            modifier = Modifier.padding(horizontal = gutter),
        )

        val selected = state.selectedDate
        if (selected != null && state.selectedDayDoses.isNotEmpty()) {
            Spacer(Modifier.height(HavnTheme.spacing.section))
            Text(
                text = selected.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")).uppercase(),
                style = HavnType.Eyebrow,
                color = colors.textTertiary,
                modifier = Modifier.padding(horizontal = gutter),
            )
            Spacer(Modifier.height(HavnTheme.spacing.lg))

            state.selectedDayDoses.forEach { detail ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = gutter, vertical = HavnTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when (detail.status) {
                                    DoseStatus.TAKEN -> colors.accent
                                    DoseStatus.SKIPPED -> colors.warning
                                    DoseStatus.PENDING -> colors.hairlineStrong
                                }
                            )
                    )
                    Spacer(Modifier.width(HavnTheme.spacing.md))
                    Text(
                        text = detail.medication.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = when (detail.status) {
                            DoseStatus.TAKEN -> "Taken"
                            DoseStatus.SKIPPED -> "Skipped"
                            DoseStatus.PENDING -> "—"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    records: Map<LocalDate, DayRecord>,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ISO weeks start Monday; DayOfWeek.value is 1..7 from Monday, so the lead
    // offset is simply value-1. Using Calendar constants here (as the previous
    // implementation did) shifted the grid by one column in locales whose week
    // starts on Sunday.
    val firstDay = month.atDay(1)
    val leadingBlanks = firstDay.dayOfWeek.value - 1
    val totalCells = leadingBlanks + month.lengthOfMonth()
    val rows = (totalCells + 6) / 7

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HavnTheme.spacing.xs)) {
        repeat(rows) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(HavnTheme.spacing.xs)) {
                repeat(7) { column ->
                    val cellIndex = row * 7 + column
                    val dayNumber = cellIndex - leadingBlanks + 1
                    if (dayNumber in 1..month.lengthOfMonth()) {
                        val date = month.atDay(dayNumber)
                        DayCell(
                            date = date,
                            record = records[date],
                            isSelected = date == selected,
                            onClick = { onSelect(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    record: DayRecord?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val today = remember { LocalDate.now() }
    val isToday = date == today
    val isFuture = date.isAfter(today)

    val rate = record?.takeIf { it.hasData && !isFuture }?.rate ?: 0f
    // Completion is read as fill density rather than a colour ramp — a scale of
    // greens would force the reader to decode a legend for what is really just
    // "more or less".
    val fill by animateColorAsState(
        targetValue = when {
            isSelected -> colors.accent
            isFuture -> Color.Transparent
            record?.hasData != true -> Color.Transparent
            else -> colors.accent.copy(alpha = 0.12f + rate * 0.62f)
        },
        animationSpec = HavnMotion.standard(),
        label = "dayFill",
    )
    val textColor = when {
        isSelected -> colors.onAccent
        isFuture -> colors.textDisabled
        rate > 0.55f -> colors.onAccent
        else -> colors.textSecondary
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(HavnTheme.radius.sm))
            .background(fill)
            .then(
                if (isToday && !isSelected) {
                    Modifier.background(Color.Transparent)
                } else Modifier
            )
            .havnPress(scaleDown = 0.9f, enabled = !isFuture, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${date.dayOfMonth}",
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )
            if (isToday) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) colors.onAccent else colors.accent)
                )
            }
        }
    }
}

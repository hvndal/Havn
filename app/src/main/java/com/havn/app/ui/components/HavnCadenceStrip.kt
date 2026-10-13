package com.havn.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Represents a single day in the weekly adherence cadence.
 */
data class CadenceDay(
    val date: LocalDate,
    val scheduled: Int,
    val taken: Int,
    val skipped: Int,
    val isToday: Boolean,
    val isFuture: Boolean,
) {
    val rate: Float get() = if (scheduled <= 0) 0f else (taken.toFloat() / scheduled).coerceIn(0f, 1f)
    val isComplete: Boolean get() = scheduled > 0 && taken >= scheduled
    val hasDoses: Boolean get() = scheduled > 0 || taken > 0 || skipped > 0
}

/**
 * 7-Day Micro-Cadence Strip.
 *
 * Implements a Scandinavian-minimalist horizontal 7-day ribbon mirroring
 * the physical 7 compartments of a Scandinavian medicine box (Monday → Sunday):
 *  - Displays weekday narrow glyphs (M, T, W, T, F, S, S) and day numbers.
 *  - Micro-cadence indicators:
 *      * Complete: Solid forest green accent disk.
 *      * Partial: Hairline circular canvas track with swept accent arc.
 *      * Skipped: Warm muted amber dot.
 *      * Empty / Past: Subtle hairline ring.
 *      * Future: Faint hairline outline.
 *  - Tactile date selection with micro-tick haptics and spring animations.
 *  - Accessible labels for TalkBack screen readers.
 */
@Composable
fun HavnCadenceStrip(
    days: List<CadenceDay>,
    selectedDate: LocalDate,
    onSelectDay: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val haptics = rememberHavnHaptics()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HavnTheme.radius.md))
            .background(colors.surface)
            .border(1.dp, colors.hairline, RoundedCornerShape(HavnTheme.radius.md))
            .padding(vertical = HavnTheme.spacing.sm, horizontal = HavnTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        days.forEach { day ->
            val isSelected = day.date == selectedDate

            CadenceDayCell(
                day = day,
                isSelected = isSelected,
                onClick = {
                    haptics.tick()
                    onSelectDay(day.date)
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CadenceDayCell(
    day: CadenceDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    val cellBg by animateColorAsState(
        targetValue = when {
            isSelected -> colors.surfaceRaised
            else -> Color.Transparent
        },
        animationSpec = HavnMotion.quick(),
        label = "cadenceCellBg",
    )

    val cellBorderColor by animateColorAsState(
        targetValue = when {
            isSelected && day.isToday -> colors.accent
            isSelected -> colors.hairlineStrong
            else -> Color.Transparent
        },
        animationSpec = HavnMotion.quick(),
        label = "cadenceCellBorder",
    )

    val weekdayLetter = remember(day.date) {
        day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
    }

    val accessibilityDesc = remember(day, isSelected) {
        buildString {
            append(day.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()))
            append(", ")
            append(day.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
            append(" ")
            append(day.date.dayOfMonth)
            if (day.isToday) append(", Today")
            if (isSelected) append(", Selected")
            when {
                day.isComplete -> append(". All doses taken.")
                day.taken > 0 -> append(". ${day.taken} of ${day.scheduled} doses taken.")
                day.skipped > 0 -> append(". ${day.skipped} doses skipped.")
                day.isFuture -> append(". Upcoming.")
                else -> append(". No doses recorded.")
            }
        }
    }

    val cellShape = RoundedCornerShape(HavnTheme.radius.sm)

    Column(
        modifier = modifier
            .clip(cellShape)
            .background(cellBg)
            .then(
                if (isSelected) Modifier.border(1.dp, cellBorderColor, cellShape) else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = HavnTheme.spacing.xs, horizontal = 2.dp)
            .semantics { contentDescription = accessibilityDesc },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Weekday narrow glyph (M, T, W, T, F, S, S)
        Text(
            text = weekdayLetter.uppercase(),
            style = HavnType.EyebrowQuiet.copy(fontSize = 9.sp),
            color = if (day.isToday) colors.accent else colors.textTertiary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(2.dp))

        // Day of month number
        Text(
            text = "${day.date.dayOfMonth}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected || day.isToday) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 13.sp,
            ),
            color = when {
                day.isToday -> colors.accent
                isSelected -> colors.textPrimary
                day.isFuture -> colors.textTertiary
                else -> colors.textSecondary
            },
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(4.dp))

        // Micro-Cadence Adherence Indicator
        CadenceIndicator(day = day)

        // Today pill indicator if today is not selected
        if (day.isToday && !isSelected) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(2.5.dp)
                    .clip(CircleShape)
                    .background(colors.accent),
            )
        } else {
            Spacer(Modifier.height(2.dp))
            Box(modifier = Modifier.size(2.5.dp)) // Spacer preservation for equal height
        }
    }
}

/**
 * Micro indicator displaying adherence state with pure Canvas drawing.
 */
@Composable
private fun CadenceIndicator(day: CadenceDay) {
    val colors = HavnTheme.colors

    Box(
        modifier = Modifier.size(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            day.isComplete -> {
                // 100% Complete: solid Scandinavian forest sage disk
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.accent),
                )
            }
            day.rate > 0f -> {
                // Partial progress: micro swept arc
                Canvas(modifier = Modifier.size(8.dp)) {
                    val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
                    // Background track
                    drawCircle(
                        color = colors.hairlineStrong,
                        radius = size.minDimension / 2f - stroke.width / 2f,
                        style = stroke,
                    )
                    // Swept arc
                    drawArc(
                        color = colors.accent,
                        startAngle = -90f,
                        sweepAngle = 360f * day.rate,
                        useCenter = false,
                        style = stroke,
                    )
                }
            }
            day.skipped > 0 && day.taken == 0 -> {
                // Skipped doses only: muted terracotta/amber pip
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.warning.copy(alpha = 0.75f)),
                )
            }
            day.isToday -> {
                // Today with 0 doses yet: subtle open ring
                Canvas(modifier = Modifier.size(7.dp)) {
                    drawCircle(
                        color = colors.accent.copy(alpha = 0.5f),
                        radius = size.minDimension / 2f - 0.7.dp.toPx(),
                        style = Stroke(width = 1.2.dp.toPx()),
                    )
                }
            }
            day.isFuture -> {
                // Future days: faint dot
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colors.hairline),
                )
            }
            else -> {
                // Past days with no doses: quiet hairline ring
                Canvas(modifier = Modifier.size(5.dp)) {
                    drawCircle(
                        color = colors.hairlineStrong,
                        radius = size.minDimension / 2f - 0.5.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }
            }
        }
    }
}

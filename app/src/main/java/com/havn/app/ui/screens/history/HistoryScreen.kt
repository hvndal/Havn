package com.havn.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.domain.model.DoseLog
import com.havn.app.domain.model.DoseStatus
import com.havn.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = uiState.currentMonth.format(
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
                ),
                style = MaterialTheme.typography.headlineMedium,
                color = Charcoal,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ChevronButton("<") { viewModel.previousMonth() }
                ChevronButton(">") { viewModel.nextMonth() }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Calendar
        HavnCalendar(
            yearMonth = uiState.currentMonth,
            doseLogs = uiState.monthLogs,
            selectedDate = uiState.selectedDate,
            onDateSelected = { viewModel.selectDate(it) },
        )

        Spacer(Modifier.height(24.dp))

        // Selected day dose log
        if (uiState.selectedDate != null) {
            val dayLogs = uiState.monthLogs.filter { log ->
                val date = java.time.Instant.ofEpochMilli(log.scheduledTime)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                date == uiState.selectedDate
            }
            Text(
                text = uiState.selectedDate!!.format(
                    DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.ENGLISH)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = StoneGrey,
                letterSpacing = 0.06.sp,
            )
            Spacer(Modifier.height(10.dp))
            if (dayLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No records for this day.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StoneGrey,
                    )
                }
            } else {
                dayLogs.forEach { log ->
                    DoseLogRow(log = log)
                    Spacer(Modifier.height(8.dp))
                }
            }
        } else {
            // Empty state
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = "\uD83C\uDF3F", fontSize = 40.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Tap a day to see your history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StoneGrey,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun HavnCalendar(
    yearMonth: YearMonth,
    doseLogs: List<DoseLog>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDow = yearMonth.atDay(1).dayOfWeek.value % 7 // 0=Sun

    val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")

    // Group logs by date
    val logsByDate = doseLogs.groupBy { log ->
        java.time.Instant.ofEpochMilli(log.scheduledTime)
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    }

    Column {
        // Day headers
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGrey,
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        // Days grid
        val totalCells = firstDow + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - firstDow + 1
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (dayNum in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayNum)
                            val logs = logsByDate[date] ?: emptyList()
                            val isToday = date == LocalDate.now()
                            val isSelected = date == selectedDate
                            val takenCount = logs.count { it.status == DoseStatus.TAKEN }
                            val totalCount = logs.size
                            CalendarDay(
                                day = dayNum,
                                isToday = isToday,
                                isSelected = isSelected,
                                takenCount = takenCount,
                                totalCount = totalCount,
                                onClick = { onDateSelected(date) },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    takenCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
) {
    val bg = when {
        isSelected -> Sage
        isToday    -> SagePale
        else       -> Color.Transparent
    }
    val text = when {
        isSelected -> White
        isToday    -> SageDeep
        else       -> Charcoal
    }
    // Adherence dot color
    val dotColor = when {
        totalCount == 0 -> Color.Transparent
        takenCount == totalCount -> Sage
        takenCount == 0 -> Terracotta.copy(alpha = 0.5f)
        else -> ButterAmber
    }

    Column(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = text,
        )
        if (dotColor != Color.Transparent) {
            Spacer(Modifier.height(1.dp))
            Box(
                modifier = Modifier.size(4.dp).clip(CircleShape).background(dotColor)
            )
        }
    }
}

@Composable
private fun DoseLogRow(log: DoseLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .border(1.dp, SurfaceHighest, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    when (log.status) {
                        DoseStatus.TAKEN -> Sage
                        DoseStatus.SKIPPED -> Terracotta
                        DoseStatus.PENDING -> StoneLight
                    }
                )
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = when (log.status) {
                DoseStatus.TAKEN -> "Taken"
                DoseStatus.SKIPPED -> "Skipped"
                DoseStatus.PENDING -> "Pending"
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Charcoal,
        )
        Spacer(Modifier.weight(1f))
        log.takenAt?.let { ts ->
            val t = java.time.Instant.ofEpochMilli(ts).atZone(java.time.ZoneId.systemDefault()).toLocalTime()
            Text(
                text = t.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)).lowercase(),
                style = MaterialTheme.typography.labelMedium,
                color = StoneGrey,
            )
        }
    }
}

@Composable
private fun ChevronButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SurfaceHigh)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, color = CharcoalMid, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

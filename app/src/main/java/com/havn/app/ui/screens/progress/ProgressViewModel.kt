package com.havn.app.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.audio.SoundManager
import com.havn.app.data.repository.HavnRepository
import com.havn.app.data.session.SessionManager
import com.havn.app.data.session.SessionState
import com.havn.app.domain.model.DoseLog
import com.havn.app.domain.model.DoseStatus
import com.havn.app.domain.model.Medication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

enum class ProgressView { TRENDS, CALENDAR }

data class DayRecord(
    val date: LocalDate,
    val scheduled: Int,
    val taken: Int,
    val skipped: Int,
) {
    val isFuture: Boolean get() = date.isAfter(LocalDate.now())
    val hasData: Boolean get() = scheduled > 0
    val rate: Float get() = if (scheduled == 0) 0f else taken.toFloat() / scheduled
    val isComplete: Boolean get() = scheduled > 0 && taken >= scheduled
}

data class MedRecord(
    val medication: Medication,
    val scheduled: Int,
    val taken: Int,
) {
    val rate: Float get() = if (scheduled == 0) 0f else taken.toFloat() / scheduled
}

data class ProgressStats(
    val rate: Float = 0f,
    val taken: Int = 0,
    val scheduled: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val perfectDays: Int = 0,
)

data class ProgressUiState(
    val view: ProgressView = ProgressView.TRENDS,
    val stats: ProgressStats = ProgressStats(),
    val last30: List<DayRecord> = emptyList(),
    val byMedication: List<MedRecord> = emptyList(),
    val month: YearMonth = YearMonth.now(),
    val monthRecords: Map<LocalDate, DayRecord> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val selectedDayDoses: List<DayDoseDetail> = emptyList(),
    val hasMedications: Boolean = false,
    val isLoading: Boolean = true,
)

data class DayDoseDetail(
    val medication: Medication,
    val slot: String,
    val status: DoseStatus,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val sessionManager: SessionManager,
    val soundManager: SoundManager,
) : ViewModel() {

    private val view = MutableStateFlow(ProgressView.TRENDS)
    private val month = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now())

    val uiState: StateFlow<ProgressUiState> = sessionManager.state
        .flatMapLatest { session ->
            if (session !is SessionState.SignedIn) {
                flowOf(ProgressUiState(isLoading = false))
            } else {
                combine(
                    repository.getMedicationsForUser(session.user.id),
                    repository.getDoseLogsForPastDays(session.user.id, 30),
                    view,
                    month,
                    selectedDate,
                ) { meds, logs, currentView, currentMonth, date ->
                    build(meds, logs, currentView, currentMonth, date)
                }.flatMapLatest { base ->
                    // The calendar reaches outside the 30-day trend window, so
                    // the visible month is loaded separately rather than
                    // pretending the trend data covers it.
                    combine(
                        repository.getDoseLogsForMonth(
                            session.user.id,
                            base.month.year,
                            base.month.monthValue,
                        ),
                        repository.getMedicationsForUser(session.user.id),
                    ) { monthLogs, meds ->
                        base.copy(
                            monthRecords = monthRecords(meds, monthLogs, base.month),
                            selectedDayDoses = dayDetail(meds, monthLogs, base.selectedDate),
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    private fun build(
        meds: List<Medication>,
        logs: List<DoseLog>,
        currentView: ProgressView,
        currentMonth: YearMonth,
        date: LocalDate?,
    ): ProgressUiState {
        val today = LocalDate.now()
        val active = meds.filter { it.isActive }
        val dailyDoses = active.sumOf { it.reminderTimes.size.coerceAtLeast(1) }
        val byDate = logs.groupBy { it.localDate() }

        val days = (29 downTo 0).map { back ->
            val d = today.minusDays(back.toLong())
            val dayLogs = byDate[d].orEmpty()
            DayRecord(
                date = d,
                // Scheduled counts are derived from today's medication set,
                // which is an approximation for past days — a medication added
                // last week was not "scheduled" the week before. It is clamped
                // to the logs actually present so a day can never read as worse
                // than what was recorded.
                scheduled = maxOf(dailyDoses, dayLogs.size),
                taken = dayLogs.count { it.status == DoseStatus.TAKEN },
                skipped = dayLogs.count { it.status == DoseStatus.SKIPPED },
            )
        }

        val counted = days.filter { it.hasData }
        val totalTaken = counted.sumOf { it.taken }
        val totalScheduled = counted.sumOf { it.scheduled }

        var current = 0
        for (day in days.reversed()) {
            if (day.isComplete) current++ else break
        }
        var best = 0
        var run = 0
        days.forEach { day ->
            if (day.isComplete) { run++; if (run > best) best = run } else run = 0
        }

        val medRecords = active.map { med ->
            val medLogs = logs.filter { it.medicationId == med.id }
            MedRecord(
                medication = med,
                scheduled = 30 * med.reminderTimes.size.coerceAtLeast(1),
                taken = medLogs.count { it.status == DoseStatus.TAKEN },
            )
        }.sortedByDescending { it.rate }

        return ProgressUiState(
            view = currentView,
            stats = ProgressStats(
                rate = if (totalScheduled == 0) 0f else totalTaken.toFloat() / totalScheduled,
                taken = totalTaken,
                scheduled = totalScheduled,
                currentStreak = current,
                bestStreak = best,
                perfectDays = days.count { it.isComplete },
            ),
            last30 = days,
            byMedication = medRecords,
            month = currentMonth,
            selectedDate = date,
            hasMedications = active.isNotEmpty(),
            isLoading = false,
        )
    }

    private fun monthRecords(
        meds: List<Medication>,
        logs: List<DoseLog>,
        month: YearMonth,
    ): Map<LocalDate, DayRecord> {
        val active = meds.filter { it.isActive }
        val daily = active.sumOf { it.reminderTimes.size.coerceAtLeast(1) }
        val byDate = logs.groupBy { it.localDate() }
        return (1..month.lengthOfMonth()).associate { day ->
            val date = month.atDay(day)
            val dayLogs = byDate[date].orEmpty()
            date to DayRecord(
                date = date,
                scheduled = if (dayLogs.isEmpty() && date.isAfter(LocalDate.now())) 0
                else maxOf(daily, dayLogs.size),
                taken = dayLogs.count { it.status == DoseStatus.TAKEN },
                skipped = dayLogs.count { it.status == DoseStatus.SKIPPED },
            )
        }
    }

    private fun dayDetail(
        meds: List<Medication>,
        logs: List<DoseLog>,
        date: LocalDate?,
    ): List<DayDoseDetail> {
        if (date == null) return emptyList()
        val dayLogs = logs.filter { it.localDate() == date }
        val medById = meds.associateBy { it.id }
        return meds.filter { it.isActive }.flatMap { med ->
            val slots = med.reminderTimes.ifEmpty { listOf("") }
            slots.map { slot ->
                val log = dayLogs.firstOrNull {
                    it.medicationId == med.id && (it.scheduledSlot == slot || it.scheduledSlot.isEmpty())
                }
                DayDoseDetail(
                    medication = medById[med.id] ?: med,
                    slot = slot,
                    status = log?.status ?: DoseStatus.PENDING,
                )
            }
        }.sortedBy { it.slot.ifEmpty { "zz" } }
    }

    private fun DoseLog.localDate(): LocalDate =
        Instant.ofEpochMilli(scheduledTime).atZone(ZoneId.systemDefault()).toLocalDate()

    fun setView(next: ProgressView) {
        if (view.value == next) return
        soundManager.playSoftTap()
        view.value = next
    }

    fun previousMonth() { month.update { it.minusMonths(1) } }
    fun nextMonth() {
        // Never page past the current month — there is nothing recorded there
        // and an empty future grid reads as a bug.
        month.update { if (it >= YearMonth.now()) it else it.plusMonths(1) }
    }

    fun selectDate(date: LocalDate) {
        soundManager.playSoftTap()
        selectedDate.update { if (it == date) null else date }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            val user = (sessionManager.state.value as? SessionState.SignedIn)?.user ?: return@launch
            repository.seedSample30DayLogs(user.id)
            soundManager.playSoftChime()
        }
    }
}

package com.havn.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.audio.SoundManager
import com.havn.app.data.repository.HavnRepository
import com.havn.app.data.session.SessionManager
import com.havn.app.data.session.SessionState
import com.havn.app.domain.model.DayPeriod
import com.havn.app.domain.model.DoseLog
import com.havn.app.domain.model.DoseStatus
import com.havn.app.domain.model.Medication
import com.havn.app.domain.model.TodayDose
import com.havn.app.domain.model.User
import com.havn.app.ui.components.CadenceDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val profiles: List<User> = emptyList(),
    val doses: List<TodayDose> = emptyList(),
    val cadenceDays: List<CadenceDay> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val hasMedications: Boolean = false,
    val isLoading: Boolean = true,
) {
    val isTodaySelected: Boolean get() = selectedDate == LocalDate.now()
    val total: Int get() = doses.size
    val taken: Int get() = doses.count { it.isTaken }
    val skipped: Int get() = doses.count { it.isSkipped }
    val remaining: Int get() = doses.count { it.isPending }
    val progress: Float get() = if (total == 0) 0f else (taken.toFloat() / total)
    val allDone: Boolean get() = total > 0 && remaining == 0

    /** The next dose still waiting, used for the home screen's focus line. */
    val nextDose: TodayDose? get() = doses.firstOrNull { it.isPending }

    fun dosesIn(period: DayPeriod): List<TodayDose> = doses.filter { it.period == period }
}

/** One-shot signals the screen reacts to (a celebration, a snackbar). */
sealed interface HomeEvent {
    data object DayCompleted : HomeEvent
    data class DoseTaken(val name: String) : HomeEvent
    data class Undoable(val message: String) : HomeEvent
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val sessionManager: SessionManager,
    val soundManager: SoundManager,
) : ViewModel() {

    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<HomeEvent> = _events

    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HomeUiState> = sessionManager.state
        .flatMapLatest { session ->
            when (session) {
                is SessionState.SignedIn -> combine(
                    repository.getMedicationsForUser(session.user.id),
                    repository.getDoseLogsForPastDays(session.user.id, 14),
                    selectedDate,
                ) { meds, pastLogs, selected ->
                    val today = LocalDate.now()
                    val todayLogs = pastLogs.filter { it.localDate() == today }
                    val todayDoses = buildDosesForDay(meds, todayLogs)
                    val cadence = buildCadenceDays(meds, pastLogs, todayDoses)

                    val activeDoses = if (selected == today) {
                        todayDoses
                    } else {
                        val selectedLogs = pastLogs.filter { it.localDate() == selected }
                        buildDosesForDay(meds, selectedLogs)
                    }

                    HomeUiState(
                        user = session.user,
                        profiles = session.allProfiles,
                        doses = activeDoses,
                        cadenceDays = cadence,
                        selectedDate = selected,
                        hasMedications = meds.any { it.isActive },
                        isLoading = false,
                    )
                }
                SessionState.Resolving -> flowOf(HomeUiState(isLoading = true))
                SessionState.SignedOut -> flowOf(HomeUiState(isLoading = false))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun DoseLog.localDate(): LocalDate =
        Instant.ofEpochMilli(scheduledTime).atZone(ZoneId.systemDefault()).toLocalDate()

    /**
     * Constructs the 7-day micro-cadence strip for the current week (Mon -> Sun).
     */
    private fun buildCadenceDays(
        meds: List<Medication>,
        logs: List<DoseLog>,
        todayDoses: List<TodayDose>,
    ): List<CadenceDay> {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val activeMeds = meds.filter { it.isActive }
        val dailyDoseCount = activeMeds.sumOf { it.reminderTimes.size.coerceAtLeast(1) }
        val byDate = logs.groupBy { it.localDate() }

        return (0L..6L).map { offset ->
            val date = startOfWeek.plusDays(offset)
            val isToday = date == today
            val isFuture = date.isAfter(today)

            if (isToday) {
                CadenceDay(
                    date = date,
                    scheduled = todayDoses.size,
                    taken = todayDoses.count { it.isTaken },
                    skipped = todayDoses.count { it.isSkipped },
                    isToday = true,
                    isFuture = false,
                )
            } else if (isFuture) {
                CadenceDay(
                    date = date,
                    scheduled = dailyDoseCount,
                    taken = 0,
                    skipped = 0,
                    isToday = false,
                    isFuture = true,
                )
            } else {
                val dayLogs = byDate[date].orEmpty()
                CadenceDay(
                    date = date,
                    scheduled = maxOf(dailyDoseCount, dayLogs.size),
                    taken = dayLogs.count { it.status == DoseStatus.TAKEN },
                    skipped = dayLogs.count { it.status == DoseStatus.SKIPPED },
                    isToday = false,
                    isFuture = false,
                )
            }
        }
    }

    /**
     * Expands medications into one entry per scheduled time.
     */
    private fun buildDosesForDay(
        meds: List<Medication>,
        logs: List<DoseLog>,
    ): List<TodayDose> {
        val bySlot = logs.associateBy { it.medicationId to it.scheduledSlot }
        return meds
            .filter { it.isActive }
            .flatMap { med ->
                val slots = med.reminderTimes.ifEmpty { listOf("") }
                slots.map { slot ->
                    TodayDose(
                        medication = med,
                        doseLog = bySlot[med.id to slot]
                            ?: bySlot[med.id to ""]?.takeIf { slot == med.reminderTimes.firstOrNull() },
                        slot = slot,
                    )
                }
            }
            .sortedWith(compareBy({ it.sortKey }, { it.medication.name }))
    }

    fun selectDate(date: LocalDate) {
        soundManager.playSoftTap()
        selectedDate.value = date
    }

    fun toggleDose(dose: TodayDose) {
        viewModelScope.launch {
            val date = selectedDate.value
            val wasLastRemaining = uiState.value.remaining == 1 && dose.isPending
            if (dose.isTaken) {
                repository.markDoseUntaken(dose.medication, date, dose.slot)
                soundManager.playSoftTap()
            } else {
                repository.markDoseTaken(dose.medication, date, dose.slot)
                soundManager.playSoftChime()
                _events.tryEmit(HomeEvent.DoseTaken(dose.medication.name))
                if (wasLastRemaining) _events.tryEmit(HomeEvent.DayCompleted)
            }
        }
    }

    fun setStatus(dose: TodayDose, status: DoseStatus) {
        viewModelScope.launch {
            val date = selectedDate.value
            repository.setDoseStatus(dose.medication, date, dose.slot, status)
            if (status == DoseStatus.TAKEN) soundManager.playSoftChime()
            else soundManager.playSoftTap()
        }
    }

    fun markAllRemaining() {
        viewModelScope.launch {
            val date = selectedDate.value
            val pending = uiState.value.doses.filter { it.isPending }
            if (pending.isEmpty()) return@launch
            pending.forEach { repository.markDoseTaken(it.medication, date, it.slot) }
            soundManager.playSoftChime()
            _events.tryEmit(HomeEvent.DayCompleted)
        }
    }

    fun switchProfile(userId: Long) {
        viewModelScope.launch {
            sessionManager.signIn(userId)
            soundManager.playSoftTap()
        }
    }
}

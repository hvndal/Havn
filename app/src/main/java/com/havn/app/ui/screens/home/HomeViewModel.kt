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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val profiles: List<User> = emptyList(),
    val doses: List<TodayDose> = emptyList(),
    val hasMedications: Boolean = false,
    val isLoading: Boolean = true,
) {
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

    val uiState: StateFlow<HomeUiState> = sessionManager.state
        .flatMapLatest { session ->
            when (session) {
                is SessionState.SignedIn -> combine(
                    repository.getMedicationsForUser(session.user.id),
                    repository.getDoseLogsForDay(session.user.id, LocalDate.now()),
                ) { meds, logs ->
                    HomeUiState(
                        user = session.user,
                        profiles = session.allProfiles,
                        doses = buildTodayDoses(meds, logs),
                        hasMedications = meds.any { it.isActive },
                        isLoading = false,
                    )
                }
                SessionState.Resolving -> flowOf(HomeUiState(isLoading = true))
                SessionState.SignedOut -> flowOf(HomeUiState(isLoading = false))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /**
     * Expands medications into one entry per scheduled time.
     *
     * The previous implementation emitted a single entry per medication using
     * only `reminderTimes.first()`, so a medication set to 08:00 and 20:00
     * appeared once and its evening dose could never be recorded.
     */
    private fun buildTodayDoses(
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
                        // Pre-v2 rows carry an empty slot. Falling back to one
                        // keeps history recorded before the schema change
                        // visible against the medication's first time.
                            ?: bySlot[med.id to ""]?.takeIf { slot == med.reminderTimes.firstOrNull() },
                        slot = slot,
                    )
                }
            }
            .sortedWith(compareBy({ it.sortKey }, { it.medication.name }))
    }

    fun toggleDose(dose: TodayDose) {
        viewModelScope.launch {
            val wasLastRemaining = uiState.value.remaining == 1 && dose.isPending
            if (dose.isTaken) {
                repository.markDoseUntaken(dose.medication, LocalDate.now(), dose.slot)
                soundManager.playSoftTap()
            } else {
                repository.markDoseTaken(dose.medication, LocalDate.now(), dose.slot)
                soundManager.playSoftChime()
                _events.tryEmit(HomeEvent.DoseTaken(dose.medication.name))
                if (wasLastRemaining) _events.tryEmit(HomeEvent.DayCompleted)
            }
        }
    }

    fun setStatus(dose: TodayDose, status: DoseStatus) {
        viewModelScope.launch {
            repository.setDoseStatus(dose.medication, LocalDate.now(), dose.slot, status)
            if (status == DoseStatus.TAKEN) soundManager.playSoftChime()
            else soundManager.playSoftTap()
        }
    }

    fun markAllRemaining() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val pending = uiState.value.doses.filter { it.isPending }
            if (pending.isEmpty()) return@launch
            pending.forEach { repository.markDoseTaken(it.medication, today, it.slot) }
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

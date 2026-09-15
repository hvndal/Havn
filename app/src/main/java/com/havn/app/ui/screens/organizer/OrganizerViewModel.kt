package com.havn.app.ui.screens.organizer

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
import com.havn.app.ui.components.OrganizerDataMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class OrganizerUiState(
    val doses: List<TodayDose> = emptyList(),
    val selectedPeriod: DayPeriod = DayPeriod.current(),
    val payloadJson: String = "{}",
    val hasMedications: Boolean = false,
    val isLoading: Boolean = true,
) {
    val selectedDoses: List<TodayDose>
        get() = doses.filter { it.period == selectedPeriod }.sortedBy { it.sortKey }

    fun count(period: DayPeriod): Int = doses.count { it.period == period }
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class OrganizerViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val sessionManager: SessionManager,
    val soundManager: SoundManager,
) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(DayPeriod.current())

    val uiState: StateFlow<OrganizerUiState> = sessionManager.state
        .flatMapLatest { session ->
            if (session !is SessionState.SignedIn) {
                flowOf(OrganizerUiState(isLoading = false))
            } else {
                combine(
                    repository.getMedicationsForUser(session.user.id),
                    repository.getDoseLogsForDay(session.user.id, LocalDate.now()),
                    selectedPeriod,
                ) { meds, logs, period ->
                    val doses = buildDoses(meds, logs)
                    OrganizerUiState(
                        doses = doses,
                        selectedPeriod = period,
                        payloadJson = OrganizerDataMapper.buildPeriodJson(doses),
                        hasMedications = meds.any { it.isActive },
                        isLoading = false,
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrganizerUiState())

    private fun buildDoses(meds: List<Medication>, logs: List<DoseLog>): List<TodayDose> {
        val bySlot = logs.associateBy { it.medicationId to it.scheduledSlot }
        return meds.filter { it.isActive }.flatMap { med ->
            med.reminderTimes.ifEmpty { listOf("") }.map { slot ->
                TodayDose(
                    medication = med,
                    doseLog = bySlot[med.id to slot] ?: bySlot[med.id to ""]
                        ?.takeIf { slot == med.reminderTimes.firstOrNull() },
                    slot = slot,
                )
            }
        }.sortedBy { it.sortKey }
    }

    /**
     * Selects a compartment.
     *
     * Bounded by the four day periods. The screen previously showed seven
     * weekday chips while the 3D view was fed a four-slot payload, so tapping
     * Friday through Sunday sent an index the organizer had no slot for and it
     * silently did nothing.
     */
    fun selectPeriod(period: DayPeriod) {
        if (selectedPeriod.value == period) return
        soundManager.playCeramicClick()
        selectedPeriod.value = period
    }

    fun selectSlotIndex(index: Int) {
        DayPeriod.entries.getOrNull(index)?.let(::selectPeriod)
    }

    fun toggleDose(dose: TodayDose) {
        viewModelScope.launch {
            if (dose.isTaken) {
                repository.markDoseUntaken(dose.medication, LocalDate.now(), dose.slot)
                soundManager.playSoftTap()
            } else {
                repository.markDoseTaken(dose.medication, LocalDate.now(), dose.slot)
                soundManager.playSoftChime()
            }
        }
    }
}

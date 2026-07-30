package com.havn.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.audio.SoundManager
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import com.havn.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val todayMeds: List<TodayMedication> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val prefs: UserPreferences,
    val soundManager: SoundManager,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = prefs.activeUserId
        .flatMapLatest { userId ->
            if (userId < 0) flowOf(HomeUiState(isLoading = false))
            else combine(
                repository.getMedicationsForUser(userId),
                repository.getDoseLogsForDay(userId, LocalDate.now()),
            ) { meds, logs ->
                val logMap = logs.associateBy { it.medicationId }
                val todayMeds = meds.mapNotNull { med ->
                    if (med.reminderTimes.isEmpty() && med.repeatType == RepeatType.DAILY) {
                        TodayMedication(med, logMap[med.id], "Any time")
                    } else {
                        med.reminderTimes.firstOrNull()?.let { t ->
                            TodayMedication(med, logMap[med.id], t)
                        } ?: TodayMedication(med, logMap[med.id], "Any time")
                    }
                }.sortedBy { it.scheduledTime }
                HomeUiState(
                    user = repository.getUserById(userId),
                    todayMeds = todayMeds,
                    isLoading = false,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleMedication(med: Medication) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val log = uiState.value.todayMeds.find { it.medication.id == med.id }?.doseLog
            if (log?.status == DoseStatus.TAKEN) {
                repository.markDoseUntaken(med, today)
                soundManager.playSoftTap()
            } else {
                repository.markDoseTaken(med, today)
                soundManager.playSoftChime()
            }
        }
    }
}

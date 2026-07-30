package com.havn.app.ui.screens.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.audio.SoundManager
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import com.havn.app.domain.model.Medication
import com.havn.app.ui.components.OrganizerWeekMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class OrganizerUiState(
    val allMeds: List<Medication> = emptyList(),
    val selectedDayMeds: List<Medication> = emptyList(),
    val selectedDayIndex: Int = 0,
    val weekDataJson: String = "[]",
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class OrganizerViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val prefs: UserPreferences,
    val soundManager: SoundManager,
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(LocalDate.now().dayOfWeek.value - 1)

    val uiState: StateFlow<OrganizerUiState> = prefs.activeUserId
        .flatMapLatest { userId ->
            if (userId < 0) flowOf(OrganizerUiState())
            else combine(
                repository.getMedicationsForUser(userId),
                _selectedDay,
            ) { meds, dayIdx ->
                val filtered = OrganizerWeekMapper.medsForDay(meds, dayIdx)
                OrganizerUiState(
                    allMeds = meds,
                    selectedDayMeds = filtered,
                    selectedDayIndex = dayIdx,
                    weekDataJson = OrganizerWeekMapper.buildWeekDataJson(meds),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrganizerUiState())

    fun selectDay(dayIndex: Int) {
        _selectedDay.value = dayIndex
    }

    fun onSlotTapped(slotIndex: Int) {
        soundManager.playCeramicClick()
    }
}

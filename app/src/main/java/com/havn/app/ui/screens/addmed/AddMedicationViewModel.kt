package com.havn.app.ui.screens.addmed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.audio.SoundManager
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import com.havn.app.data.session.SessionManager
import com.havn.app.data.session.SessionState
import com.havn.app.domain.model.MedIconType
import com.havn.app.domain.model.Medication
import com.havn.app.domain.model.RepeatType
import com.havn.app.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMedUiState(
    val isEditing: Boolean = false,
    val name: String = "",
    val dosage: String = "",
    val times: List<String> = listOf("08:00"),
    val repeat: RepeatType = RepeatType.DAILY,
    val iconType: MedIconType = MedIconType.CAPSULE,
    val colorTag: String = "sage",
    val notes: String = "",
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false,
    val saved: Boolean = false,
) {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val sessionManager: SessionManager,
    private val prefs: UserPreferences,
    private val reminderScheduler: ReminderScheduler,
    private val soundManager: SoundManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editingId: Long? = savedStateHandle.get<Long>("medId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(AddMedUiState(isEditing = editingId != null))
    val uiState: StateFlow<AddMedUiState> = _uiState.asStateFlow()

    init {
        if (editingId != null) {
            viewModelScope.launch {
                val med = repository.getMedicationById(editingId)
                if (med != null) {
                    _uiState.update {
                        it.copy(
                            name = med.name,
                            dosage = med.dosage,
                            times = med.reminderTimes,
                            repeat = med.repeatType,
                            iconType = med.iconType,
                            colorTag = med.colorTag,
                            notes = med.notes,
                            isLoaded = true,
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoaded = true) }
                }
            }
        } else {
            _uiState.update { it.copy(isLoaded = true) }
        }
    }

    fun setName(value: String) = _uiState.update {
        it.copy(name = value, nameError = null)
    }

    fun setDosage(value: String) = _uiState.update { it.copy(dosage = value) }
    fun setNotes(value: String) = _uiState.update { it.copy(notes = value) }
    fun setRepeat(value: RepeatType) {
        soundManager.playSoftTap()
        _uiState.update { it.copy(repeat = value) }
    }

    fun setIcon(value: MedIconType) {
        soundManager.playSoftTap()
        _uiState.update { it.copy(iconType = value) }
    }

    fun setColor(value: String) {
        soundManager.playSoftTap()
        _uiState.update { it.copy(colorTag = value) }
    }

    /** Adds a time, keeping the list sorted and free of duplicates. */
    fun addTime(time: String) = _uiState.update { state ->
        if (time in state.times) state
        else state.copy(times = (state.times + time).sorted())
    }

    fun replaceTime(index: Int, time: String) = _uiState.update { state ->
        val updated = state.times.toMutableList()
        if (index !in updated.indices) return@update state
        updated[index] = time
        state.copy(times = updated.distinct().sorted())
    }

    fun removeTime(index: Int) = _uiState.update { state ->
        state.copy(times = state.times.filterIndexed { i, _ -> i != index })
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Give this a name so you recognise it.") }
            return
        }
        if (state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // The active profile comes from the resolved session, never from
            // the raw preference. Reading the pref directly is what allowed
            // medications to be saved against userId = -1 when the stored id
            // pointed at a deleted profile — those rows were written and then
            // never shown by any screen.
            val session = sessionManager.state.value
            val userId = (session as? SessionState.SignedIn)?.user?.id
            if (userId == null) {
                _uiState.update {
                    it.copy(isSaving = false, nameError = "No active profile. Try signing in again.")
                }
                return@launch
            }

            val times = if (state.repeat == RepeatType.AS_NEEDED) emptyList() else state.times

            val medication = Medication(
                id = editingId ?: 0L,
                userId = userId,
                name = state.name.trim(),
                dosage = state.dosage.trim(),
                reminderTimes = times,
                repeatType = state.repeat,
                colorTag = state.colorTag,
                iconType = state.iconType,
                notes = state.notes.trim(),
                isActive = true,
            )

            val id = repository.saveMedication(medication)
            val includePreDose = prefs.preDoseEnabled.first()
            reminderScheduler.scheduleReminder(medication.copy(id = id), includePreDose)

            soundManager.playSoftChime()
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }
    }
}

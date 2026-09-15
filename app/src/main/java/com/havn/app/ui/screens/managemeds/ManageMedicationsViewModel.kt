package com.havn.app.ui.screens.managemeds

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MedFilter(val label: String) {
    ALL("All"),
    SCHEDULED("Scheduled"),
    AS_NEEDED("As needed"),
    PAUSED("Paused"),
}

data class ManageMedsUiState(
    val medications: List<Medication> = emptyList(),
    val visible: List<Medication> = emptyList(),
    val query: String = "",
    val filter: MedFilter = MedFilter.ALL,
    val hasAny: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ManageMedicationsViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val sessionManager: SessionManager,
    private val prefs: UserPreferences,
    private val reminderScheduler: ReminderScheduler,
    val soundManager: SoundManager,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(MedFilter.ALL)

    val uiState: StateFlow<ManageMedsUiState> = sessionManager.state
        .flatMapLatest { session ->
            if (session !is SessionState.SignedIn) {
                flowOf(ManageMedsUiState(isLoading = false))
            } else {
                combine(
                    repository.getMedicationsForUser(session.user.id),
                    query,
                    filter,
                ) { meds, q, f ->
                    val visible = meds.filter { med ->
                        val matchesQuery = q.isBlank() ||
                            med.name.contains(q, ignoreCase = true) ||
                            med.dosage.contains(q, ignoreCase = true) ||
                            med.notes.contains(q, ignoreCase = true)

                        val matchesFilter = when (f) {
                            MedFilter.ALL -> true
                            MedFilter.SCHEDULED -> med.isActive && med.reminderTimes.isNotEmpty()
                            MedFilter.AS_NEEDED ->
                                med.isActive && (med.reminderTimes.isEmpty() ||
                                    med.repeatType == RepeatType.AS_NEEDED)
                            MedFilter.PAUSED -> !med.isActive
                        }
                        matchesQuery && matchesFilter
                    }.sortedWith(
                        compareByDescending<Medication> { it.isActive }
                            .thenBy { it.reminderTimes.firstOrNull() ?: "zz" }
                            .thenBy { it.name.lowercase() }
                    )

                    ManageMedsUiState(
                        medications = meds,
                        visible = visible,
                        query = q,
                        filter = f,
                        hasAny = meds.isNotEmpty(),
                        isLoading = false,
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManageMedsUiState())

    fun setQuery(value: String) { query.value = value }

    fun setFilter(value: MedFilter) {
        if (filter.value == value) return
        soundManager.playSoftTap()
        filter.value = value
    }

    fun toggleActive(med: Medication) {
        viewModelScope.launch {
            val updated = med.copy(isActive = !med.isActive)
            repository.updateMedication(updated)
            if (updated.isActive) {
                reminderScheduler.scheduleReminder(updated, prefs.preDoseEnabled.first())
            } else {
                reminderScheduler.cancelReminder(med)
            }
            soundManager.playSoftTap()
        }
    }

    fun delete(med: Medication) {
        viewModelScope.launch {
            // Cancel before deleting. Cancelling afterwards used the medication's
            // own reminderTimes to clear legacy alarms, which is fine, but the
            // WorkManager tag cancel has to happen regardless — a deleted
            // medication with live work would keep waking the worker, which then
            // found no row and did nothing, forever.
            reminderScheduler.cancelReminder(med)
            repository.deleteMedication(med)
            soundManager.playSoftTap()
        }
    }
}

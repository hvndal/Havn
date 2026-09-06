package com.havn.app.ui.screens.addmed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import com.havn.app.domain.model.*
import com.havn.app.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val prefs: UserPreferences,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    fun saveMedication(
        name: String,
        dosage: String,
        time: String,
        repeat: RepeatType,
        iconType: MedIconType,
        colorTag: String,
        notes: String,
        shape: MedShape = MedShape.CAPSULE,
        secondaryColorTag: String? = null,
        size: MedSize = MedSize.MEDIUM,
        scoreLine: MedScoreLine = MedScoreLine.NONE,
        imprint: String = "",
        coating: MedCoating = MedCoating.SATIN,
        onComplete: () -> Unit,
    ) {
        viewModelScope.launch {
            val userId = prefs.activeUserId.first()
            val med = Medication(
                userId = userId,
                name = name.trim(),
                dosage = dosage.trim(),
                reminderTimes = if (time.isNotBlank()) listOf(time) else emptyList(),
                repeatType = repeat,
                colorTag = colorTag,
                iconType = iconType,
                notes = notes.trim(),
                shape = shape,
                secondaryColorTag = secondaryColorTag,
                size = size,
                scoreLine = scoreLine,
                imprint = imprint,
                coating = coating,
            )
            val id = repository.saveMedication(med)
            // Schedule reminders
            if (time.isNotBlank()) {
                reminderScheduler.scheduleReminder(med.copy(id = id))
            }
            onComplete()
        }
    }
}

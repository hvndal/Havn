package com.havn.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import com.havn.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val users: List<User> = emptyList(),
    val activeUserId: Long = -1L,
    val reminderSound: String = "CHIME",
    val reminderVibration: Boolean = true,
    val theme: String = "SYSTEM",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val prefs: UserPreferences,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.getAllUsers(),
        prefs.activeUserId,
        prefs.reminderSound,
        prefs.reminderVibration,
        prefs.themePref,
    ) { users, userId, sound, vibration, theme ->
        SettingsUiState(
            users = users,
            activeUserId = userId,
            reminderSound = sound,
            reminderVibration = vibration,
            theme = theme,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun switchUser(userId: Long) {
        viewModelScope.launch { prefs.setActiveUser(userId) }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
            // If we deleted the active user, switch to first remaining
            if (user.id == prefs.activeUserId.first()) {
                val remaining = repository.getAllUsers().first()
                prefs.setActiveUser(remaining.firstOrNull()?.id ?: -1L)
            }
        }
    }

    fun setReminderSound(sound: String) {
        viewModelScope.launch { prefs.setReminderSound(sound) }
    }

    fun setVibration(v: Boolean) {
        viewModelScope.launch { prefs.setReminderVibration(v) }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { prefs.setTheme(theme) }
    }
}

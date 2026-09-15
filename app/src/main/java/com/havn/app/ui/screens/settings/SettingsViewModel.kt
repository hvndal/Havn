package com.havn.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.data.backup.BackupManager
import com.havn.app.data.backup.RestoreResult
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import com.havn.app.data.session.SessionManager
import com.havn.app.data.session.SessionState
import com.havn.app.domain.model.User
import com.havn.app.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val activeUser: User? = null,
    val profiles: List<User> = emptyList(),
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val interfaceSound: Boolean = true,
    val isLoading: Boolean = true,
)

/** One-shot outcomes from a backup or restore, surfaced as a banner. */
sealed interface BackupEvent {
    data class Share(val intent: android.content.Intent) : BackupEvent
    data class Message(val text: String, val isError: Boolean = false) : BackupEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val sessionManager: SessionManager,
    private val prefs: UserPreferences,
    private val backupManager: BackupManager,
) : ViewModel() {

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _events = MutableSharedFlow<BackupEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<BackupEvent> = _events

    val uiState: StateFlow<SettingsUiState> = combine(
        sessionManager.state,
        prefs.themePref,
        prefs.interfaceSound,
    ) { session, theme, sound ->
        SettingsUiState(
            activeUser = session.userOrNull,
            profiles = (session as? SessionState.SignedIn)?.allProfiles.orEmpty(),
            theme = ThemeMode.from(theme),
            interfaceSound = sound,
            isLoading = session is SessionState.Resolving,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { prefs.setTheme(mode.name) }
    }

    fun setInterfaceSound(enabled: Boolean) {
        viewModelScope.launch { prefs.setInterfaceSound(enabled) }
    }

    fun switchProfile(userId: Long) {
        viewModelScope.launch { sessionManager.signIn(userId) }
    }

    fun createProfile(name: String, age: Int?, color: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            sessionManager.createAndSignIn(name.trim(), age ?: 0, color)
        }
    }

    fun deleteProfile(user: User) {
        viewModelScope.launch { sessionManager.deleteProfile(user) }
    }

    /**
     * Signs out of the current profile without deleting anything.
     *
     * There was no way to do this before — Settings offered profile *switching*
     * by navigating to onboarding while leaving the signed-in screens on the
     * back stack, so pressing back returned to them. Sign-out now clears the
     * session, cancels pending reminders, and the nav graph swaps to the
     * signed-out graph on its own.
     */
    fun signOut() {
        viewModelScope.launch { sessionManager.signOut() }
    }

    fun clearHistory() {
        viewModelScope.launch {
            val user = sessionManager.state.value.userOrNull ?: return@launch
            repository.clearHistory(user.id)
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            val user = sessionManager.state.value.userOrNull ?: return@launch
            repository.seedSample30DayLogs(user.id)
        }
    }

    // ── Backup ───────────────────────────────────────────────────────────────

    /**
     * Builds an export and hands it to the system share sheet, which is how
     * the user gets it to Drive, Files, email or anywhere else. Hävn requests
     * no network permission, so handing the file to another app is the only
     * way off the device — and the right one: the destination is the user's
     * choice, not the app's.
     */
    fun exportBackup() {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            runCatching { backupManager.exportToShareIntent() }
                .onSuccess { _events.tryEmit(BackupEvent.Share(it)) }
                .onFailure {
                    _events.tryEmit(BackupEvent.Message("Couldn't build the backup.", isError = true))
                }
            _busy.value = false
        }
    }

    /** Writes the export straight to a location the user picked. */
    fun saveBackupTo(target: android.net.Uri) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            val ok = backupManager.writeTo(target)
            _events.tryEmit(
                if (ok) BackupEvent.Message("Backup saved.")
                else BackupEvent.Message("Couldn't write to that location.", isError = true)
            )
            _busy.value = false
        }
    }

    fun suggestedFileName(): String = backupManager.fileName()

    fun restoreBackup(source: android.net.Uri) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            when (val result = backupManager.restore(source)) {
                is RestoreResult.Success -> _events.tryEmit(
                    BackupEvent.Message(
                        "Restored ${result.profiles} profile(s), ${result.medications} " +
                            "medications and ${result.doses} recorded doses."
                    )
                )
                is RestoreResult.Failure ->
                    _events.tryEmit(BackupEvent.Message(result.reason, isError = true))
            }
            _busy.value = false
        }
    }
}

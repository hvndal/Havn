package com.havn.app.ui.screens.reminders

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import com.havn.app.data.session.SessionManager
import com.havn.app.data.session.SessionState
import com.havn.app.domain.model.Medication
import com.havn.app.notifications.HavnNotificationChannels
import com.havn.app.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/** One upcoming reminder occurrence. */
data class UpcomingReminder(
    val medication: Medication,
    val slot: String,
    val at: LocalDateTime,
) {
    val isToday: Boolean get() = at.toLocalDate() == LocalDate.now()
}

enum class PermissionState {
    /** Pre-Android 13, or already granted. */
    Granted,

    /** Runtime permission not yet granted. */
    Denied,

    /**
     * Granted at the OS level but the user has turned Hävn's notifications off
     * in system settings, so nothing will appear. Worth distinguishing — asking
     * again does nothing; only a trip to settings fixes it.
     */
    BlockedInSettings,
}

data class RemindersUiState(
    val upcoming: List<UpcomingReminder> = emptyList(),
    val scheduledMedications: List<Medication> = emptyList(),
    val unscheduledMedications: List<Medication> = emptyList(),
    val permission: PermissionState = PermissionState.Granted,
    val eveningCheckEnabled: Boolean = true,
    val eveningCheckTime: String = "20:00",
    val preDoseEnabled: Boolean = true,
    val vibration: Boolean = true,
    val sound: String = "CHIME",
    val isLoading: Boolean = true,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class RemindersViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: HavnRepository,
    private val sessionManager: SessionManager,
    private val prefs: UserPreferences,
    private val scheduler: ReminderScheduler,
) : ViewModel() {

    private val permissionRefresh = MutableStateFlow(0)

    val uiState: StateFlow<RemindersUiState> = sessionManager.state
        .flatMapLatest { session ->
            if (session !is SessionState.SignedIn) {
                flowOf(RemindersUiState(isLoading = false))
            } else {
                combine(
                    repository.getMedicationsForUser(session.user.id),
                    combine(
                        prefs.eveningCheckEnabled,
                        prefs.eveningCheckTime,
                        prefs.preDoseEnabled,
                    ) { evening, eveningTime, preDose -> Triple(evening, eveningTime, preDose) },
                    combine(
                        prefs.reminderVibration,
                        prefs.reminderSound,
                    ) { vibration, sound -> vibration to sound },
                    permissionRefresh,
                ) { meds, notifSettings, alertSettings, _ ->
                    val (evening, eveningTime, preDose) = notifSettings
                    val (vibration, sound) = alertSettings
                    val active = meds.filter { it.isActive }

                    RemindersUiState(
                        upcoming = buildUpcoming(active),
                        scheduledMedications = active.filter { it.reminderTimes.isNotEmpty() },
                        unscheduledMedications = meds.filter {
                            it.isActive && it.reminderTimes.isEmpty()
                        },
                        permission = currentPermissionState(),
                        eveningCheckEnabled = evening,
                        eveningCheckTime = eveningTime,
                        preDoseEnabled = preDose,
                        vibration = vibration,
                        sound = sound,
                        isLoading = false,
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RemindersUiState())

    /**
     * The next 24 hours of reminders, computed from the medications themselves.
     *
     * Deliberately derived rather than read back from WorkManager: WorkManager
     * is the mechanism, the medication schedule is the truth, and querying
     * pending work would show the user scheduling internals (including the
     * separate pre-dose entries) rather than their own plan.
     */
    private fun buildUpcoming(meds: List<Medication>): List<UpcomingReminder> {
        val now = LocalDateTime.now()
        return meds.flatMap { med ->
            med.reminderTimes.mapNotNull { slot ->
                val time = runCatching { LocalTime.parse(slot) }.getOrNull()
                    ?: return@mapNotNull null
                var at = LocalDate.now().atTime(time)
                if (!at.isAfter(now)) at = at.plusDays(1)
                UpcomingReminder(med, slot, at)
            }
        }.sortedBy { it.at }
    }

    private fun currentPermissionState(): PermissionState {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return PermissionState.Denied
        }

        if (!manager.areNotificationsEnabled()) return PermissionState.BlockedInSettings

        // Permission can be granted while the dose channel itself is muted,
        // which produces silent "delivered" reminders the user never sees.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = manager.getNotificationChannel(HavnNotificationChannels.DOSE)
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                return PermissionState.BlockedInSettings
            }
        }

        return PermissionState.Granted
    }

    /** Called when returning from the system permission dialog or settings. */
    fun refreshPermission() = permissionRefresh.update { it + 1 }

    fun onPermissionRequested() {
        viewModelScope.launch { prefs.setNotifPermissionAsked(true) }
    }

    fun setEveningCheck(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setEveningCheckEnabled(enabled)
            if (enabled) scheduler.scheduleEveningCheck(prefs.eveningCheckTime.first())
            else scheduler.cancelEveningCheck()
        }
    }

    fun setEveningCheckTime(time: String) {
        viewModelScope.launch {
            prefs.setEveningCheckTime(time)
            if (prefs.eveningCheckEnabled.first()) scheduler.scheduleEveningCheck(time)
        }
    }

    fun setPreDose(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setPreDoseEnabled(enabled)
            // Re-run the reconciler so the change takes effect on already
            // scheduled medications rather than only on the next one edited.
            scheduler.scheduleDailySyncWork()
        }
    }

    fun setVibration(enabled: Boolean) {
        viewModelScope.launch { prefs.setReminderVibration(enabled) }
    }

    fun setSound(sound: String) {
        viewModelScope.launch { prefs.setReminderSound(sound) }
    }

    fun sendTestNotification() = scheduler.triggerTestNotification()
}

package com.havn.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "havn_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val ACTIVE_USER_ID = longPreferencesKey("active_user_id")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val THEME_PREF = stringPreferencesKey("theme") // LIGHT, DARK, SYSTEM
        val REMINDER_SOUND = stringPreferencesKey("reminder_sound") // CHIME, MARIMBA, SILENT
        val REMINDER_VIBRATION = booleanPreferencesKey("reminder_vibration")
        val INTERFACE_SOUND = booleanPreferencesKey("interface_sound")
        val EVENING_CHECK_ENABLED = booleanPreferencesKey("evening_check_enabled")
        val EVENING_CHECK_TIME = stringPreferencesKey("evening_check_time") // "HH:mm"
        val PRE_DOSE_ENABLED = booleanPreferencesKey("pre_dose_enabled")
        val NOTIF_PERMISSION_ASKED = booleanPreferencesKey("notif_permission_asked")

        const val NO_USER = -1L
        const val DEFAULT_EVENING_CHECK = "20:00"
    }

    /**
     * A corrupt or unreadable preferences file must not crash the app on
     * launch — it degrades to defaults instead, which for this app means
     * "signed out" rather than "won't start".
     */
    private val data: Flow<Preferences> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    val activeUserId: Flow<Long> = data.map { it[ACTIVE_USER_ID] ?: NO_USER }
    val onboardingDone: Flow<Boolean> = data.map { it[ONBOARDING_DONE] ?: false }
    val themePref: Flow<String> = data.map { it[THEME_PREF] ?: "SYSTEM" }
    val reminderSound: Flow<String> = data.map { it[REMINDER_SOUND] ?: "CHIME" }
    val reminderVibration: Flow<Boolean> = data.map { it[REMINDER_VIBRATION] ?: true }
    val interfaceSound: Flow<Boolean> = data.map { it[INTERFACE_SOUND] ?: true }
    val eveningCheckEnabled: Flow<Boolean> = data.map { it[EVENING_CHECK_ENABLED] ?: true }
    val eveningCheckTime: Flow<String> = data.map { it[EVENING_CHECK_TIME] ?: DEFAULT_EVENING_CHECK }
    val preDoseEnabled: Flow<Boolean> = data.map { it[PRE_DOSE_ENABLED] ?: true }
    val notifPermissionAsked: Flow<Boolean> = data.map { it[NOTIF_PERMISSION_ASKED] ?: false }

    suspend fun setActiveUser(id: Long) {
        context.dataStore.edit { it[ACTIVE_USER_ID] = id }
        com.havn.app.widget.updateHavnWidget(context)
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[ONBOARDING_DONE] = done }
    }

    /**
     * Clears session state in a single atomic edit.
     *
     * Doing this as two separate writes let an observer see the intermediate
     * state (no active user, but onboarding still marked complete) and route
     * on it — which is how a sign-out could land on a signed-in screen.
     */
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[ACTIVE_USER_ID] = NO_USER
            prefs[ONBOARDING_DONE] = false
        }
        com.havn.app.widget.updateHavnWidget(context)
    }

    suspend fun setTheme(theme: String) { context.dataStore.edit { it[THEME_PREF] = theme } }
    suspend fun setReminderSound(sound: String) { context.dataStore.edit { it[REMINDER_SOUND] = sound } }
    suspend fun setReminderVibration(v: Boolean) { context.dataStore.edit { it[REMINDER_VIBRATION] = v } }
    suspend fun setInterfaceSound(v: Boolean) { context.dataStore.edit { it[INTERFACE_SOUND] = v } }
    suspend fun setEveningCheckEnabled(v: Boolean) { context.dataStore.edit { it[EVENING_CHECK_ENABLED] = v } }
    suspend fun setEveningCheckTime(time: String) { context.dataStore.edit { it[EVENING_CHECK_TIME] = time } }
    suspend fun setPreDoseEnabled(v: Boolean) { context.dataStore.edit { it[PRE_DOSE_ENABLED] = v } }
    suspend fun setNotifPermissionAsked(v: Boolean) { context.dataStore.edit { it[NOTIF_PERMISSION_ASKED] = v } }
}

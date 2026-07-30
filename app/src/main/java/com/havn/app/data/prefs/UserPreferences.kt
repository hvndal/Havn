package com.havn.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    }

    val activeUserId: Flow<Long> = context.dataStore.data.map { it[ACTIVE_USER_ID] ?: -1L }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_DONE] ?: false }
    val themePref: Flow<String> = context.dataStore.data.map { it[THEME_PREF] ?: "SYSTEM" }
    val reminderSound: Flow<String> = context.dataStore.data.map { it[REMINDER_SOUND] ?: "CHIME" }
    val reminderVibration: Flow<Boolean> = context.dataStore.data.map { it[REMINDER_VIBRATION] ?: true }

    suspend fun setActiveUser(id: Long) { context.dataStore.edit { it[ACTIVE_USER_ID] = id } }
    suspend fun setOnboardingDone(done: Boolean) { context.dataStore.edit { it[ONBOARDING_DONE] = done } }
    suspend fun setTheme(theme: String) { context.dataStore.edit { it[THEME_PREF] = theme } }
    suspend fun setReminderSound(sound: String) { context.dataStore.edit { it[REMINDER_SOUND] = sound } }
    suspend fun setReminderVibration(v: Boolean) { context.dataStore.edit { it[REMINDER_VIBRATION] = v } }
}

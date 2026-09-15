package com.havn.app.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Brings every scheduled reminder back in line with what is actually stored.
 *
 * Runs daily, at boot, and once immediately on launch. Because WorkManager
 * one-shots do not survive indefinitely and the OS may drop them under Doze,
 * treating this as the reconciler — rather than trusting each individual
 * enqueue to persist forever — is what makes reminders reliable over weeks.
 */
@HiltWorker
class RescheduleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: HavnRepository,
    private val prefs: UserPreferences,
    private val scheduler: ReminderScheduler,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = prefs.activeUserId.first()
        if (userId < 0) {
            // Signed out: make sure nothing is left pending.
            scheduler.cancelAllScheduledWork()
            return Result.success()
        }

        val includePreDose = prefs.preDoseEnabled.first()
        val meds = repository.getMedicationsForUser(userId).first()

        meds.forEach { med ->
            if (med.isActive && med.reminderTimes.isNotEmpty()) {
                scheduler.scheduleReminder(med, includePreDose)
            } else {
                scheduler.cancelReminder(med)
            }
        }

        if (prefs.eveningCheckEnabled.first()) {
            scheduler.scheduleEveningCheck(prefs.eveningCheckTime.first())
        } else {
            scheduler.cancelEveningCheck()
        }

        return Result.success()
    }
}

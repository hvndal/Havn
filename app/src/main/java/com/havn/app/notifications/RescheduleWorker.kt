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
        if (userId < 0) return Result.success()
        val meds = repository.getMedicationsForUser(userId).first()
        meds.filter { it.reminderTimes.isNotEmpty() }.forEach { med ->
            scheduler.scheduleReminder(med)
        }
        return Result.success()
    }
}

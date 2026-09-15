package com.havn.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.havn.app.domain.model.Medication
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val workManager: WorkManager = WorkManager.getInstance(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @JvmOverloads
    fun scheduleReminder(medication: Medication, includePreDose: Boolean = true) {
        // Cancel first, unconditionally. Editing a medication's times used to
        // leave the *old* times scheduled — enqueueUniqueWork only replaces the
        // names it is given, so a dose moved from 08:00 to 09:00 kept firing at
        // both. Clearing this medication's tag first makes the new set
        // authoritative.
        cancelReminderById(medication.id)

        if (!medication.isActive) return

        medication.reminderTimes.forEach { timeStr ->
            scheduleDoseWork(medication, timeStr, includePreDose)
        }
    }

    private fun scheduleDoseWork(
        medication: Medication,
        timeStr: String,
        includePreDose: Boolean = true,
    ) {
        val time = runCatching {
            LocalTime.parse(timeStr, timeFormatter)
        }.getOrNull() ?: return

        val now = LocalDateTime.now()

        // 1. Exact Scheduled Dose Reminder
        var doseDateTime = LocalDate.now().atTime(time)
        if (!doseDateTime.isAfter(now)) {
            doseDateTime = doseDateTime.plusDays(1)
        }
        val delayMs = Duration.between(now, doseDateTime).toMillis().coerceAtLeast(0)

        val timeKey = timeStr.replace(":", "_")
        val uniqueDoseName = "pill_dose_${medication.id}_$timeKey"

        val doseData = workDataOf(
            PillReminderWorker.KEY_MED_ID to medication.id,
            PillReminderWorker.KEY_MED_NAME to medication.name,
            PillReminderWorker.KEY_MED_DOSAGE to medication.dosage,
            PillReminderWorker.KEY_TIME to timeStr,
            PillReminderWorker.KEY_USER_ID to medication.userId,
            PillReminderWorker.KEY_IS_PRE_DOSE to false,
            PillReminderWorker.KEY_IS_EVENING_CHECK to false,
            PillReminderWorker.KEY_IS_TEST to false,
        )

        val doseRequest = OneTimeWorkRequestBuilder<PillReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(doseData)
            .addTag("tag_med_${medication.id}")
            .addTag("tag_pill_reminder")
            .addTag("tag_dose")
            .build()

        workManager.enqueueUniqueWork(
            uniqueDoseName,
            ExistingWorkPolicy.REPLACE,
            doseRequest
        )

        // 2. Pre-dose Reminder (15 mins before scheduled dose)
        val preDoseDateTime = doseDateTime.minusMinutes(15)
        val preDelayMs = Duration.between(now, preDoseDateTime).toMillis()
        if (includePreDose && preDelayMs > 0) {
            val uniquePreDoseName = "pill_predose_${medication.id}_$timeKey"
            val preData = workDataOf(
                PillReminderWorker.KEY_MED_ID to medication.id,
                PillReminderWorker.KEY_MED_NAME to medication.name,
                PillReminderWorker.KEY_MED_DOSAGE to medication.dosage,
                PillReminderWorker.KEY_TIME to timeStr,
                PillReminderWorker.KEY_USER_ID to medication.userId,
                PillReminderWorker.KEY_IS_PRE_DOSE to true,
                PillReminderWorker.KEY_IS_EVENING_CHECK to false,
                PillReminderWorker.KEY_IS_TEST to false,
            )

            val preRequest = OneTimeWorkRequestBuilder<PillReminderWorker>()
                .setInitialDelay(preDelayMs, TimeUnit.MILLISECONDS)
                .setInputData(preData)
                .addTag("tag_med_${medication.id}")
                .addTag("tag_pill_reminder")
                .addTag("tag_predose")
                .build()

            workManager.enqueueUniqueWork(
                uniquePreDoseName,
                ExistingWorkPolicy.REPLACE,
                preRequest
            )
        }
    }

    fun scheduleNextDailyDose(medId: Long, timeStr: String, isPreDose: Boolean) {
        val time = runCatching {
            LocalTime.parse(timeStr, timeFormatter)
        }.getOrNull() ?: return

        val now = LocalDateTime.now()
        val targetDateTime = LocalDate.now().plusDays(1).atTime(time)
        val finalDateTime = if (isPreDose) targetDateTime.minusMinutes(15) else targetDateTime
        val delayMs = Duration.between(now, finalDateTime).toMillis().coerceAtLeast(0)

        val timeKey = timeStr.replace(":", "_")
        val uniqueName = if (isPreDose) {
            "pill_predose_${medId}_$timeKey"
        } else {
            "pill_dose_${medId}_$timeKey"
        }

        val data = workDataOf(
            PillReminderWorker.KEY_MED_ID to medId,
            PillReminderWorker.KEY_TIME to timeStr,
            PillReminderWorker.KEY_IS_PRE_DOSE to isPreDose,
            PillReminderWorker.KEY_IS_EVENING_CHECK to false,
            PillReminderWorker.KEY_IS_TEST to false,
        )

        val request = OneTimeWorkRequestBuilder<PillReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("tag_med_$medId")
            .addTag("tag_pill_reminder")
            .addTag(if (isPreDose) "tag_predose" else "tag_dose")
            .build()

        workManager.enqueueUniqueWork(
            uniqueName,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelReminder(medication: Medication) {
        workManager.cancelAllWorkByTag("tag_med_${medication.id}")

        // Also clean up any legacy alarm intents
        medication.reminderTimes.forEachIndexed { i, _ ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = (medication.id * 100 + i).toInt()
            val pending = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            pending?.let { alarmManager.cancel(it) }

            val preRequestCode = (medication.id * 100 + 50 + i).toInt()
            val prePending = PendingIntent.getBroadcast(
                context, preRequestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            prePending?.let { alarmManager.cancel(it) }
        }
    }

    fun cancelReminderById(medicationId: Long) {
        workManager.cancelAllWorkByTag("tag_med_$medicationId")
    }

    /**
     * Cancels everything this app has scheduled. Used on sign-out and when the
     * last profile is deleted — otherwise reminders keep firing for a profile
     * nobody is signed into.
     */
    fun cancelAllScheduledWork() {
        workManager.cancelAllWorkByTag("tag_pill_reminder")
        workManager.cancelAllWorkByTag("tag_evening_check")
        workManager.cancelUniqueWork("pill_evening_check")
        workManager.cancelUniqueWork("havn_daily_sync")
        workManager.cancelUniqueWork("havn_immediate_sync")
    }

    fun cancelEveningCheck() {
        workManager.cancelUniqueWork("pill_evening_check")
        workManager.cancelAllWorkByTag("tag_evening_check")
    }

    /**
     * @param at "HH:mm" — user-configurable rather than hard-coded to 20:00,
     *   since "before bed" is not the same hour for everyone.
     */
    @JvmOverloads
    fun scheduleEveningCheck(at: String = "20:00") {
        val time = runCatching { LocalTime.parse(at, timeFormatter) }
            .getOrElse { LocalTime.of(20, 0) }

        val now = LocalDateTime.now()
        var eveningTime = LocalDate.now().atTime(time)
        if (!eveningTime.isAfter(now)) {
            eveningTime = eveningTime.plusDays(1)
        }

        val delayMs = Duration.between(now, eveningTime).toMillis().coerceAtLeast(0)

        val data = workDataOf(
            PillReminderWorker.KEY_IS_EVENING_CHECK to true,
            PillReminderWorker.KEY_IS_TEST to false,
            PillReminderWorker.KEY_IS_PRE_DOSE to false,
        )

        val request = OneTimeWorkRequestBuilder<PillReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("tag_evening_check")
            .build()

        workManager.enqueueUniqueWork(
            "pill_evening_check",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun scheduleDailySyncWork() {
        // Daily periodic maintenance worker (every 24 hours)
        val periodicSyncRequest = PeriodicWorkRequestBuilder<RescheduleWorker>(24, TimeUnit.HOURS)
            .addTag("tag_daily_sync")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "havn_daily_sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicSyncRequest
        )

        // Immediate one-time sync to guarantee existing meds are scheduled immediately
        val immediateSync = OneTimeWorkRequestBuilder<RescheduleWorker>()
            .addTag("tag_immediate_sync")
            .build()

        workManager.enqueueUniqueWork(
            "havn_immediate_sync",
            ExistingWorkPolicy.REPLACE,
            immediateSync
        )
    }

    fun triggerTestNotification() {
        val testData = workDataOf(
            PillReminderWorker.KEY_IS_TEST to true,
            PillReminderWorker.KEY_IS_EVENING_CHECK to false,
            PillReminderWorker.KEY_IS_PRE_DOSE to false,
        )

        val testRequest = OneTimeWorkRequestBuilder<PillReminderWorker>()
            .setInputData(testData)
            .addTag("tag_test_notification")
            .build()

        workManager.enqueueUniqueWork(
            "pill_test_notification_${System.currentTimeMillis()}",
            ExistingWorkPolicy.REPLACE,
            testRequest
        )
    }
}



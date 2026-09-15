package com.havn.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.havn.app.data.db.DoseLogEntity
import com.havn.app.data.db.HavnDatabase
import com.havn.app.widget.updateHavnWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Handles the actions attached to a reminder notification.
 *
 * Notification *presentation* now lives entirely in [PillReminderWorker];
 * this class previously duplicated all of it — a second copy of the channel
 * setup, the copy strings and the action wiring, which had already drifted out
 * of sync with the worker's version.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_TAKEN = "com.havn.app.ACTION_MARK_TAKEN"
        const val ACTION_SNOOZE = "com.havn.app.ACTION_SNOOZE"

        private const val EXTRA_MED_ID = "med_id"
        private const val EXTRA_USER_ID = "user_id"
        private const val EXTRA_NOTIF_ID = "notif_id"
        private const val EXTRA_TIME = "time"

        private const val SNOOZE_MINUTES = 15L

        fun markTakenIntent(
            context: Context,
            medId: Long,
            userId: Long,
            notifId: Int,
        ): PendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 50_000,
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_MARK_TAKEN
                putExtra(EXTRA_MED_ID, medId)
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_NOTIF_ID, notifId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        fun snoozeIntent(
            context: Context,
            medId: Long,
            userId: Long,
            notifId: Int,
            time: String,
        ): PendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 60_000,
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(EXTRA_MED_ID, medId)
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_NOTIF_ID, notifId)
                putExtra(EXTRA_TIME, time)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MARK_TAKEN -> handleMarkTaken(context, intent)
            ACTION_SNOOZE -> handleSnooze(context, intent)
        }
    }

    private fun handleMarkTaken(context: Context, intent: Intent) {
        val medId = intent.getLongExtra(EXTRA_MED_ID, -1L)
        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, -1)

        dismiss(context, notifId)
        if (medId <= 0 || userId <= 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = HavnDatabase.getInstance(context)
                val today = LocalDate.now()
                val zone = ZoneId.systemDefault()
                val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
                val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val now = System.currentTimeMillis()

                val existing = db.doseLogDao().getDoseLogForMedToday(medId, dayStart, dayEnd)
                if (existing != null) {
                    db.doseLogDao().updateDoseLog(existing.copy(status = "TAKEN", takenAt = now))
                } else {
                    db.doseLogDao().insertDoseLog(
                        DoseLogEntity(
                            medicationId = medId,
                            userId = userId,
                            // Anchored to the start of the day, matching how
                            // the repository writes dose logs. Stamping this
                            // with the current time instead put the log in a
                            // different bucket from an in-app entry for the
                            // same dose, so a dose taken from the notification
                            // could show up twice in history.
                            scheduledTime = dayStart,
                            takenAt = now,
                            status = "TAKEN",
                        )
                    )
                }
                updateHavnWidget(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Re-fires the same reminder in 15 minutes. Uses WorkManager rather than an
     * exact alarm — a quarter-hour reminder does not justify an exact-alarm
     * wakeup, and WorkManager survives process death without extra permissions.
     */
    private fun handleSnooze(context: Context, intent: Intent) {
        val medId = intent.getLongExtra(EXTRA_MED_ID, -1L)
        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, -1)

        val time = intent.getStringExtra(EXTRA_TIME).orEmpty()
        dismiss(context, notifId)
        if (medId <= 0 || time.isBlank()) return

        val data = workDataOf(
            PillReminderWorker.KEY_MED_ID to medId,
            PillReminderWorker.KEY_USER_ID to userId,
            PillReminderWorker.KEY_TIME to time,
            PillReminderWorker.KEY_IS_PRE_DOSE to false,
            PillReminderWorker.KEY_IS_EVENING_CHECK to false,
            PillReminderWorker.KEY_IS_TEST to false,
            // Tomorrow's dose is already enqueued by the reminder that is being
            // snoozed. Without this flag the snoozed re-fire would enqueue a
            // second one, and the medication would gradually accumulate
            // duplicate daily reminders.
            PillReminderWorker.KEY_IS_SNOOZE to true,
        )

        WorkManager.getInstance(context).enqueueUniqueWork(
            "pill_snooze_$medId",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<PillReminderWorker>()
                .setInitialDelay(SNOOZE_MINUTES, TimeUnit.MINUTES)
                .setInputData(data)
                .addTag("tag_med_$medId")
                .addTag("tag_pill_reminder")
                .build(),
        )
    }

    private fun dismiss(context: Context, notifId: Int) {
        if (notifId == -1) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notifId)
    }
}

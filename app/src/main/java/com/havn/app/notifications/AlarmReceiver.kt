package com.havn.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.havn.app.R
import com.havn.app.data.db.DoseLogEntity
import com.havn.app.data.db.HavnDatabase
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.prefs.dataStore
import com.havn.app.ui.MainActivity
import com.havn.app.widget.updateHavnWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.havn.app.data.repository.HavnRepository

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    
    @Inject lateinit var repository: HavnRepository
    @Inject lateinit var userPrefs: UserPreferences
    @Inject lateinit var alarmScheduler: HavnAlarmScheduler

    companion object {
        const val ACTION_MARK_TAKEN = "com.havn.app.ACTION_MARK_TAKEN"
        const val ACTION_MARK_ALL_TAKEN = "com.havn.app.ACTION_MARK_ALL_TAKEN"
        const val ACTION_SNOOZE = "com.havn.app.ACTION_SNOOZE"
        const val ACTION_SNOOZE_ALL = "com.havn.app.ACTION_SNOOZE_ALL"
        const val ACTION_DOSE_ALARM = "com.havn.app.ACTION_DOSE_ALARM"

        const val EXTRA_MED_ID = "med_id"
        const val EXTRA_MED_IDS = "med_ids"
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_NOTIF_ID = "notif_id"
        const val EXTRA_TIME = "time"
        const val EXTRA_TIME_MILLIS = "time_millis"
        const val EXTRA_SLOT = "slot"
        const val EXTRA_IS_SNOOZE = "is_snooze"
        private const val SNOOZE_MS = 15 * 60_000L

        const val BRAND_ACCENT = 0xFF516351.toInt()

        fun markTakenIntent(context: Context, medId: Long, userId: Long, notifId: Int, slot: String): PendingIntent = PendingIntent.getBroadcast(
            context, notifId + 50_000,
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_MARK_TAKEN
                putExtra(EXTRA_MED_ID, medId)
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_NOTIF_ID, notifId)
                putExtra(EXTRA_SLOT, slot)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        fun markAllTakenIntent(context: Context, medIds: LongArray, userId: Long, notifId: Int, slot: String): PendingIntent = PendingIntent.getBroadcast(
            context, notifId + 55_000,
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_MARK_ALL_TAKEN
                putExtra(EXTRA_MED_IDS, medIds)
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_NOTIF_ID, notifId)
                putExtra(EXTRA_SLOT, slot)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        fun snoozeIntent(context: Context, medId: Long, userId: Long, notifId: Int, slot: String): PendingIntent = PendingIntent.getBroadcast(
            context, notifId + 60_000,
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(EXTRA_MED_ID, medId)
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_NOTIF_ID, notifId)
                putExtra(EXTRA_SLOT, slot)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        
        fun snoozeAllIntent(context: Context, medIds: LongArray, userId: Long, notifId: Int, slot: String): PendingIntent = PendingIntent.getBroadcast(
            context, notifId + 65_000,
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_SNOOZE_ALL
                putExtra(EXTRA_MED_IDS, medIds)
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_NOTIF_ID, notifId)
                putExtra(EXTRA_SLOT, slot)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_MARK_TAKEN -> handleMarkTaken(context, intent)
                    ACTION_MARK_ALL_TAKEN -> handleMarkAllTaken(context, intent)
                    ACTION_SNOOZE -> handleSnooze(context, intent)
                    ACTION_SNOOZE_ALL -> handleSnoozeAll(context, intent)
                    ACTION_DOSE_ALARM -> handleDoseAlarm(context, intent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleMarkTaken(context: Context, intent: Intent) {
        val medId = intent.getLongExtra(EXTRA_MED_ID, -1L)
        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, -1)
        val slot = intent.getStringExtra(EXTRA_SLOT) ?: return
        dismiss(context, notifId)
        if (medId <= 0 || userId <= 0) return
        markMedicationTaken(context, medId, slot)
    }

    private suspend fun handleMarkAllTaken(context: Context, intent: Intent) {
        val medIds = intent.getLongArrayExtra(EXTRA_MED_IDS) ?: return
        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, -1)
        val slot = intent.getStringExtra(EXTRA_SLOT) ?: return
        dismiss(context, notifId)
        if (userId <= 0) return
        for (medId in medIds) {
            markMedicationTaken(context, medId, slot)
        }
    }

    private suspend fun markMedicationTaken(context: Context, medId: Long, slot: String) {
        val med = repository.getMedicationById(medId) ?: return
        repository.markDoseTaken(med, LocalDate.now(), slot)
        updateHavnWidget(context)
    }

    /**
     * Re-nudges the same slot in 15 minutes. The re-fire goes through
     * [handleDoseAlarm], which re-reads the logs, so anything taken in the
     * meantime is left out (and nothing fires if all of it was).
     */
    private fun handleSnooze(context: Context, intent: Intent) {
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, -1)
        val slot = intent.getStringExtra(EXTRA_SLOT) ?: return
        dismiss(context, notifId)
        val time = runCatching { LocalTime.parse(slot) }.getOrNull() ?: return
        val slotMillis = LocalDate.now().atTime(time)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmScheduler.scheduleSnooze(slotMillis, System.currentTimeMillis() + SNOOZE_MS)
    }

    private fun handleSnoozeAll(context: Context, intent: Intent) = handleSnooze(context, intent)

    private suspend fun handleDoseAlarm(context: Context, intent: Intent) {
        val timeMillis = intent.getLongExtra(EXTRA_TIME_MILLIS, -1L)
        if (timeMillis <= 0) return

        val slot = Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val timeStr = slot.toLocalTime().format(timeFormatter)

        val activeUserId = userPrefs.activeUserId.first()
        if (activeUserId < 0) return
        
        // Use default sounds for now, as we removed the custom settings
        val soundPref = "CHIME"
        val vibrationPref = true

        val meds = repository.getMedicationsForUser(activeUserId).first().filter { it.isActive && it.isScheduledOn(slot.toLocalDate()) }
        val slotMeds = meds.filter { med ->
            timeStr in med.reminderTimes
        }

        // Doses already logged in the app (or from an earlier nudge) are not
        // nudged again.
        val handled = repository.getDoseLogsForDay(activeUserId, slot.toLocalDate()).first()
            .filter { it.status != com.havn.app.domain.model.DoseStatus.PENDING }
            .map { it.medicationId to it.scheduledSlot }
            .toSet()
        val pending = slotMeds.filter { (it.id to timeStr) !in handled }

        // Keep the 14-day window rolling even if the app isn't opened.
        if (!intent.getBooleanExtra(EXTRA_IS_SNOOZE, false)) alarmScheduler.rebuildAlarms()

        if (pending.isEmpty()) return

        val displayTime = slot.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a")).replace("AM", "am").replace("PM", "pm")

        val title: String
        val body: String
        val medIds = pending.map { it.id }.toLongArray()

        if (pending.size == 1) {
            val med = pending.first()
            title = med.name
            body = buildString {
                if (med.dosage.isNotBlank()) append(med.dosage).append(" · ")
                append(displayTime)
            }
        } else {
            title = "${pending.size} doses at $displayTime"
            body = pending.joinToString(" · ") { it.name }
        }

        val notifId = (timeMillis / 60000).toInt().absoluteValue
        showNotification(context, notifId, title, body, medIds, activeUserId, soundPref, vibrationPref, timeStr)
    }

    private fun showNotification(
        context: Context, notifId: Int, title: String, body: String,
        medIds: LongArray, userId: Long, soundPref: String, vibrationPref: Boolean, slot: String
    ) {
        HavnNotificationChannels.ensureCreated(context)
        val channelId = HavnNotificationChannels.DOSE
        val silent = soundPref.equals("SILENT", ignoreCase = true)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, notifId, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(BRAND_ACCENT)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setSilent(silent)

        if (vibrationPref && !silent) {
            builder.setVibrate(longArrayOf(0, 140, 90, 140))
        }

        if (medIds.size == 1) {
            builder.addAction(R.drawable.ic_check, "Taken", markTakenIntent(context, medIds[0], userId, notifId, slot))
            builder.addAction(R.drawable.ic_notification, "In 15 min", snoozeIntent(context, medIds[0], userId, notifId, slot))
        } else {
            builder.addAction(R.drawable.ic_check, "All taken", markAllTakenIntent(context, medIds, userId, notifId, slot))
            builder.addAction(R.drawable.ic_notification, "In 15 min", snoozeAllIntent(context, medIds, userId, notifId, slot))
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, builder.build())
    }

    private fun dismiss(context: Context, notifId: Int) {
        if (notifId == -1) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notifId)
    }
}

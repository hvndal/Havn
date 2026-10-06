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
        const val ACTION_PRE_DOSE = "com.havn.app.ACTION_PRE_DOSE"
        const val ACTION_EVENING_CHECK = "com.havn.app.ACTION_EVENING_CHECK"
        const val ACTION_TEST = "com.havn.app.ACTION_TEST"

        const val EXTRA_MED_ID = "med_id"
        const val EXTRA_MED_IDS = "med_ids"
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_NOTIF_ID = "notif_id"
        const val EXTRA_TIME = "time"
        const val EXTRA_TIME_MILLIS = "time_millis"
        const val EXTRA_SLOT = "slot"
        const val EXTRA_IS_SNOOZE = "is_snooze"
        private const val SNOOZE_MS = 15 * 60_000L

        const val BRAND_ACCENT = 0xFF5E6E5D.toInt()

        // Notification-ID bands, so a dose, its early nudge, an evening
        // summary and a test can never overwrite one another.
        private const val PRE_DOSE_BAND = 300_000_000
        private const val EVENING_ID = 7_000_001
        private const val TEST_ID = 7_000_002

        /**
         * The status-bar ID for one profile's doses at one minute. Shared with
         * the widget so logging a dose there clears the same notification.
         */
        fun doseNotificationId(slotMillis: Long, userId: Long): Int =
            ((slotMillis / 60_000) * 31 + userId).toInt().absoluteValue % 200_000_000

        fun slotMillis(date: LocalDate, slot: String): Long? =
            runCatching { LocalTime.parse(slot) }.getOrNull()
                ?.let { date.atTime(it).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }

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
                    ACTION_PRE_DOSE -> handlePreDose(context, intent)
                    ACTION_EVENING_CHECK -> handleEveningCheck(context)
                    ACTION_TEST -> showTest(context)
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

    /** Doses due at [slot] for one profile that nobody has logged yet. */
    private suspend fun pendingAt(userId: Long, slot: java.time.LocalDateTime): List<com.havn.app.domain.model.Medication> {
        val timeStr = slot.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        val date = slot.toLocalDate()
        val due = repository.getMedicationsForUser(userId).first()
            .filter { it.isActive && it.isScheduledOn(date) && timeStr in it.reminderTimes }
        if (due.isEmpty()) return emptyList()
        // Doses already logged in the app, the widget or an earlier nudge are
        // not nudged again.
        val handled = repository.getDoseLogsForDay(userId, date).first()
            .filter { it.status != com.havn.app.domain.model.DoseStatus.PENDING }
            .map { it.medicationId to it.scheduledSlot }
            .toSet()
        return due.filter { (it.id to timeStr) !in handled }
    }

    private fun slotOf(millis: Long) =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()

    private fun displayTime(t: LocalTime) =
        t.format(DateTimeFormatter.ofPattern("h:mm a")).replace("AM", "am").replace("PM", "pm")

    private suspend fun handleDoseAlarm(context: Context, intent: Intent) {
        val timeMillis = intent.getLongExtra(EXTRA_TIME_MILLIS, -1L)
        if (timeMillis <= 0) return
        val slot = slotOf(timeMillis)
        val timeStr = slot.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))

        // Keep the 14-day window rolling even if the app isn't opened.
        if (!intent.getBooleanExtra(EXTRA_IS_SNOOZE, false)) alarmScheduler.rebuildAlarms()

        // Every profile on the device, not only the one open in the app.
        val users = repository.getAllUsers().first()
        for (user in users) {
            val pending = pendingAt(user.id, slot)
            if (pending.isEmpty()) continue
            val who = if (users.size > 1) "${user.name} · " else ""
            val time = displayTime(slot.toLocalTime())
            val (title, body) = if (pending.size == 1) {
                val med = pending.first()
                "$who${med.name}" to buildString {
                    if (med.dosage.isNotBlank()) append(med.dosage).append(" · ")
                    append(time)
                }
            } else {
                "$who${pending.size} doses at $time" to pending.joinToString(" · ") { it.name }
            }
            showNotification(
                context, doseNotificationId(timeMillis, user.id), title, body,
                pending.map { it.id }.toLongArray(), user.id, "CHIME", true, timeStr,
            )
        }
    }

    /** The optional heads-up 15 minutes before a dose. Informational only. */
    private suspend fun handlePreDose(context: Context, intent: Intent) {
        val slotMillis = intent.getLongExtra(EXTRA_TIME_MILLIS, -1L)
        if (slotMillis <= 0) return
        val slot = slotOf(slotMillis)
        val users = repository.getAllUsers().first()
        for (user in users) {
            val pending = pendingAt(user.id, slot)
            if (pending.isEmpty()) continue
            val who = if (users.size > 1) "${user.name} · " else ""
            post(
                context,
                HavnNotificationChannels.PRE_DOSE,
                (doseNotificationId(slotMillis, user.id) % 100_000_000) + PRE_DOSE_BAND,
                "${who}In 15 minutes",
                pending.joinToString(" · ") { it.name } + " at " + displayTime(slot.toLocalTime()),
            )
        }
    }

    /** One quiet summary in the evening, only if something is still open. */
    private suspend fun handleEveningCheck(context: Context) {
        alarmScheduler.rebuildAlarms()
        val now = java.time.LocalDateTime.now()
        val today = now.toLocalDate()
        val lines = mutableListOf<String>()
        val users = repository.getAllUsers().first()
        for (user in users) {
            val logs = repository.getDoseLogsForDay(user.id, today).first()
                .filter { it.status != com.havn.app.domain.model.DoseStatus.PENDING }
                .map { it.medicationId to it.scheduledSlot }
                .toSet()
            val open = repository.getMedicationsForUser(user.id).first()
                .filter { it.isActive && it.repeatType != com.havn.app.domain.model.RepeatType.AS_NEEDED && it.isScheduledOn(today) }
                .flatMap { med ->
                    med.reminderTimes
                        .filter { t -> runCatching { LocalTime.parse(t) }.getOrNull()?.let { !today.atTime(it).isAfter(now) } == true }
                        .filter { t -> (med.id to t) !in logs }
                        .map { med.name }
                }
            if (open.isEmpty()) continue
            val who = if (users.size > 1) "${user.name}: " else ""
            lines += who + open.distinct().joinToString(", ")
        }
        if (lines.isEmpty()) return
        post(context, HavnNotificationChannels.EVENING, EVENING_ID, "A few doses are still open", lines.joinToString("\n"))
    }

    /** "Test reminder" in Settings: always posts, regardless of schedule. */
    private fun showTest(context: Context) {
        post(
            context, HavnNotificationChannels.DOSE, TEST_ID,
            "Hävn test reminder",
            "This is how a dose reminder will look and sound.",
        )
    }

    private fun post(context: Context, channel: String, id: Int, title: String, body: String) {
        HavnNotificationChannels.ensureCreated(context)
        val open = PendingIntent.getActivity(
            context, id,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(BRAND_ACCENT)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(id, n)
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

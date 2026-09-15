package com.havn.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.havn.app.R
import com.havn.app.data.db.HavnDatabase
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.absoluteValue

@HiltWorker
class PillReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val database: HavnDatabase,
    private val userPreferences: UserPreferences,
    private val reminderScheduler: ReminderScheduler,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_MED_ID = "key_med_id"
        const val KEY_MED_NAME = "key_med_name"
        const val KEY_MED_DOSAGE = "key_med_dosage"
        const val KEY_TIME = "key_time"
        const val KEY_USER_ID = "key_user_id"
        const val KEY_IS_PRE_DOSE = "key_is_pre_dose"
        const val KEY_IS_EVENING_CHECK = "key_is_evening_check"
        const val KEY_IS_TEST = "key_is_test"
        const val KEY_IS_SNOOZE = "key_is_snooze"

        /** Sage, for the notification accent stripe and icon tint. */
        const val BRAND_ACCENT = 0xFF516351.toInt()

        const val EVENING_NOTIF_ID = 8888
        const val TEST_NOTIF_ID = 9999
    }

    override suspend fun doWork(): Result {
        val isTest = inputData.getBoolean(KEY_IS_TEST, false)
        val isEveningCheck = inputData.getBoolean(KEY_IS_EVENING_CHECK, false)
        val soundPref = userPreferences.reminderSound.first()
        val vibrationPref = userPreferences.reminderVibration.first()

        if (isTest) {
            showNotification(
                context = appContext,
                notifId = TEST_NOTIF_ID,
                title = "This is how a reminder looks",
                body = "Dose reminders arrive like this, at the times you set. " +
                    "You can mark a dose taken without opening Hävn.",
                medId = -1L,
                userId = -1L,
                soundPref = soundPref,
                vibrationPref = vibrationPref,
            )
            return Result.success()
        }

        if (isEveningCheck) {
            if (!userPreferences.eveningCheckEnabled.first()) return Result.success()
            handleEveningCheck(soundPref, vibrationPref)
            reminderScheduler.scheduleEveningCheck(userPreferences.eveningCheckTime.first())
            return Result.success()
        }

        val medId = inputData.getLong(KEY_MED_ID, -1L)
        val timeStr = inputData.getString(KEY_TIME) ?: ""
        val isPreDose = inputData.getBoolean(KEY_IS_PRE_DOSE, false)

        if (medId <= 0 || timeStr.isBlank()) return Result.success()

        val med = database.medicationDao().getMedicationById(medId)
        if (med == null || !med.isActive) return Result.success()

        // Only remind for the profile currently signed in. Without this, a
        // reminder enqueued under one profile keeps firing after the user
        // switches — naming a medication that is no longer theirs.
        val activeUserId = userPreferences.activeUserId.first()
        if (activeUserId < 0 || med.userId != activeUserId) return Result.success()

        // A time removed from the medication must stop firing, even if a
        // previously enqueued worker outlived the edit.
        val stillScheduled = runCatching {
            Json.decodeFromString<List<String>>(med.reminderTimesJson)
        }.getOrDefault(emptyList())
        if (timeStr !in stillScheduled) return Result.success()

        val today = LocalDate.now()
        val dayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val existingLog = database.doseLogDao().getDoseLogForMedToday(medId, dayStart, dayEnd)
        val alreadyHandled = existingLog?.status == "TAKEN" || existingLog?.status == "SKIPPED"

        if (!alreadyHandled) {
            val notifId = notificationId(medId, timeStr, isPreDose)
            val dosage = med.dosage.takeIf { it.isNotBlank() }

            // Deliberately steady copy. The previous version picked a random
            // line from a list of streak-flavoured nudges on every fire, so the
            // same daily reminder read differently each day and leaned on
            // pressure ("protect your streak") to drive adherence. For a
            // medication reminder, predictable and factual is the premium
            // register — the user should be able to read it at a glance,
            // half-awake, and know exactly what to do.
            val title = if (isPreDose) "${med.name} in 15 minutes" else med.name
            val body = buildString {
                if (dosage != null) append(dosage).append(" · ")
                append(formatClock(timeStr))
                if (isPreDose) append(" · coming up") else append(" · due now")
            }

            showNotification(
                context = appContext,
                notifId = notifId,
                title = title,
                body = body,
                medId = medId,
                userId = med.userId,
                soundPref = soundPref,
                vibrationPref = vibrationPref,
                isPreDose = isPreDose,
                snoozeTime = timeStr,
            )
        }

        // A snoozed re-fire must not enqueue tomorrow's dose: the reminder it
        // was snoozed from already did that. Otherwise each snooze quietly
        // stacks another daily reminder onto the medication.
        val isSnooze = inputData.getBoolean(KEY_IS_SNOOZE, false)
        if (!isSnooze && (!isPreDose || userPreferences.preDoseEnabled.first())) {
            reminderScheduler.scheduleNextDailyDose(medId, timeStr, isPreDose)
        }

        return Result.success()
    }

    private suspend fun handleEveningCheck(soundPref: String, vibrationPref: Boolean) {
        val activeUserId = userPreferences.activeUserId.first()
        if (activeUserId < 0) return

        val meds = database.medicationDao().getActiveMedicationsForUserSync(activeUserId)
        if (meds.isEmpty()) return

        val today = LocalDate.now()
        val dayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val logs = database.doseLogDao().getDoseLogsForDaySync(activeUserId, dayStart, dayEnd)
        val handledIds = logs
            .filter { it.status == "TAKEN" || it.status == "SKIPPED" }
            .map { it.medicationId }
            .toSet()
        val outstanding = meds.filter { it.id !in handledIds }

        if (outstanding.isEmpty()) return

        val names = outstanding.joinToString(", ") { it.name }
        val title = when (outstanding.size) {
            1 -> "One dose left today"
            else -> "${outstanding.size} doses left today"
        }

        showNotification(
            context = appContext,
            notifId = EVENING_NOTIF_ID,
            title = title,
            body = names,
            medId = if (outstanding.size == 1) outstanding.first().id else -1L,
            userId = activeUserId,
            soundPref = soundPref,
            vibrationPref = vibrationPref,
            isEveningCheck = true,
        )
    }

    private fun notificationId(medId: Long, timeStr: String, isPreDose: Boolean): Int {
        val base = (medId * 100 + (timeStr.hashCode().absoluteValue % 50)).toInt()
        return if (isPreDose) base + 50 else base
    }

    private fun formatClock(raw: String): String = runCatching {
        val time = java.time.LocalTime.parse(raw)
        time.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))
            .replace("AM", "am").replace("PM", "pm")
    }.getOrDefault(raw)

    private fun showNotification(
        context: Context,
        notifId: Int,
        title: String,
        body: String,
        medId: Long,
        userId: Long,
        soundPref: String,
        vibrationPref: Boolean,
        isPreDose: Boolean = false,
        isEveningCheck: Boolean = false,
        snoozeTime: String = "",
    ) {
        HavnNotificationChannels.ensureCreated(context)
        val channelId = HavnNotificationChannels.channelFor(isPreDose, isEveningCheck)
        val silent = soundPref.equals("SILENT", ignoreCase = true)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(BRAND_ACCENT)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                if (isPreDose || isEveningCheck) NotificationCompat.PRIORITY_DEFAULT
                else NotificationCompat.PRIORITY_HIGH
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setSilent(silent)

        if (vibrationPref && !silent) {
            builder.setVibrate(longArrayOf(0, 140, 90, 140))
        }

        if (medId > 0 && userId > 0) {
            builder.addAction(
                R.drawable.ic_check,
                "Mark taken",
                AlarmReceiver.markTakenIntent(context, medId, userId, notifId),
            )
            // Snooze matters more than it looks. Without it, the only ways to
            // clear a reminder you cannot act on right now are to dismiss it
            // (and forget) or to mark a dose you have not actually taken —
            // which corrupts the adherence record the app exists to keep.
            if (!isEveningCheck) {
                builder.addAction(
                    R.drawable.ic_notification,
                    "In 15 min",
                    AlarmReceiver.snoozeIntent(context, medId, userId, notifId, snoozeTime),
                )
            }
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, builder.build())
    }
}

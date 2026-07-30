package com.havn.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.havn.app.domain.model.Medication
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(medication: Medication) {
        medication.reminderTimes.forEachIndexed { i, timeStr ->
            val time = runCatching {
                LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
            }.getOrNull() ?: return@forEachIndexed

            val now = java.time.LocalDateTime.now()
            var alarmTime = LocalDate.now().atTime(time)
            if (!alarmTime.isAfter(now)) alarmTime = alarmTime.plusDays(1)

            val epochMs = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("med_id", medication.id)
                putExtra("med_name", medication.name)
                putExtra("med_dosage", medication.dosage)
                putExtra("reminder_index", i)
            }

            val requestCode = (medication.id * 100 + i).toInt()
            val pending = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, epochMs, pending
                )
            } catch (e: SecurityException) {
                // Fallback: inexact alarm
                alarmManager.set(AlarmManager.RTC_WAKEUP, epochMs, pending)
            }
        }
    }

    fun cancelReminder(medication: Medication) {
        medication.reminderTimes.forEachIndexed { i, _ ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = (medication.id * 100 + i).toInt()
            val pending = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            pending?.let { alarmManager.cancel(it) }
        }
    }
}

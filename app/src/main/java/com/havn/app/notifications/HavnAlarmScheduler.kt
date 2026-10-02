package com.havn.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.havn.app.data.repository.HavnRepository
import com.havn.app.data.prefs.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HavnAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: HavnRepository,
    private val prefs: UserPreferences,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    suspend fun rebuildAlarms() {
        val activeUserId = prefs.activeUserId.first()
        if (activeUserId < 0) {
            cancelAllCurrentlyScheduled()
            return
        }

        val meds = repository.getMedicationsForUser(activeUserId).first()
            .filter { it.isActive }

        // Find all unique LocalDateTime slots for the next 14 days
        val today = LocalDate.now()
        val slots = mutableSetOf<LocalDateTime>()

        for (i in 0..14) {
            val date = today.plusDays(i.toLong())
            val scheduledMeds = meds.filter { it.isScheduledOn(date) }
            
            for (med in scheduledMeds) {
                for (timeStr in med.reminderTimes) {
                    val time = runCatching { LocalTime.parse(timeStr, timeFormatter) }.getOrNull()
                    if (time != null) {
                        slots.add(date.atTime(time))
                    }
                }
            }
        }

        val now = LocalDateTime.now()
        val futureSlots = slots.filter { it.isAfter(now) }.sorted()

        cancelAllCurrentlyScheduled()
        val newScheduled = mutableListOf<Long>()

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        for (slot in futureSlots) {
            val epochMillis = slot.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val requestCode = (epochMillis / 60000).toInt()
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_DOSE_ALARM
                putExtra(AlarmReceiver.EXTRA_TIME_MILLIS, epochMillis)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    epochMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    epochMillis,
                    pendingIntent
                )
            }
            newScheduled.add(epochMillis)
        }
        
        prefs.saveScheduledAlarms(newScheduled)
    }

    private suspend fun cancelAllCurrentlyScheduled() {
        val currentlyScheduled = prefs.scheduledAlarms.first()
        for (epochMillis in currentlyScheduled) {
            val requestCode = (epochMillis / 60000).toInt()
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_DOSE_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        prefs.saveScheduledAlarms(emptyList())
    }
}

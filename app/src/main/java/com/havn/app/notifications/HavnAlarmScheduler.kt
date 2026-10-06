package com.havn.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import com.havn.app.domain.model.RepeatType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps a rolling 14-day window of reminder alarms.
 *
 * Alarms are per *minute*, not per medication: one alarm wakes the receiver,
 * which then works out every dose due at that minute for every profile on the
 * device. That is why a shared phone keeps reminding the second profile even
 * while the first one is the active profile in the UI.
 *
 * Three kinds of alarm are kept, each with its own action so their
 * PendingIntents never collide: the dose itself, an optional early nudge
 * 15 minutes before, and an optional evening check-in.
 */
@Singleton
class HavnAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: HavnRepository,
    private val prefs: UserPreferences,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    suspend fun rebuildAlarms() {
        val users = repository.getAllUsers().first()
        cancelAllCurrentlyScheduled()
        if (users.isEmpty()) return

        val meds = users.flatMap { repository.getMedicationsForUser(it.id).first() }
            .filter { it.isActive && it.repeatType != RepeatType.AS_NEEDED }

        val today = LocalDate.now()
        val slots = sortedSetOf<LocalDateTime>()
        for (i in 0..WINDOW_DAYS) {
            val date = today.plusDays(i.toLong())
            for (med in meds.filter { it.isScheduledOn(date) }) {
                for (timeStr in med.reminderTimes) {
                    val time = runCatching { LocalTime.parse(timeStr, timeFormatter) }.getOrNull() ?: continue
                    slots.add(date.atTime(time))
                }
            }
        }

        val now = LocalDateTime.now()
        val zone = ZoneId.systemDefault()
        val scheduled = mutableListOf<Long>()
        val preDose = prefs.preDoseEnabled.first()

        for (slot in slots) {
            val slotMillis = slot.atZone(zone).toInstant().toEpochMilli()
            if (slot.isAfter(now)) {
                set(AlarmReceiver.ACTION_DOSE_ALARM, slotMillis, slotMillis)
                scheduled.add(slotMillis)
            }
            val early = slot.minusMinutes(PRE_DOSE_MINUTES)
            if (preDose && early.isAfter(now)) {
                set(AlarmReceiver.ACTION_PRE_DOSE, slotMillis, early.atZone(zone).toInstant().toEpochMilli())
                scheduled.add(slotMillis)
            }
        }

        if (prefs.eveningCheckEnabled.first()) {
            val time = runCatching { LocalTime.parse(prefs.eveningCheckTime.first(), timeFormatter) }
                .getOrDefault(LocalTime.of(20, 0))
            for (i in 0..WINDOW_DAYS) {
                val at = today.plusDays(i.toLong()).atTime(time)
                if (!at.isAfter(now)) continue
                val millis = at.atZone(zone).toInstant().toEpochMilli()
                set(AlarmReceiver.ACTION_EVENING_CHECK, millis, millis)
                scheduled.add(millis)
            }
        }

        prefs.saveScheduledAlarms(scheduled.distinct())
    }

    /** One-off re-nudge for [slotMillis], firing at [atMillis]. */
    fun scheduleSnooze(slotMillis: Long, atMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DOSE_ALARM
            putExtra(AlarmReceiver.EXTRA_TIME_MILLIS, slotMillis)
            putExtra(AlarmReceiver.EXTRA_IS_SNOOZE, true)
        }
        val pending = PendingIntent.getBroadcast(
            context, requestCode(slotMillis) + SNOOZE_OFFSET, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending)
    }

    /**
     * A nudge, not an alarm clock: inexact but Doze-safe, and needs no special
     * "Alarms & reminders" permission. Usually lands within minutes.
     * [slotMillis] identifies the slot; [atMillis] is when it fires.
     */
    private fun set(action: String, slotMillis: Long, atMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra(AlarmReceiver.EXTRA_TIME_MILLIS, slotMillis)
        }
        val pending = PendingIntent.getBroadcast(
            context, requestCode(slotMillis), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending)
    }

    private suspend fun cancelAllCurrentlyScheduled() {
        for (millis in prefs.scheduledAlarms.first()) {
            for (action in ALL_ACTIONS) {
                val intent = Intent(context, AlarmReceiver::class.java).apply { this.action = action }
                PendingIntent.getBroadcast(
                    context, requestCode(millis), intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
                )?.let {
                    alarmManager.cancel(it)
                    it.cancel()
                }
            }
        }
        prefs.saveScheduledAlarms(emptyList())
    }

    companion object {
        private const val WINDOW_DAYS = 14
        const val PRE_DOSE_MINUTES = 15L
        private const val SNOOZE_OFFSET = 1_000_000
        private val ALL_ACTIONS = listOf(
            AlarmReceiver.ACTION_DOSE_ALARM,
            AlarmReceiver.ACTION_PRE_DOSE,
            AlarmReceiver.ACTION_EVENING_CHECK,
        )

        /** Minutes since the epoch — unique per slot, fits an Int until 6053. */
        fun requestCode(millis: Long): Int = (millis / 60_000).toInt()
    }
}

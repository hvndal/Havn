package com.havn.app.domain.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class User(
    val id: Long = 0,
    val name: String,
    val age: Int,
    val avatarColor: String = "#516351",
    val createdAt: Long = System.currentTimeMillis(),
)

data class Medication(
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val dosage: String,
    /** Sorted "HH:mm" times. Empty means "any time today". */
    val reminderTimes: List<String> = emptyList(),
    val repeatType: RepeatType = RepeatType.DAILY,
    val weeklyDays: List<Int> = emptyList(), // 1=Mon, 7=Sun
    val startDate: Long = 0L,
    val colorTag: String = "sage",
    val iconType: MedIconType = MedIconType.CAPSULE,
    val isActive: Boolean = true,
    val notes: String = "",
) {
    /**
     * One schedule rule used everywhere.
     * As Needed medications are never "scheduled" for a day, they just exist.
     * Daily and Weekly medications are scheduled on or after their start date.
     */
    fun isScheduledOn(date: java.time.LocalDate): Boolean {
        if (!isActive) return false
        if (repeatType == RepeatType.AS_NEEDED) return false
        
        val startLocalDate = java.time.Instant.ofEpochMilli(startDate)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            
        if (date.isBefore(startLocalDate)) return false
        
        return when (repeatType) {
            RepeatType.DAILY -> true
            RepeatType.WEEKLY -> date.dayOfWeek.value in weeklyDays
            else -> false
        }
    }
}

data class DoseLog(
    val id: Long = 0,
    val medicationId: Long,
    val userId: Long,
    val scheduledTime: Long,
    val takenAt: Long? = null,
    val status: DoseStatus = DoseStatus.PENDING,
    val scheduledSlot: String = "",
)

enum class RepeatType { DAILY, WEEKLY, AS_NEEDED }
enum class DoseStatus { PENDING, TAKEN, SKIPPED }
enum class MedIconType { CAPSULE, TABLET, LIQUID, POWDER, INJECTION }

/**
 * One scheduled dose on one day — a medication *and* the specific time it is
 * due. A twice-daily medication produces two of these, which is what lets Today
 * show, remind about and record each dose separately.
 */
data class TodayDose(
    val medication: Medication,
    val doseLog: DoseLog?,
    /** "HH:mm", or empty for an anytime dose. */
    val slot: String,
) {
    val status: DoseStatus get() = doseLog?.status ?: DoseStatus.PENDING
    val isTaken: Boolean get() = status == DoseStatus.TAKEN
    val isSkipped: Boolean get() = status == DoseStatus.SKIPPED
    val isPending: Boolean get() = status == DoseStatus.PENDING

    val time: LocalTime? = runCatching { LocalTime.parse(slot) }.getOrNull()

    /** Sorts anytime doses to the end rather than to midnight. */
    val sortKey: Int get() = time?.let { it.hour * 60 + it.minute } ?: Int.MAX_VALUE

    fun displayTime(): String = time
        ?.format(DateTimeFormatter.ofPattern("h:mm a"))
        ?.replace("AM", "am")?.replace("PM", "pm")
        ?: "Any time"

    /** Which part of the day this dose belongs to — drives the organizer. */
    val period: DayPeriod get() = DayPeriod.forHour(time?.hour)
}

enum class DayPeriod(val label: String, val range: String) {
    MORNING("Morning", "05:00 – 11:59"),
    AFTERNOON("Afternoon", "12:00 – 16:59"),
    EVENING("Evening", "17:00 – 20:59"),
    NIGHT("Night", "21:00 – 04:59");

    companion object {
        fun forHour(hour: Int?): DayPeriod = when (hour) {
            null -> MORNING
            in 5..11 -> MORNING
            in 12..16 -> AFTERNOON
            in 17..20 -> EVENING
            else -> NIGHT
        }

        fun current(): DayPeriod = forHour(LocalTime.now().hour)
    }
}

package com.havn.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val avatarColor: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "medications",
    indices = [Index("userId")],
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val dosage: String,
    val reminderTimesJson: String = "[]", // JSON ["07:30", "21:00"]
    val repeatType: String = "DAILY",
    val colorTag: String = "sage",
    val iconType: String = "CAPSULE",
    val isActive: Boolean = true,
    val notes: String = "",
)

@Entity(
    tableName = "dose_logs",
    indices = [
        Index("userId", "scheduledTime"),
        Index("medicationId", "scheduledTime"),
    ],
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val userId: Long,
    /** Start-of-day epoch millis. The day bucket this dose belongs to. */
    val scheduledTime: Long,
    val takenAt: Long? = null,
    val status: String = "PENDING", // PENDING, TAKEN, SKIPPED
    /**
     * Which scheduled time within the day this log is for, as "HH:mm".
     *
     * Added in schema v2. Without it a medication could only ever have one dose
     * log per day, while `reminderTimes` has always been a list and the
     * scheduler has always enqueued a reminder for every entry in it. The
     * result was that a twice-daily medication showed one row on Today, and the
     * evening reminder saw the morning dose already marked taken and silently
     * declined to notify — so the second dose of the day was never reminded and
     * never recorded.
     *
     * Empty string means "no specific time", which is also what v1 rows migrate
     * to so their existing history stays intact.
     */
    val scheduledSlot: String = "",
)

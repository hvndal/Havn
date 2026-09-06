package com.havn.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val avatarColor: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val dosage: String,
    val reminderTimesJson: String = "[]", // JSON [\"07:30\", \"21:00\"]
    val repeatType: String = "DAILY",
    val colorTag: String = "sage",
    val iconType: String = "CAPSULE",
    val isActive: Boolean = true,
    val notes: String = "",
    val shape: String = "CAPSULE",
    val secondaryColorTag: String? = null,
    val size: String = "MEDIUM",
    val scoreLine: String = "NONE",
    val imprint: String = "",
    val coating: String = "SATIN",
)

@Entity(tableName = "dose_logs")
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val userId: Long,
    val scheduledTime: Long,
    val takenAt: Long? = null,
    val status: String = "PENDING", // PENDING, TAKEN, SKIPPED
)

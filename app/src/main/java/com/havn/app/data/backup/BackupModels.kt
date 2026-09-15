package com.havn.app.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The on-disk shape of a Hävn export.
 *
 * Versioned from the start: this file is the user's medication history, and a
 * backup taken today has to still restore after the app's schema moves on.
 * [schemaVersion] is checked on import and an unknown future version is
 * refused rather than partially applied.
 */
@Serializable
data class HavnBackup(
    @SerialName("schema_version") val schemaVersion: Int = CURRENT_SCHEMA,
    @SerialName("app_version") val appVersion: String = "",
    @SerialName("exported_at") val exportedAt: Long = System.currentTimeMillis(),
    val profiles: List<BackupProfile> = emptyList(),
) {
    companion object {
        /** Bump when the shape below changes incompatibly. */
        const val CURRENT_SCHEMA = 1
    }

    val medicationCount: Int get() = profiles.sumOf { it.medications.size }
    val doseCount: Int get() = profiles.sumOf { it.doseLogs.size }
}

@Serializable
data class BackupProfile(
    val name: String,
    val age: Int = 0,
    @SerialName("avatar_color") val avatarColor: String = "#516351",
    @SerialName("created_at") val createdAt: Long = 0L,
    val medications: List<BackupMedication> = emptyList(),
    @SerialName("dose_logs") val doseLogs: List<BackupDoseLog> = emptyList(),
)

@Serializable
data class BackupMedication(
    /**
     * The medication's id *within this backup only*. Dose logs reference it so
     * the two stay linked through a restore; real database ids are reassigned
     * on import, because the rows may land in a database that already has data.
     */
    @SerialName("local_id") val localId: Long,
    val name: String,
    val dosage: String = "",
    @SerialName("reminder_times") val reminderTimes: List<String> = emptyList(),
    @SerialName("repeat_type") val repeatType: String = "DAILY",
    @SerialName("color_tag") val colorTag: String = "sage",
    @SerialName("icon_type") val iconType: String = "CAPSULE",
    @SerialName("is_active") val isActive: Boolean = true,
    val notes: String = "",
)

@Serializable
data class BackupDoseLog(
    @SerialName("medication_local_id") val medicationLocalId: Long,
    @SerialName("scheduled_time") val scheduledTime: Long,
    @SerialName("taken_at") val takenAt: Long? = null,
    val status: String = "PENDING",
    @SerialName("scheduled_slot") val scheduledSlot: String = "",
)

/** What an import did, so the UI can say something specific afterwards. */
sealed interface RestoreResult {
    data class Success(
        val profiles: Int,
        val medications: Int,
        val doses: Int,
    ) : RestoreResult

    data class Failure(val reason: String) : RestoreResult
}

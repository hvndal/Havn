package com.havn.app.domain.model

import kotlinx.serialization.Serializable

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
    val reminderTimes: List<String> = emptyList(), // "HH:mm"
    val repeatType: RepeatType = RepeatType.DAILY,
    val colorTag: String = "sage",
    val iconType: MedIconType = MedIconType.CAPSULE,
    val isActive: Boolean = true,
    val notes: String = "",
    // Visual properties
    val shape: MedShape = MedShape.CAPSULE,
    val secondaryColorTag: String? = null,
    val size: MedSize = MedSize.MEDIUM,
    val scoreLine: MedScoreLine = MedScoreLine.NONE,
    val imprint: String = "",
    val coating: MedCoating = MedCoating.SATIN,
) {
    val visualSpec: MedicationVisualSpec
        get() = MedicationVisualSpec(
            shape = shape,
            primaryColorTag = colorTag,
            secondaryColorTag = secondaryColorTag,
            size = size,
            scoreLine = scoreLine,
            imprint = imprint,
            coating = coating,
        )
}

data class DoseLog(
    val id: Long = 0,
    val medicationId: Long,
    val userId: Long,
    val scheduledTime: Long,
    val takenAt: Long? = null,
    val status: DoseStatus = DoseStatus.PENDING,
)

enum class RepeatType { DAILY, WEEKLY, AS_NEEDED }
enum class DoseStatus { PENDING, TAKEN, SKIPPED }
enum class MedIconType { CAPSULE, TABLET, LIQUID, POWDER, INJECTION }

data class TodayMedication(
    val medication: Medication,
    val doseLog: DoseLog?,
    val scheduledTime: String, // "HH:mm" formatted
)

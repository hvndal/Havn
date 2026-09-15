package com.havn.app.data.repository

import android.content.Context
import com.havn.app.data.db.*
import com.havn.app.domain.model.*
import com.havn.app.widget.updateHavnWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HavnRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val medicationDao: MedicationDao,
    private val doseLogDao: DoseLogDao,
) {
    private val zone: ZoneId get() = ZoneId.systemDefault()

    private fun LocalDate.startMillis(): Long =
        atStartOfDay(zone).toInstant().toEpochMilli()

    // ── Users ────────────────────────────────────────────────────────────────
    fun getAllUsers(): Flow<List<User>> =
        userDao.getAllUsers().map { list -> list.map { it.toDomain() } }

    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)?.toDomain()

    suspend fun createUser(user: User): Long {
        val id = userDao.insertUser(user.toEntity())
        updateHavnWidget(context)
        return id
    }

    suspend fun deleteUser(user: User) {
        // Children first. Deleting the parent row first left a window in which
        // the user was gone but their medications and logs were not, and any
        // observer that recomposed in between saw orphaned data.
        doseLogDao.deleteAllForUser(user.id)
        medicationDao.deleteAllForUser(user.id)
        userDao.deleteUser(user.toEntity())
        updateHavnWidget(context)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
        updateHavnWidget(context)
    }

    // ── Medications ──────────────────────────────────────────────────────────
    fun getMedicationsForUser(userId: Long): Flow<List<Medication>> =
        medicationDao.getMedicationsForUser(userId).map { list -> list.map { it.toDomain() } }

    suspend fun getMedicationById(id: Long): Medication? =
        medicationDao.getMedicationById(id)?.toDomain()

    suspend fun saveMedication(med: Medication): Long {
        val id = medicationDao.insertMedication(med.toEntity())
        updateHavnWidget(context)
        return id
    }

    suspend fun deleteMedication(med: Medication) {
        medicationDao.deleteMedication(med.toEntity())
        updateHavnWidget(context)
    }

    suspend fun updateMedication(med: Medication) {
        medicationDao.updateMedication(med.toEntity())
        updateHavnWidget(context)
    }

    // ── Dose logs ────────────────────────────────────────────────────────────
    fun getDoseLogsForDay(userId: Long, date: LocalDate): Flow<List<DoseLog>> =
        doseLogDao.getDoseLogsForDay(userId, date.startMillis(), date.plusDays(1).startMillis())
            .map { logs -> logs.map { it.toDomain() } }

    fun getDoseLogsForMonth(userId: Long, year: Int, month: Int): Flow<List<DoseLog>> {
        val start = LocalDate.of(year, month, 1)
        return doseLogDao.getDoseLogsForMonth(
            userId,
            start.startMillis(),
            start.plusMonths(1).startMillis(),
        ).map { logs -> logs.map { it.toDomain() } }
    }

    fun getDoseLogsForPastDays(userId: Long, days: Int = 30): Flow<List<DoseLog>> {
        val today = LocalDate.now()
        return doseLogDao.getDoseLogsForRange(
            userId,
            today.minusDays(days.toLong() - 1).startMillis(),
            today.plusDays(1).startMillis(),
        ).map { logs -> logs.map { it.toDomain() } }
    }

    /**
     * Records a dose outcome for one medication at one scheduled time.
     *
     * [slot] is what makes a twice-daily medication work: the morning and
     * evening doses are separate rows, so marking one taken no longer suppresses
     * the reminder for the other.
     */
    suspend fun setDoseStatus(
        medication: Medication,
        date: LocalDate,
        slot: String,
        status: DoseStatus,
    ) {
        val dayStart = date.startMillis()
        val dayEnd = date.plusDays(1).startMillis()
        val existing = doseLogDao.getDoseLogForSlot(medication.id, dayStart, dayEnd, slot)
        val now = System.currentTimeMillis()
        val takenAt = if (status == DoseStatus.PENDING) null else now

        if (existing != null) {
            doseLogDao.updateDoseLog(
                existing.copy(status = status.name, takenAt = takenAt, scheduledSlot = slot)
            )
        } else if (status != DoseStatus.PENDING) {
            // Only materialise a row once something actually happened. Writing
            // PENDING rows for every scheduled dose would inflate the database
            // and make "scheduled but not yet due" indistinguishable from
            // "deliberately left pending".
            doseLogDao.insertDoseLog(
                DoseLogEntity(
                    medicationId = medication.id,
                    userId = medication.userId,
                    scheduledTime = dayStart,
                    takenAt = takenAt,
                    status = status.name,
                    scheduledSlot = slot,
                )
            )
        }
        updateHavnWidget(context)
    }

    suspend fun markDoseTaken(medication: Medication, date: LocalDate, slot: String = "") =
        setDoseStatus(medication, date, slot, DoseStatus.TAKEN)

    suspend fun markDoseSkipped(medication: Medication, date: LocalDate, slot: String = "") =
        setDoseStatus(medication, date, slot, DoseStatus.SKIPPED)

    suspend fun markDoseUntaken(medication: Medication, date: LocalDate, slot: String = "") =
        setDoseStatus(medication, date, slot, DoseStatus.PENDING)

    /**
     * Fills in a month of plausible history so the Progress screen has
     * something to show during evaluation. Only reachable from Settings, behind
     * an explicit confirmation — it writes real rows into real history.
     */
    suspend fun seedSample30DayLogs(userId: Long) {
        val today = LocalDate.now()
        var meds = medicationDao.getActiveMedicationsForUserSync(userId)
        if (meds.isEmpty()) {
            listOf(
                MedicationEntity(
                    userId = userId, name = "Omega-3", dosage = "1000 mg",
                    reminderTimesJson = "[\"08:00\"]", colorTag = "clay", iconType = "LIQUID",
                ),
                MedicationEntity(
                    userId = userId, name = "Vitamin D3", dosage = "2000 IU",
                    reminderTimesJson = "[\"08:30\"]", colorTag = "amber", iconType = "TABLET",
                ),
                MedicationEntity(
                    userId = userId, name = "Magnesium", dosage = "400 mg",
                    reminderTimesJson = "[\"21:00\"]", colorTag = "slate", iconType = "CAPSULE",
                ),
            ).forEach { medicationDao.insertMedication(it) }
            meds = medicationDao.getActiveMedicationsForUserSync(userId)
        }

        val rows = mutableListOf<DoseLogEntity>()
        for (daysAgo in 29 downTo 0) {
            val date = today.minusDays(daysAgo.toLong())
            val dayStart = date.startMillis()
            meds.forEach { med ->
                val slots = runCatching {
                    Json.decodeFromString<List<String>>(med.reminderTimesJson)
                }.getOrDefault(emptyList()).ifEmpty { listOf("") }

                slots.forEach { slot ->
                    val seed = (date.dayOfYear * 13 + med.id * 7 + slot.hashCode()).toInt()
                    val skipped = Math.floorMod(seed, 11) == 0 && daysAgo > 0
                    rows += DoseLogEntity(
                        medicationId = med.id,
                        userId = userId,
                        scheduledTime = dayStart,
                        takenAt = if (skipped) dayStart else dayStart + 9 * 3_600_000L,
                        status = if (skipped) "SKIPPED" else "TAKEN",
                        scheduledSlot = slot,
                    )
                }
            }
        }
        doseLogDao.insertDoseLogs(rows)
        updateHavnWidget(context)
    }

    /** Wipes dose history for a profile, leaving medications in place. */
    suspend fun clearHistory(userId: Long) {
        doseLogDao.deleteAllForUser(userId)
        updateHavnWidget(context)
    }

    // ── Mappers ──────────────────────────────────────────────────────────────
    private fun UserEntity.toDomain() = User(id, name, age, avatarColor, createdAt)
    private fun User.toEntity() = UserEntity(id, name, age, avatarColor, createdAt)

    private fun MedicationEntity.toDomain() = Medication(
        id = id,
        userId = userId,
        name = name,
        dosage = dosage,
        // A malformed times payload must not crash the whole medication list;
        // an unparseable value degrades to "no scheduled time".
        reminderTimes = runCatching {
            Json.decodeFromString<List<String>>(reminderTimesJson)
        }.getOrDefault(emptyList()).sorted(),
        repeatType = runCatching { RepeatType.valueOf(repeatType) }.getOrDefault(RepeatType.DAILY),
        colorTag = colorTag,
        iconType = runCatching { MedIconType.valueOf(iconType) }.getOrDefault(MedIconType.CAPSULE),
        isActive = isActive,
        notes = notes,
    )

    private fun Medication.toEntity() = MedicationEntity(
        id = id,
        userId = userId,
        name = name,
        dosage = dosage,
        reminderTimesJson = Json.encodeToString(reminderTimes),
        repeatType = repeatType.name,
        colorTag = colorTag,
        iconType = iconType.name,
        isActive = isActive,
        notes = notes,
    )

    private fun DoseLogEntity.toDomain() = DoseLog(
        id = id,
        medicationId = medicationId,
        userId = userId,
        scheduledTime = scheduledTime,
        takenAt = takenAt,
        status = runCatching { DoseStatus.valueOf(status) }.getOrDefault(DoseStatus.PENDING),
        scheduledSlot = scheduledSlot,
    )
}

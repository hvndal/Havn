package com.havn.app.data.repository

import com.havn.app.data.db.*
import com.havn.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HavnRepository @Inject constructor(
    private val userDao: UserDao,
    private val medicationDao: MedicationDao,
    private val doseLogDao: DoseLogDao,
) {
    // ── Users ──────────────────────────────────────────────────────────────────
    fun getAllUsers(): Flow<List<User>> =
        userDao.getAllUsers().map { list -> list.map { it.toDomain() } }

    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)?.toDomain()

    suspend fun getMedicationById(id: Long, userId: Long): Medication? =
        medicationDao.getMedicationById(id, userId)?.toDomain()

    suspend fun createUser(user: User): Long =
        userDao.insertUser(user.toEntity())

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity())
        doseLogDao.deleteAllForUser(user.id)
    }

    suspend fun updateUser(user: User) = userDao.updateUser(user.toEntity())

    // ── Medications ────────────────────────────────────────────────────────────
    fun getMedicationsForUser(userId: Long): Flow<List<Medication>> =
        medicationDao.getMedicationsForUser(userId).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun saveMedication(med: Medication): Long =
        medicationDao.insertMedication(med.toEntity())

    suspend fun deleteMedication(med: Medication) =
        medicationDao.deleteMedication(med.toEntity())

    suspend fun updateMedication(med: Medication) =
        medicationDao.updateMedication(med.toEntity())

    // ── Dose Logs ──────────────────────────────────────────────────────────────
    fun getDoseLogsForDay(userId: Long, date: LocalDate): Flow<List<DoseLog>> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return doseLogDao.getDoseLogsForDay(userId, start, end).map { it.map { e -> e.toDomain() } }
    }

    fun getDoseLogsForMonth(userId: Long, year: Int, month: Int): Flow<List<DoseLog>> {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.of(year, month, 1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = LocalDate.of(year, month, 1).plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return doseLogDao.getDoseLogsForMonth(userId, start, end).map { it.map { e -> e.toDomain() } }
    }

    suspend fun markDoseTaken(medication: Medication, date: LocalDate) {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val existing = doseLogDao.getDoseLogForMedToday(medication.id, medication.userId, start, end)
        val takenNow = System.currentTimeMillis()
        if (existing != null) {
            doseLogDao.updateDoseLog(existing.copy(status = "TAKEN", takenAt = takenNow))
        } else {
            doseLogDao.insertDoseLog(
                DoseLogEntity(
                    medicationId = medication.id,
                    userId = medication.userId,
                    scheduledTime = start,
                    takenAt = takenNow,
                    status = "TAKEN",
                )
            )
        }
    }

    suspend fun markDoseUntaken(medication: Medication, date: LocalDate) {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val existing = doseLogDao.getDoseLogForMedToday(medication.id, medication.userId, start, end)
        if (existing != null) {
            doseLogDao.updateDoseLog(existing.copy(status = "PENDING", takenAt = null))
        }
    }

    // ── Mappers ────────────────────────────────────────────────────────────────
    private fun UserEntity.toDomain() = User(id, name, age, avatarColor, createdAt)
    private fun User.toEntity() = UserEntity(id, name, age, avatarColor, createdAt)

    private fun MedicationEntity.toDomain() = Medication(
        id = id,
        userId = userId,
        name = name,
        dosage = dosage,
        reminderTimes = Json.decodeFromString(reminderTimesJson),
        repeatType = RepeatType.valueOf(repeatType),
        colorTag = colorTag,
        iconType = MedIconType.valueOf(iconType),
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
        status = DoseStatus.valueOf(status),
    )
}

package com.havn.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    suspend fun getAllUsersSync(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE userId = :userId AND isActive = 1 ORDER BY id ASC")
    fun getMedicationsForUser(userId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE userId = :userId AND isActive = 1 ORDER BY id ASC")
    suspend fun getActiveMedicationsForUserSync(userId: Long): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(med: MedicationEntity): Long

    @Delete
    suspend fun deleteMedication(med: MedicationEntity)

    @Update
    suspend fun updateMedication(med: MedicationEntity)

    @Query("SELECT * FROM medications WHERE userId = :userId ORDER BY id ASC")
    suspend fun getAllForUserSync(userId: Long): List<MedicationEntity>

    @Query("DELETE FROM medications WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs WHERE userId = :userId AND scheduledTime >= :dayStart AND scheduledTime < :dayEnd ORDER BY scheduledTime ASC")
    fun getDoseLogsForDay(userId: Long, dayStart: Long, dayEnd: Long): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE userId = :userId AND scheduledTime >= :dayStart AND scheduledTime < :dayEnd ORDER BY scheduledTime ASC")
    suspend fun getDoseLogsForDaySync(userId: Long, dayStart: Long, dayEnd: Long): List<DoseLogEntity>

    @Query("SELECT * FROM dose_logs WHERE userId = :userId AND scheduledTime >= :monthStart AND scheduledTime < :monthEnd ORDER BY scheduledTime ASC")
    fun getDoseLogsForMonth(userId: Long, monthStart: Long, monthEnd: Long): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE userId = :userId AND scheduledTime >= :startTime AND scheduledTime < :endTime ORDER BY scheduledTime ASC")
    fun getDoseLogsForRange(userId: Long, startTime: Long, endTime: Long): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE userId = :userId AND scheduledTime >= :startTime AND scheduledTime < :endTime ORDER BY scheduledTime ASC")
    suspend fun getDoseLogsForRangeSync(userId: Long, startTime: Long, endTime: Long): List<DoseLogEntity>

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medicationId AND scheduledTime >= :dayStart AND scheduledTime < :dayEnd ORDER BY id ASC LIMIT 1")
    suspend fun getDoseLogForMedToday(medicationId: Long, dayStart: Long, dayEnd: Long): DoseLogEntity?

    /**
     * The log for one specific scheduled time on one day.
     *
     * Falls back to matching an empty slot so rows written before schema v2 —
     * and medications with no set time — still resolve.
     */
    @Query(
        "SELECT * FROM dose_logs WHERE medicationId = :medicationId " +
            "AND scheduledTime >= :dayStart AND scheduledTime < :dayEnd " +
            "AND (scheduledSlot = :slot OR (:slot = '' AND scheduledSlot = '')) " +
            "ORDER BY id ASC LIMIT 1"
    )
    suspend fun getDoseLogForSlot(
        medicationId: Long,
        dayStart: Long,
        dayEnd: Long,
        slot: String,
    ): DoseLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(log: DoseLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLogs(logs: List<DoseLogEntity>)

    @Update
    suspend fun updateDoseLog(log: DoseLogEntity)

    @Delete
    suspend fun deleteDoseLog(log: DoseLogEntity)

    @Query("SELECT * FROM dose_logs WHERE userId = :userId ORDER BY scheduledTime ASC")
    suspend fun getAllForUserSync(userId: Long): List<DoseLogEntity>

    @Query("DELETE FROM dose_logs WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}

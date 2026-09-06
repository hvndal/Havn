package com.havn.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

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

    @Query("SELECT * FROM medications WHERE id = :id AND userId = :userId")
    suspend fun getMedicationById(id: Long, userId: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(med: MedicationEntity): Long

    @Delete
    suspend fun deleteMedication(med: MedicationEntity)

    @Update
    suspend fun updateMedication(med: MedicationEntity)
}

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs WHERE userId = :userId AND scheduledTime >= :dayStart AND scheduledTime < :dayEnd ORDER BY scheduledTime ASC")
    fun getDoseLogsForDay(userId: Long, dayStart: Long, dayEnd: Long): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE userId = :userId AND scheduledTime >= :monthStart AND scheduledTime < :monthEnd ORDER BY scheduledTime ASC")
    fun getDoseLogsForMonth(userId: Long, monthStart: Long, monthEnd: Long): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medicationId AND userId = :userId AND scheduledTime >= :dayStart AND scheduledTime < :dayEnd LIMIT 1")
    suspend fun getDoseLogForMedToday(medicationId: Long, userId: Long, dayStart: Long, dayEnd: Long): DoseLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(log: DoseLogEntity): Long

    @Update
    suspend fun updateDoseLog(log: DoseLogEntity)

    @Delete
    suspend fun deleteDoseLog(log: DoseLogEntity)

    @Query("DELETE FROM dose_logs WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}

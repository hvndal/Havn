package com.havn.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, MedicationEntity::class, DoseLogEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class HavnDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun medicationDao(): MedicationDao
    abstract fun doseLogDao(): DoseLogDao

    companion object {
        @Volatile private var INSTANCE: HavnDatabase? = null

        fun getInstance(context: Context): HavnDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    HavnDatabase::class.java,
                    "havn_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

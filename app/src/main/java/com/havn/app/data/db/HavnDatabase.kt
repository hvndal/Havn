package com.havn.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UserEntity::class, MedicationEntity::class, DoseLogEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class HavnDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun medicationDao(): MedicationDao
    abstract fun doseLogDao(): DoseLogDao

    companion object {
        @Volatile private var INSTANCE: HavnDatabase? = null

        /**
         * v1 → v2: per-slot dose logs, plus indices.
         *
         * A real migration rather than `fallbackToDestructiveMigration()` —
         * this database holds the user's medication history, which is the
         * entire value of the app. Existing rows get an empty slot, which is
         * treated as "the day's single dose" so nothing already recorded
         * changes meaning.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE dose_logs ADD COLUMN scheduledSlot TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_dose_logs_userId_scheduledTime " +
                        "ON dose_logs (userId, scheduledTime)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_dose_logs_medicationId_scheduledTime " +
                        "ON dose_logs (medicationId, scheduledTime)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_medications_userId " +
                        "ON medications (userId)"
                )
            }
        }

        /**
         * v2 → v3: Add weeklyDaysJson and startDate to medications.
         * Existing medications get their start date from their oldest logged dose,
         * or fallback to current time, so no history is lost.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN weeklyDaysJson TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE medications ADD COLUMN startDate INTEGER NOT NULL DEFAULT 0")
                
                db.execSQL("""
                    UPDATE medications 
                    SET startDate = COALESCE(
                        (SELECT MIN(scheduledTime) FROM dose_logs WHERE dose_logs.medicationId = medications.id),
                        (CAST(strftime('%s', 'now') AS INTEGER) * 1000)
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): HavnDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HavnDatabase::class.java,
                    "havn_db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

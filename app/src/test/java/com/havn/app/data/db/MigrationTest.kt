package com.havn.app.data.db

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * An update from the very first Play release must open without crashing and
 * keep every row. Builds a database exactly as Room created it at schema v1,
 * then opens it with the current Room setup (which runs 1→2→3 and validates
 * the result against today's entities — any mismatch throws here, rather than
 * on a user's phone at launch).
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], application = Application::class)
class MigrationTest {

    @Test
    fun v1DatabaseMigratesToCurrentWithDataIntact() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val name = "migration-test.db"
        context.deleteDatabase(name)

        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name).apply { parentFile?.mkdirs() }, null).use { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `age` INTEGER NOT NULL, `avatarColor` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `medications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `dosage` TEXT NOT NULL, `reminderTimesJson` TEXT NOT NULL, `repeatType` TEXT NOT NULL, `colorTag` TEXT NOT NULL, `iconType` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `notes` TEXT NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `dose_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `medicationId` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `scheduledTime` INTEGER NOT NULL, `takenAt` INTEGER, `status` TEXT NOT NULL)")
            db.execSQL("INSERT INTO users VALUES (1, 'Elin', 34, '#516351', 1700000000000)")
            db.execSQL("INSERT INTO medications VALUES (1, 1, 'Vitamin D', '1000 IU', '[\"08:00\"]', 'DAILY', 'sage', 'CAPSULE', 1, '')")
            db.execSQL("INSERT INTO dose_logs VALUES (1, 1, 1, 1700000000000, 1700000100000, 'TAKEN')")
            db.version = 1
        }

        val room = Room.databaseBuilder(context, HavnDatabase::class.java, name)
            .addMigrations(HavnDatabase.MIGRATION_1_2, HavnDatabase.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()

        runBlocking {
            val meds = room.medicationDao().getActiveMedicationsForUserSync(1)
            assertEquals(1, meds.size)
            assertEquals("Vitamin D", meds[0].name)
            // 2→3 back-fills the start date from the oldest logged dose.
            assertEquals(1700000000000, meds[0].startDate)
            assertEquals("[]", meds[0].weeklyDaysJson)

            val logs = room.doseLogDao().getDoseLogsForDaySync(1, 0, Long.MAX_VALUE)
            assertEquals(1, logs.size)
            assertEquals("", logs[0].scheduledSlot)
        }
        room.close()
    }
}

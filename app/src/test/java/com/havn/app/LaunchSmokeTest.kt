package com.havn.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havn.app.data.db.DoseLogEntity
import com.havn.app.data.db.HavnDatabase
import com.havn.app.data.db.MedicationEntity
import com.havn.app.data.db.UserEntity
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.ui.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

/**
 * Boots the real app — Hilt graph, Room, DataStore, alarms, Compose — and
 * walks every screen a user can reach, failing on any crash. Each screen is
 * also rendered to `app/build/screenshots/` so a release can be eyeballed
 * without a device.
 *
 * It is one test on purpose: Room and DataStore hold process-wide singletons,
 * and Robolectric gives each test a fresh data directory, so splitting this up
 * would test stale singletons rather than the app.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LaunchSmokeTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val outDir = File("build/screenshots").apply { mkdirs() }

    @Test
    fun everyScreenLaunchesAndRenders() {
        // Infinite ambient animations would otherwise keep the clock busy forever.
        compose.mainClock.autoAdvance = false

        // ── 1. Fresh install: onboarding ────────────────────────────────────
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitFor("No account, no cloud")
            snap(scenario, "01-welcome")
        }

        // ── 2. A real profile with a realistic day ──────────────────────────
        seed()

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitFor("Good ")
            waitFor("Metformin")
            snap(scenario, "02-today")

            tab("Organizer"); waitFor("MODEL H.01"); snap(scenario, "03-organizer")
            tab("Progress"); pump(1500); snap(scenario, "04-progress")
            tab("Settings"); pump(1500); snap(scenario, "05-settings")
            tab("Today"); waitFor("Metformin")

            compose.onNode(hasContentDescription("Reminders")).performClick()
            pump(1500); snap(scenario, "06-reminders")
            back(scenario)

            // Today's "Manage" link sits below the fold; Settings → Medications
            // reaches the same screen and is always visible.
            tab("Settings")
            compose.onAllNodes(hasText("Medications")).onFirst().performClick()
            pump(1500); snap(scenario, "07-manage")

            // Manage pins "Add a medication" to the bottom, always on screen.
            compose.onAllNodes(hasText("Add a medication")).onFirst().performClick()
            pump(1500); snap(scenario, "08-add")
            back(scenario)
            back(scenario)
            pump(500)

            scenario.onActivity { assertFalse("Activity finished unexpectedly", it.isFinishing) }
        }

        // ── 3. Dark mode renders too ────────────────────────────────────────
        runBlocking { UserPreferences(context).setTheme("DARK") }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitFor("Metformin")
            snap(scenario, "09-today-dark")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun seed() = runBlocking {
        val db = HavnDatabase.getInstance(context)
        val userId = db.userDao().insertUser(UserEntity(name = "Elin", age = 34, avatarColor = "#5E6E5D"))
        fun med(name: String, dose: String, times: String, type: String, tag: String, repeat: String = "DAILY") =
            MedicationEntity(
                userId = userId, name = name, dosage = dose, reminderTimesJson = times,
                repeatType = repeat, iconType = type, colorTag = tag,
            )
        val vitD = db.medicationDao().insertMedication(med("Vitamin D", "1000 IU", """["08:00"]""", "CAPSULE", "sage"))
        db.medicationDao().insertMedication(med("Metformin", "500 mg", """["08:00","20:00"]""", "TABLET", "sand"))
        db.medicationDao().insertMedication(med("Omega-3", "1 softgel", """["08:30"]""", "LIQUID", "amber"))
        db.medicationDao().insertMedication(med("Magnesium", "250 mg", """["21:30"]""", "POWDER", "slate"))
        db.medicationDao().insertMedication(med("Ibuprofen", "200 mg", "[]", "TABLET", "clay", repeat = "AS_NEEDED"))

        val dayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        db.doseLogDao().insertDoseLog(
            DoseLogEntity(
                medicationId = vitD, userId = userId, scheduledTime = dayStart,
                takenAt = System.currentTimeMillis(), status = "TAKEN", scheduledSlot = "08:00",
            )
        )

        val prefs = UserPreferences(context)
        prefs.setActiveUser(userId)
        prefs.setOnboardingDone(true)
    }

    private fun tab(label: String) {
        compose.onNode(
            hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        ).performClick()
        pump(600)
    }

    private fun back(scenario: ActivityScenario<MainActivity>) {
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        pump(800)
    }

    /** Advance frames and drain the main looper for [ms] of wall time. */
    private fun pump(ms: Long = 400) {
        val end = System.currentTimeMillis() + ms
        while (System.currentTimeMillis() < end) {
            compose.mainClock.advanceTimeBy(48)
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(12)
        }
    }

    private fun waitFor(text: String, timeoutMs: Long = 20_000) {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            pump(150)
            val found = runCatching {
                compose.onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            }.getOrDefault(false)
            if (found) {
                pump(1200) // let entrance animations settle before a screenshot
                return
            }
        }
        fail("Timed out waiting for \"$text\" on screen")
    }

    private fun snap(scenario: ActivityScenario<MainActivity>, name: String) {
        scenario.onActivity { activity ->
            val view = activity.window.decorView
            if (view.width == 0 || view.height == 0) return@onActivity
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(outDir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}

private fun androidx.compose.ui.test.SemanticsNodeInteractionCollection.onFirst() = this[0]

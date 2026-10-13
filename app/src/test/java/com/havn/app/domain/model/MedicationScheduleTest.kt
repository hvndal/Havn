package com.havn.app.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class MedicationScheduleTest {

    @Test
    fun `as needed medications are never scheduled`() {
        val med = Medication(
            userId = 1,
            name = "Test",
            dosage = "10mg",
            repeatType = RepeatType.AS_NEEDED,
            startDate = 0L,
            isActive = true
        )
        assertFalse(med.isScheduledOn(LocalDate.now()))
    }

    @Test
    fun `daily medication scheduled on or after start date`() {
        val today = LocalDate.now()
        val startMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val med = Medication(
            userId = 1,
            name = "Test",
            dosage = "10mg",
            repeatType = RepeatType.DAILY,
            startDate = startMillis,
            isActive = true
        )
        
        assertFalse(med.isScheduledOn(today.minusDays(1)))
        assertTrue(med.isScheduledOn(today))
        assertTrue(med.isScheduledOn(today.plusDays(1)))
    }

    @Test
    fun `weekly medication scheduled only on selected days after start date`() {
        val today = LocalDate.of(2023, 10, 2) // Monday
        val startMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val med = Medication(
            userId = 1,
            name = "Test",
            dosage = "10mg",
            repeatType = RepeatType.WEEKLY,
            weeklyDays = listOf(1, 3, 5), // Mon, Wed, Fri
            startDate = startMillis,
            isActive = true
        )
        
        assertFalse(med.isScheduledOn(today.minusDays(1))) // Sunday, before start
        assertTrue(med.isScheduledOn(today)) // Monday
        assertFalse(med.isScheduledOn(today.plusDays(1))) // Tuesday
        assertTrue(med.isScheduledOn(today.plusDays(2))) // Wednesday
    }

    @Test
    fun `paused medication is never scheduled`() {
        val today = LocalDate.now()
        val startMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val med = Medication(
            userId = 1,
            name = "Test",
            dosage = "10mg",
            repeatType = RepeatType.DAILY,
            startDate = startMillis,
            isActive = false
        )
        assertFalse(med.isScheduledOn(today))
    }
}

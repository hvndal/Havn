package com.havn.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MedicationVisualsTest {

    @Test
    fun testMedicationVisualDefaults() {
        val med = Medication(
            userId = 1L,
            name = "Test Aspirin",
            dosage = "100mg"
        )

        assertEquals(MedShape.CAPSULE, med.shape)
        assertEquals(MedSize.MEDIUM, med.size)
        assertEquals(MedScoreLine.NONE, med.scoreLine)
        assertEquals(MedCoating.SATIN, med.coating)

        val spec = med.visualSpec
        assertEquals(MedShape.CAPSULE, spec.shape)
        assertEquals("sage", spec.primaryColorTag)
    }

    @Test
    fun testMedicationVisualSpecCustomProperties() {
        val med = Medication(
            id = 1L,
            userId = 1L,
            name = "Round Tablet",
            dosage = "50mg",
            colorTag = "terracotta",
            secondaryColorTag = "butter",
            shape = MedShape.ROUND_TABLET,
            size = MedSize.LARGE,
            scoreLine = MedScoreLine.SINGLE,
            imprint = "H50",
            coating = MedCoating.MATTE
        )

        val spec = med.visualSpec
        assertEquals(MedShape.ROUND_TABLET, spec.shape)
        assertEquals("terracotta", spec.primaryColorTag)
        assertEquals("butter", spec.secondaryColorTag)
        assertEquals(MedSize.LARGE, spec.size)
        assertEquals(MedScoreLine.SINGLE, spec.scoreLine)
        assertEquals("H50", spec.imprint)
        assertEquals(MedCoating.MATTE, spec.coating)
    }
}

package com.havn.app.ui.components

import com.havn.app.domain.model.DayPeriod
import com.havn.app.domain.model.MedIconType
import com.havn.app.domain.model.Medication
import com.havn.app.domain.model.TodayDose
import org.json.JSONArray
import org.json.JSONObject

/**
 * Turns the day's doses into the payload the 3D organiser renders.
 *
 * Replaces `OrganizerWeekMapper`, which exposed two mutually incompatible
 * shapes — a four-slot *period* payload and a seven-slot *week* payload. The
 * organizer screen drew seven day chips while the view model fed it the
 * four-slot payload, so tapping Friday, Saturday or Sunday fell outside the
 * slot array and the 3D view silently ignored it. There is now one shape:
 * the four periods of a day, which is what the physical object actually
 * represents.
 */
object OrganizerDataMapper {

    /** Hex accents shared with the 3D renderer. */
    fun colorTagToHex(tag: String): String = when (tag) {
        "clay", "terracotta" -> "#C77A52"
        "amber", "butter" -> "#D9AE5F"
        "slate" -> "#7E929A"
        "sand" -> "#A89878"
        else -> "#7E9A7C"
    }

    private fun shapeFor(med: Medication): String = when (med.iconType) {
        MedIconType.LIQUID -> "softgel"
        MedIconType.TABLET -> "tablet"
        MedIconType.CAPSULE -> "capsule"
        MedIconType.POWDER -> "tablet"
        MedIconType.INJECTION -> "capsule"
    }

    private fun pillJson(dose: TodayDose): JSONObject {
        val med = dose.medication
        val primary = colorTagToHex(med.colorTag)
        val shape = shapeFor(med)
        return JSONObject()
            .put("id", med.id)
            .put("name", med.name)
            .put("dosage", med.dosage)
            .put("shape", shape)
            .put("color", primary)
            .put("secondaryColor", if (shape == "capsule") "#F2EFE9" else primary)
            .put("isTaken", dose.isTaken)
            .put("size", 0.09)
    }

    fun buildPeriodJson(doses: List<TodayDose>): String {
        val current = DayPeriod.current()
        val slots = JSONArray()

        DayPeriod.entries.forEachIndexed { index, period ->
            val inPeriod = doses.filter { it.period == period }
            val pills = JSONArray().apply {
                inPeriod.forEach { put(pillJson(it)) }
            }
            slots.put(
                JSONObject()
                    .put("slotIndex", index)
                    .put("label", period.label.uppercase())
                    .put("period", period.range)
                    .put("count", inPeriod.size)
                    // "Complete" must mean *resolved*, not *taken*. A skipped
                    // dose is a decision the user made; leaving the compartment
                    // looking outstanding all evening ignores it.
                    .put("isCompleted", inPeriod.isNotEmpty() && inPeriod.none { it.isPending })
                    .put("isCurrent", period == current)
                    .put("pills", pills)
            )
        }

        return JSONObject()
            .put("mode", "period")
            .put("slots", slots)
            .toString()
    }
}

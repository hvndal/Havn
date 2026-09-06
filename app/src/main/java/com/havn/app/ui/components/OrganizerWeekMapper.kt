package com.havn.app.ui.components

import com.havn.app.domain.model.Medication
import com.havn.app.domain.model.RepeatType
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

object OrganizerWeekMapper {

    fun colorTagToHex(tag: String?): String = when (tag?.lowercase()) {
        "sage" -> "#8DA08C"
        "terracotta" -> "#FEB28F"
        "butter" -> "#E2C381"
        "slate" -> "#9BAEB5"
        "sand" -> "#BEB09A"
        "white" -> "#FBF9F5"
        "charcoal" -> "#333632"
        else -> "#8DA08C"
    }

    fun medsForDay(meds: List<Medication>, dayIndex: Int): List<Medication> =
        meds.filter { med ->
            med.isActive && when (med.repeatType) {
                RepeatType.DAILY -> true
                RepeatType.WEEKLY -> true
                RepeatType.AS_NEEDED -> false
            }
        }

    fun buildWeekDataJson(meds: List<Medication>): String {
        val todayDow = LocalDate.now().dayOfWeek.value - 1
        val arr = JSONArray()
        for (day in 0..6) {
            val dayMeds = medsForDay(meds, day)
            val colors = JSONArray()
            val items = JSONArray()

            dayMeds.take(4).forEach { med ->
                colors.put(colorTagToHex(med.colorTag))
                items.put(
                    JSONObject()
                        .put("shape", med.shape.name)
                        .put("primaryColor", colorTagToHex(med.colorTag))
                        .put("secondaryColor", med.secondaryColorTag?.let { colorTagToHex(it) } ?: JSONObject.NULL)
                        .put("scoreLine", med.scoreLine.name)
                )
            }

            arr.put(
                JSONObject()
                    .put("count", dayMeds.size)
                    .put("colors", colors)
                    .put("items", items)
                    .put("isToday", day == todayDow)
            )
        }
        return arr.toString()
    }
}

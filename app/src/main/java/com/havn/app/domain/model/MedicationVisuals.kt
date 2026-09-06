package com.havn.app.domain.model

import androidx.compose.ui.graphics.Color
import com.havn.app.ui.theme.ButterAmber
import com.havn.app.ui.theme.Sage
import com.havn.app.ui.theme.Terracotta

enum class MedShape {
    ROUND_TABLET,
    OVAL_TABLET,
    CAPSULE,
    SCORED_TABLET,
    SMALL_TABLET,
    LARGE_TABLET,
}

enum class MedSize {
    SMALL,
    MEDIUM,
    LARGE,
}

enum class MedScoreLine {
    NONE,
    SINGLE,
    CROSS,
}

enum class MedCoating {
    MATTE,
    SATIN,
    GLOSSY,
}

data class MedicationVisualSpec(
    val shape: MedShape = MedShape.CAPSULE,
    val primaryColorTag: String = "sage",
    val secondaryColorTag: String? = null,
    val size: MedSize = MedSize.MEDIUM,
    val scoreLine: MedScoreLine = MedScoreLine.NONE,
    val imprint: String = "",
    val coating: MedCoating = MedCoating.SATIN,
)

object MedColorPalette {
    val SageColor = Sage
    val TerracottaColor = Terracotta
    val ButterColor = ButterAmber
    val SlateColor = Color(0xFF9BAEB5)
    val SandColor = Color(0xFFBEB09A)
    val WhiteColor = Color(0xFFFBF9F5)
    val CharcoalColor = Color(0xFF333632)

    fun getColor(tag: String?): Color = when (tag?.lowercase()) {
        "sage" -> SageColor
        "terracotta" -> TerracottaColor
        "butter" -> ButterColor
        "slate" -> SlateColor
        "sand" -> SandColor
        "white" -> WhiteColor
        "charcoal" -> CharcoalColor
        else -> SageColor
    }

    val availableTags = listOf("sage", "terracotta", "butter", "slate", "sand", "white")
}

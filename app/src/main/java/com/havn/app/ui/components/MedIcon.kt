package com.havn.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.havn.app.R
import com.havn.app.domain.model.MedIconType
import com.havn.app.domain.model.MedShape
import com.havn.app.domain.model.MedicationVisualSpec
import com.havn.app.ui.theme.Sage

@Composable
fun MedIcon(
    type: MedIconType,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Sage,
    visualSpec: MedicationVisualSpec? = null,
) {
    if (visualSpec != null || type == MedIconType.CAPSULE || type == MedIconType.TABLET) {
        val spec = visualSpec ?: MedicationVisualSpec(
            shape = if (type == MedIconType.CAPSULE) MedShape.CAPSULE else MedShape.ROUND_TABLET
        )
        MedicationObject(
            spec = spec,
            modifier = modifier,
            size = size,
            elevationDp = 2.dp,
        )
    } else {
        val res = when (type) {
            MedIconType.CAPSULE -> R.drawable.ic_med_capsule
            MedIconType.TABLET -> R.drawable.ic_med_tablet
            MedIconType.LIQUID -> R.drawable.ic_med_liquid
            MedIconType.POWDER -> R.drawable.ic_med_powder
            MedIconType.INJECTION -> R.drawable.ic_med_injection
        }
        Icon(
            painter = painterResource(res),
            contentDescription = null,
            modifier = modifier.size(size),
            tint = tint,
        )
    }
}

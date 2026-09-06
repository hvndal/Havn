package com.havn.app.ui.screens.addmed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.havn.app.R
import com.havn.app.domain.model.*
import com.havn.app.ui.components.MedIcon
import com.havn.app.ui.components.MedicationObject
import com.havn.app.ui.theme.*

@Composable
fun AddMedicationScreen(
    onBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel(),
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("08:00") }
    var repeat by remember { mutableStateOf(RepeatType.DAILY) }
    var iconType by remember { mutableStateOf(MedIconType.CAPSULE) }
    var colorTag by remember { mutableStateOf("sage") }
    var secondaryColorTag by remember { mutableStateOf<String?>(null) }
    var shape by remember { mutableStateOf(MedShape.CAPSULE) }
    var size by remember { mutableStateOf(MedSize.MEDIUM) }
    var scoreLine by remember { mutableStateOf(MedScoreLine.NONE) }
    var imprint by remember { mutableStateOf("") }
    var coating by remember { mutableStateOf(MedCoating.SATIN) }
    var notes by remember { mutableStateOf("") }

    val repeatOptions = listOf(RepeatType.DAILY, RepeatType.WEEKLY, RepeatType.AS_NEEDED)
    val repeatLabels = listOf("Daily", "Weekly", "As needed")
    val colorTags = MedColorPalette.availableTags

    val visualSpec = MedicationVisualSpec(
        shape = shape,
        primaryColorTag = colorTag,
        secondaryColorTag = secondaryColorTag,
        size = size,
        scoreLine = scoreLine,
        imprint = imprint,
        coating = coating,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
    ) {
        // Top Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceLow)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Charcoal,
                    modifier = Modifier.size(18.dp),
                )
            }

            Text(
                text = "New Medication",
                style = MaterialTheme.typography.titleMedium,
                color = Charcoal,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.size(36.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Tactile 3D Pill Preview Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(SurfaceLow)
                            .border(1.dp, SurfaceHighest, RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        MedicationObject(
                            spec = visualSpec,
                            size = 72.dp,
                            elevationDp = 6.dp,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "TACTILE MEDICATION OBJECT",
                        style = MaterialTheme.typography.labelSmall,
                        color = StoneGrey,
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Shape Selector
            Text(
                text = "SHAPE & FORM",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGrey,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MedShape.values().forEach { s ->
                    val isSelected = s == shape
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Sage.copy(alpha = 0.15f) else SurfaceHigh)
                            .border(1.dp, if (isSelected) Sage else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                shape = s
                                if (s == MedShape.CAPSULE) {
                                    iconType = MedIconType.CAPSULE
                                } else {
                                    iconType = MedIconType.TABLET
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        MedicationObject(
                            spec = visualSpec.copy(shape = s, size = MedSize.SMALL),
                            size = 28.dp,
                            elevationDp = 1.dp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Primary & Secondary Color Pickers
            Text(
                text = "PRIMARY COLOUR",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGrey,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colorTags.forEach { tag ->
                    val c = MedColorPalette.getColor(tag)
                    Box(
                        modifier = Modifier
                            .size(if (tag == colorTag) 36.dp else 28.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(if (tag == colorTag) 2.dp else 0.dp, Charcoal.copy(alpha = 0.3f), CircleShape)
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { colorTag = tag },
                    )
                }
            }

            if (shape == MedShape.CAPSULE) {
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DUAL-TONE CAPSULE (SECONDARY)",
                        style = MaterialTheme.typography.labelSmall,
                        color = StoneGrey,
                        letterSpacing = 0.5.sp,
                    )
                    if (secondaryColorTag != null) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall,
                            color = Sage,
                            modifier = Modifier.clickable { secondaryColorTag = null }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colorTags.forEach { tag ->
                        val isSelected = tag == secondaryColorTag
                        val c = MedColorPalette.getColor(tag)
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 36.dp else 28.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(if (isSelected) 2.dp else 0.dp, Charcoal.copy(alpha = 0.3f), CircleShape)
                                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                    secondaryColorTag = if (secondaryColorTag == tag) null else tag
                                },
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Score Line & Coating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("SCORE LINE", style = MaterialTheme.typography.labelSmall, color = StoneGrey, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MedScoreLine.values().forEach { sl ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sl == scoreLine) Sage else SurfaceHigh)
                                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { scoreLine = sl }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = sl.name.take(4),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (sl == scoreLine) White else CharcoalMid
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("COATING", style = MaterialTheme.typography.labelSmall, color = StoneGrey, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MedCoating.values().forEach { ct ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (ct == coating) Sage else SurfaceHigh)
                                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { coating = ct }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = ct.name.take(4),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (ct == coating) White else CharcoalMid
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Form card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(White)
                    .border(1.dp, SurfaceHighest, RoundedCornerShape(16.dp)),
            ) {
                FormRow(label = "Name") {
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Charcoal),
                        cursorBrush = SolidColor(Sage),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (name.isEmpty()) Text("e.g. Vitamin D3", style = MaterialTheme.typography.bodyMedium.copy(color = StoneLight))
                            inner()
                        }
                    )
                }
                HorizontalDivider(color = SurfaceHighest, thickness = 0.5.dp)
                FormRow(label = "Dosage") {
                    BasicTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Charcoal),
                        cursorBrush = SolidColor(Sage),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (dosage.isEmpty()) Text("e.g. 1000 mg", style = MaterialTheme.typography.bodyMedium.copy(color = StoneLight))
                            inner()
                        }
                    )
                }
                HorizontalDivider(color = SurfaceHighest, thickness = 0.5.dp)
                FormRow(label = "Imprint") {
                    BasicTextField(
                        value = imprint,
                        onValueChange = { imprint = it.take(4) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Charcoal),
                        cursorBrush = SolidColor(Sage),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (imprint.isEmpty()) Text("e.g. H50 (Optional)", style = MaterialTheme.typography.bodyMedium.copy(color = StoneLight))
                            inner()
                        }
                    )
                }
                HorizontalDivider(color = SurfaceHighest, thickness = 0.5.dp)
                FormRow(label = "Time") {
                    BasicTextField(
                        value = time,
                        onValueChange = { time = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Charcoal),
                        cursorBrush = SolidColor(Sage),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                HorizontalDivider(color = SurfaceHighest, thickness = 0.5.dp)
                // Repeat selector
                FormRow(label = "Repeat") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeatOptions.forEachIndexed { i, r ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (r == repeat) Sage else SurfaceHigh)
                                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { repeat = r }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    text = repeatLabels[i],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (r == repeat) White else CharcoalMid,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Notes (optional)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(White)
                    .border(1.dp, SurfaceHighest, RoundedCornerShape(16.dp))
                    .padding(16.dp),
            ) {
                Text("Notes", style = MaterialTheme.typography.labelMedium, color = StoneGrey)
                Spacer(Modifier.height(6.dp))
                BasicTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Charcoal),
                    cursorBrush = SolidColor(Sage),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                    decorationBox = { inner ->
                        if (notes.isEmpty()) Text("Optional notes", style = MaterialTheme.typography.bodyMedium.copy(color = StoneLight))
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(32.dp))

            // Save button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.saveMedication(
                            name = name,
                            dosage = dosage,
                            time = time,
                            repeat = repeat,
                            iconType = iconType,
                            colorTag = colorTag,
                            notes = notes,
                            shape = shape,
                            secondaryColorTag = secondaryColorTag,
                            size = size,
                            scoreLine = scoreLine,
                            imprint = imprint,
                            coating = coating,
                            onComplete = { onBack() },
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Sage, contentColor = White,
                    disabledContainerColor = SurfaceHigh, disabledContentColor = StoneGrey,
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Text("Save Medication", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormRow(
    label: String,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Charcoal,
            modifier = Modifier.width(72.dp),
        )
        Spacer(Modifier.width(12.dp))
        content()
    }
}

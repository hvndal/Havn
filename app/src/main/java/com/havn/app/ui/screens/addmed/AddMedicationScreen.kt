package com.havn.app.ui.screens.addmed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import com.havn.app.domain.model.MedIconType
import com.havn.app.domain.model.RepeatType
import com.havn.app.ui.components.MedIcon
import com.havn.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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
    var notes by remember { mutableStateOf("") }

    val iconTypes = MedIconType.values().toList()
    val colorTags = listOf("sage", "terracotta", "butter", "slate", "sand")
    val repeatOptions = listOf(RepeatType.DAILY, RepeatType.WEEKLY, RepeatType.AS_NEEDED)
    val repeatLabels = listOf("Daily", "Weekly", "As needed")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .systemBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceHigh)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = CharcoalMid,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "Add Medication",
                style = MaterialTheme.typography.titleMedium,
                color = Charcoal,
            )
            Spacer(Modifier.weight(1f))
            // Confirm
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (name.isNotBlank()) Sage else SurfaceHigh)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        if (name.isNotBlank()) {
                            viewModel.saveMedication(
                                name, dosage, time, repeat, iconType, colorTag, notes
                            ) { onBack() }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "Save",
                    tint = if (name.isNotBlank()) White else StoneGrey,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Pill icon picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLow)
                            .border(1.dp, SurfaceHighest, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        MedIcon(type = iconType, size = 48.dp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "TAP TO CHANGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = StoneGrey,
                        letterSpacing = 0.1.sp,
                    )
                }
            }

            // Icon type row
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                iconTypes.forEach { t ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (t == iconType) Sage.copy(alpha = 0.15f) else SurfaceHigh)
                            .border(1.dp, if (t == iconType) Sage else Color.Transparent, CircleShape)
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { iconType = t },
                        contentAlignment = Alignment.Center,
                    ) {
                        MedIcon(
                            type = t,
                            size = 20.dp,
                            tint = if (t == iconType) Sage else StoneGrey,
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

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

            Spacer(Modifier.height(16.dp))

            // Color tags
            Text(
                text = "COLOUR",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGrey,
                letterSpacing = 0.1.sp,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colorTags.forEach { tag ->
                    val c = tagColor(tag)
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

            Spacer(Modifier.height(28.dp))

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
                        viewModel.saveMedication(name, dosage, time, repeat, iconType, colorTag, notes) { onBack() }
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
                Text("Save", style = MaterialTheme.typography.labelLarge)
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

private fun tagColor(tag: String) = when (tag) {
    "sage" -> Sage
    "terracotta" -> Terracotta
    "butter" -> ButterAmber
    "slate" -> Color(0xFF9BAEB5)
    "sand" -> Color(0xFFBEB09A)
    else -> Sage
}

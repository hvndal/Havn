package com.havn.app.ui.screens.addmed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import com.havn.app.ui.components.animatedFocusBorder
import com.havn.app.ui.components.pressScale
import com.havn.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    var isSaving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val iconTypes = MedIconType.values().toList()
    val colorTags = listOf("sage", "terracotta", "butter", "slate", "sand")
    val repeatOptions = listOf(RepeatType.DAILY, RepeatType.WEEKLY, RepeatType.AS_NEEDED)
    val repeatLabels = listOf("Daily", "Weekly", "As needed")

    val handleSave = {
        if (name.isNotBlank() && !isSaving) {
            isSaving = true
            coroutineScope.launch {
                viewModel.saveMedication(
                    name, dosage, time, repeat, iconType, colorTag, notes
                ) {
                    onBack()
                }
            }
        }
    }

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
            val backInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .pressScale(targetScale = 0.90f, interactionSource = backInteraction)
                    .clip(CircleShape)
                    .background(SurfaceHigh)
                    .clickable(indication = null, interactionSource = backInteraction) { onBack() },
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
            val checkInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .pressScale(targetScale = 0.90f, interactionSource = checkInteraction)
                    .clip(CircleShape)
                    .background(if (name.isNotBlank()) Sage else SurfaceHigh)
                    .clickable(
                        indication = null,
                        interactionSource = checkInteraction,
                    ) { handleSave() },
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
                    val isSelected = t == iconType
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "iconScale"
                    )
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) Sage.copy(alpha = 0.18f) else SurfaceHigh,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "iconBg"
                    )

                    val itemInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .pressScale(targetScale = 0.92f, interactionSource = itemInteraction)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(bg)
                            .border(1.dp, if (isSelected) Sage else Color.Transparent, CircleShape)
                            .clickable(indication = null, interactionSource = itemInteraction) { iconType = t },
                        contentAlignment = Alignment.Center,
                    ) {
                        MedIcon(
                            type = t,
                            size = 20.dp,
                            tint = if (isSelected) Sage else StoneGrey,
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
                val nameInteraction = remember { MutableInteractionSource() }
                FormRow(
                    label = "Name",
                    modifier = Modifier.animatedFocusBorder(nameInteraction, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Charcoal),
                        cursorBrush = SolidColor(Sage),
                        singleLine = true,
                        interactionSource = nameInteraction,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (name.isEmpty()) Text("e.g. Vitamin D3", style = MaterialTheme.typography.bodyMedium.copy(color = StoneLight))
                            inner()
                        }
                    )
                }
                HorizontalDivider(color = SurfaceHighest, thickness = 0.5.dp)
                val dosageInteraction = remember { MutableInteractionSource() }
                FormRow(
                    label = "Dosage",
                    modifier = Modifier.animatedFocusBorder(dosageInteraction, shape = RectangleShape)
                ) {
                    BasicTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Charcoal),
                        cursorBrush = SolidColor(Sage),
                        singleLine = true,
                        interactionSource = dosageInteraction,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (dosage.isEmpty()) Text("e.g. 1000 mg", style = MaterialTheme.typography.bodyMedium.copy(color = StoneLight))
                            inner()
                        }
                    )
                }
                HorizontalDivider(color = SurfaceHighest, thickness = 0.5.dp)
                val timeInteraction = remember { MutableInteractionSource() }
                FormRow(
                    label = "Time",
                    modifier = Modifier.animatedFocusBorder(timeInteraction, shape = RectangleShape)
                ) {
                    BasicTextField(
                        value = time,
                        onValueChange = { time = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Charcoal),
                        cursorBrush = SolidColor(Sage),
                        singleLine = true,
                        interactionSource = timeInteraction,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                HorizontalDivider(color = SurfaceHighest, thickness = 0.5.dp)
                // Repeat selector
                FormRow(label = "Repeat") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeatOptions.forEachIndexed { i, r ->
                            val isSelected = r == repeat
                            val bg by animateColorAsState(
                                targetValue = if (isSelected) Sage else SurfaceHigh,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label = "repeatBg"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) White else CharcoalMid,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label = "repeatText"
                            )

                            val repeatInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .pressScale(targetScale = 0.92f, interactionSource = repeatInteraction)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .clickable(indication = null, interactionSource = repeatInteraction) { repeat = r }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    text = repeatLabels[i],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor,
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
                    val isSelected = tag == colorTag
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "colorScale"
                    )

                    val colorInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .pressScale(targetScale = 0.90f, interactionSource = colorInteraction)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(if (isSelected) 2.dp else 0.dp, Charcoal.copy(alpha = 0.35f), CircleShape)
                            .clickable(indication = null, interactionSource = colorInteraction) { colorTag = tag },
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Notes (optional)
            val notesInteraction = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animatedFocusBorder(notesInteraction, shape = RoundedCornerShape(16.dp))
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
                    interactionSource = notesInteraction,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                    decorationBox = { inner ->
                        if (notes.isEmpty()) Text("Optional notes", style = MaterialTheme.typography.bodyMedium.copy(color = StoneLight))
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(32.dp))

            // Save button
            val btnInteraction = remember { MutableInteractionSource() }
            Button(
                onClick = handleSave,
                enabled = name.isNotBlank() && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .pressScale(targetScale = 0.95f, interactionSource = btnInteraction),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Sage, contentColor = White,
                    disabledContainerColor = SurfaceHigh, disabledContentColor = StoneGrey,
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                interactionSource = btnInteraction,
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
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

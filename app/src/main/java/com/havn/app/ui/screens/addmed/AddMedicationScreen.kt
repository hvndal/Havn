package com.havn.app.ui.screens.addmed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.R
import com.havn.app.domain.model.MedIconType
import com.havn.app.domain.model.RepeatType
import com.havn.app.ui.components.HavnButton
import com.havn.app.ui.components.HavnButtonSize
import com.havn.app.ui.components.HavnButtonTone
import com.havn.app.ui.components.HavnIconButton
import com.havn.app.ui.components.HavnSegmented
import com.havn.app.ui.components.HavnTextField
import com.havn.app.ui.components.HavnTimePickerDialog
import com.havn.app.ui.components.MedIcon
import com.havn.app.ui.components.havnPress
import com.havn.app.ui.screens.home.medAccent
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val COLOR_TAGS = listOf("sage", "clay", "amber", "slate", "sand")

/**
 * Add or edit a medication.
 *
 * The important change here is the time control. Times were previously a plain
 * text field the user typed "08:00" into. Anything that failed to parse was
 * accepted by the form and stored, and `ReminderScheduler` then dropped it
 * silently — so a mistyped time produced a medication that looked scheduled and
 * never once reminded anyone. Times are now picked, multiple times per day are
 * supported end to end, and an unscheduled medication says so plainly.
 */
@Composable
fun AddMedicationScreen(
    onBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HavnTheme.colors
    val gutter = HavnTheme.spacing.gutter

    var timePickerTarget by remember { mutableStateOf<Int?>(null) }
    var showAddTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = gutter)
                    .padding(top = HavnTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HavnIconButton(
                    icon = painterResource(R.drawable.ic_back),
                    contentDescription = "Back",
                    onClick = onBack,
                    size = 40.dp,
                    iconSize = 18.dp,
                )
            }

            Spacer(Modifier.height(HavnTheme.spacing.xl))

            Column(Modifier.padding(horizontal = gutter)) {
                Text(
                    text = if (uiState.isEditing) "EDITING" else "NEW",
                    style = HavnType.Eyebrow,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(HavnTheme.spacing.sm))
                Text(
                    text = if (uiState.isEditing) "Edit medication" else "Add a medication",
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.textPrimary,
                )
            }

            Spacer(Modifier.height(HavnTheme.spacing.section))

            // ── Form ─────────────────────────────────────────────────────────
            Column(Modifier.padding(horizontal = gutter)) {
                HavnTextField(
                    value = uiState.name,
                    onValueChange = viewModel::setName,
                    label = "Name",
                    placeholder = "e.g. Magnesium glycinate",
                    error = uiState.nameError,
                    imeAction = ImeAction.Next,
                )

                Spacer(Modifier.height(HavnTheme.spacing.xl))

                HavnTextField(
                    value = uiState.dosage,
                    onValueChange = viewModel::setDosage,
                    label = "Dosage",
                    placeholder = "e.g. 400 mg, 1 capsule",
                    imeAction = ImeAction.Next,
                )

                Spacer(Modifier.height(HavnTheme.spacing.section))

                // ── Form factor ──────────────────────────────────────────────
                Text(text = "FORM", style = HavnType.Eyebrow, color = colors.textTertiary)
                Spacer(Modifier.height(HavnTheme.spacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HavnTheme.spacing.md),
                ) {
                    MedIconType.entries.forEach { type ->
                        IconChoice(
                            type = type,
                            selected = type == uiState.iconType,
                            accent = medAccent(uiState.colorTag),
                            onClick = { viewModel.setIcon(type) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(Modifier.height(HavnTheme.spacing.section))

                // ── Colour ───────────────────────────────────────────────────
                Text(text = "COLOUR", style = HavnType.Eyebrow, color = colors.textTertiary)
                Spacer(Modifier.height(HavnTheme.spacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HavnTheme.spacing.md),
                ) {
                    COLOR_TAGS.forEach { tag ->
                        ColourChoice(
                            tag = tag,
                            selected = tag == uiState.colorTag,
                            onClick = { viewModel.setColor(tag) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(Modifier.height(HavnTheme.spacing.section))

                // ── Repeat ───────────────────────────────────────────────────
                Text(text = "REPEAT", style = HavnType.Eyebrow, color = colors.textTertiary)
                Spacer(Modifier.height(HavnTheme.spacing.lg))
                HavnSegmented(
                    options = listOf("Daily", "Weekly", "As needed"),
                    selectedIndex = when (uiState.repeat) {
                        RepeatType.DAILY -> 0
                        RepeatType.WEEKLY -> 1
                        RepeatType.AS_NEEDED -> 2
                    },
                    onSelect = { index ->
                        viewModel.setRepeat(
                            when (index) {
                                1 -> RepeatType.WEEKLY
                                2 -> RepeatType.AS_NEEDED
                                else -> RepeatType.DAILY
                            }
                        )
                    },
                )

                Spacer(Modifier.height(HavnTheme.spacing.section))

                // ── Times ────────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = uiState.repeat != RepeatType.AS_NEEDED,
                    enter = fadeIn(HavnMotion.standard()),
                    exit = fadeOut(HavnMotion.exit()),
                ) {
                    Column {
                        Text(
                            text = "REMINDER TIMES",
                            style = HavnType.Eyebrow,
                            color = colors.textTertiary,
                        )
                        Spacer(Modifier.height(HavnTheme.spacing.sm))
                        Text(
                            text = if (uiState.times.isEmpty()) {
                                "No times set — this medication won't remind you."
                            } else {
                                "Hävn will remind you at each of these."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.times.isEmpty()) colors.warning
                            else colors.textTertiary,
                        )
                        Spacer(Modifier.height(HavnTheme.spacing.lg))

                        uiState.times.forEachIndexed { index, time ->
                            TimeRow(
                                time = time,
                                canRemove = uiState.times.size > 1,
                                onEdit = { timePickerTarget = index },
                                onRemove = { viewModel.removeTime(index) },
                            )
                        }

                        Spacer(Modifier.height(HavnTheme.spacing.md))
                        HavnButton(
                            text = "Add a time",
                            onClick = { showAddTimePicker = true },
                            tone = HavnButtonTone.Secondary,
                            size = HavnButtonSize.Medium,
                        )
                    }
                }

                Spacer(Modifier.height(HavnTheme.spacing.section))

                // ── Notes ────────────────────────────────────────────────────
                HavnTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::setNotes,
                    label = "Instructions",
                    placeholder = "e.g. take with food",
                    singleLine = false,
                    minHeight = 90.dp,
                    imeAction = ImeAction.Default,
                )

                Spacer(Modifier.height(HavnTheme.spacing.section))

                HavnButton(
                    text = if (uiState.isEditing) "Save changes" else "Add medication",
                    onClick = viewModel::save,
                    enabled = uiState.canSave,
                    loading = uiState.isSaving,
                )

                Spacer(Modifier.height(HavnTheme.spacing.huge))
            }
        }
    }

    // Editing an existing time
    timePickerTarget?.let { index ->
        val current = uiState.times.getOrNull(index)
        HavnTimePickerDialog(
            initial = runCatching { LocalTime.parse(current) }
                .getOrDefault(LocalTime.of(8, 0)),
            title = "Reminder time",
            onDismiss = { timePickerTarget = null },
            onConfirm = { time ->
                viewModel.replaceTime(index, time.format(DateTimeFormatter.ofPattern("HH:mm")))
                timePickerTarget = null
            },
        )
    }

    if (showAddTimePicker) {
        HavnTimePickerDialog(
            initial = LocalTime.of(20, 0),
            title = "Add a reminder time",
            onDismiss = { showAddTimePicker = false },
            onConfirm = { time ->
                viewModel.addTime(time.format(DateTimeFormatter.ofPattern("HH:mm")))
                showAddTimePicker = false
            },
        )
    }
}

@Composable
private fun TimeRow(
    time: String,
    canRemove: Boolean,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    val colors = HavnTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = HavnTheme.spacing.sm)
            .clip(RoundedCornerShape(HavnTheme.radius.md))
            .background(colors.surfaceSunken)
            .havnPress(scaleDown = 0.99f, onClickLabel = "Change time", onClick = onEdit)
            .padding(horizontal = HavnTheme.spacing.lg, vertical = HavnTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatClock(time),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (canRemove) {
            Text(
                text = "Remove",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textTertiary,
                modifier = Modifier
                    .clip(RoundedCornerShape(HavnTheme.radius.sm))
                    .havnPress(scaleDown = 0.95f, onClick = onRemove)
                    .padding(horizontal = HavnTheme.spacing.sm, vertical = HavnTheme.spacing.xs),
            )
        }
    }
}

@Composable
private fun IconChoice(
    type: MedIconType,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val background by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = if (colors.isDark) 0.22f else 0.14f)
        else colors.surfaceSunken,
        animationSpec = HavnMotion.standard(),
        label = "iconChoiceBg",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 1.5.dp else 0.dp,
        animationSpec = HavnMotion.settle(),
        label = "iconChoiceBorder",
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(HavnTheme.radius.md))
            .background(background)
            .border(borderWidth, if (selected) accent else Color.Transparent, RoundedCornerShape(HavnTheme.radius.md))
            .havnPress(
                scaleDown = 0.92f,
                onClickLabel = type.name.lowercase(),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        MedIcon(
            type = type,
            size = 24.dp,
            tint = if (selected) accent else colors.textTertiary,
        )
    }
}

@Composable
private fun ColourChoice(
    tag: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val tint = medAccent(tag)
    val ring by animateDpAsState(
        targetValue = if (selected) 3.dp else 0.dp,
        animationSpec = HavnMotion.settle(),
        label = "colourRing",
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .havnPress(scaleDown = 0.9f, onClickLabel = tag, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(tint)
                .border(ring, colors.canvas, CircleShape)
                .border(
                    width = if (selected) 1.5.dp else 0.dp,
                    color = if (selected) colors.textPrimary else Color.Transparent,
                    shape = CircleShape,
                )
        )
    }
}

private fun formatClock(raw: String): String = runCatching {
    LocalTime.parse(raw)
        .format(DateTimeFormatter.ofPattern("h:mm a"))
        .replace("AM", "am").replace("PM", "pm")
}.getOrDefault(raw)

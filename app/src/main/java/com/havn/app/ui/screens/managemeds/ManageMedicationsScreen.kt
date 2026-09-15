package com.havn.app.ui.screens.managemeds

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.R
import com.havn.app.domain.model.Medication
import com.havn.app.ui.components.HavnBreathingMark
import com.havn.app.ui.components.HavnButton
import com.havn.app.ui.components.HavnButtonSize
import com.havn.app.ui.components.HavnButtonTone
import com.havn.app.ui.components.HavnConfirmDialog
import com.havn.app.ui.components.HavnDoseRowSkeleton
import com.havn.app.ui.components.HavnEmptyState
import com.havn.app.ui.components.HavnIconButton
import com.havn.app.ui.components.HavnSwitch
import com.havn.app.ui.components.HavnTextField
import com.havn.app.ui.components.MedIcon
import com.havn.app.ui.components.havnPress
import com.havn.app.ui.components.havnReveal
import com.havn.app.ui.components.rememberRevealProgress
import com.havn.app.ui.screens.home.medAccent
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * The medication shelf.
 *
 * A list rather than a grid of cards, with an inline pause switch so the most
 * common maintenance action — "stop reminding me about this for a while" —
 * takes one tap and does not require opening the medication.
 */
@Composable
fun ManageMedicationsScreen(
    onBack: () -> Unit,
    onAddMedication: () -> Unit,
    onEditMedication: (Long) -> Unit,
    viewModel: ManageMedicationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HavnTheme.colors
    val gutter = HavnTheme.spacing.gutter

    var pendingDelete by remember { mutableStateOf<Medication?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = HavnTheme.spacing.huge * 2),
        ) {
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
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
                        text = "${uiState.medications.size} SAVED",
                        style = HavnType.Eyebrow,
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.sm))
                    Text(
                        text = "Medications",
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.textPrimary,
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.xl))
            }

            if (uiState.hasAny) {
                item(key = "search") {
                    HavnTextField(
                        value = uiState.query,
                        onValueChange = viewModel::setQuery,
                        placeholder = "Search",
                        minHeight = 48.dp,
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.lg))
                }

                item(key = "filters") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = gutter),
                        horizontalArrangement = Arrangement.spacedBy(HavnTheme.spacing.sm),
                    ) {
                        items(MedFilter.entries.toList()) { filter ->
                            FilterChip(
                                label = filter.label,
                                selected = filter == uiState.filter,
                                onClick = { viewModel.setFilter(filter) },
                            )
                        }
                    }
                    Spacer(Modifier.height(HavnTheme.spacing.xl))
                }
            }

            when {
                uiState.isLoading -> {
                    items(4) {
                        HavnDoseRowSkeleton(modifier = Modifier.padding(horizontal = gutter))
                    }
                }

                !uiState.hasAny -> {
                    item(key = "empty-all") {
                        HavnEmptyState(
                            eyebrow = "Nothing saved",
                            headline = "Your shelf is empty",
                            body = "Add the things you take. Hävn keeps the schedule and " +
                                "the record; you just tap when you've taken one.",
                            illustration = { HavnBreathingMark() },
                            action = {
                                HavnButton(
                                    text = "Add a medication",
                                    onClick = onAddMedication,
                                    size = HavnButtonSize.Medium,
                                    fillWidth = false,
                                )
                            },
                            modifier = Modifier.padding(horizontal = gutter),
                        )
                    }
                }

                uiState.visible.isEmpty() -> {
                    item(key = "empty-filtered") {
                        HavnEmptyState(
                            headline = "Nothing matches",
                            body = if (uiState.query.isNotBlank()) {
                                "No medication called \"${uiState.query}\"."
                            } else {
                                "No medications in this filter."
                            },
                            modifier = Modifier.padding(horizontal = gutter),
                        )
                    }
                }

                else -> {
                    itemsIndexed(uiState.visible, key = { _, med -> med.id }) { index, med ->
                        MedicationRow(
                            medication = med,
                            index = index,
                            onClick = { onEditMedication(med.id) },
                            onToggleActive = { viewModel.toggleActive(med) },
                            onDelete = { pendingDelete = med },
                            modifier = Modifier.padding(horizontal = gutter),
                        )
                    }
                }
            }
        }

        if (uiState.hasAny) {
            HavnButton(
                text = "Add a medication",
                onClick = onAddMedication,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = gutter, vertical = HavnTheme.spacing.xl),
            )
        }
    }

    pendingDelete?.let { med ->
        HavnConfirmDialog(
            title = "Delete ${med.name}?",
            body = "Its reminders stop immediately. Doses already recorded stay in " +
                "your history.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                viewModel.delete(med)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = HavnTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(HavnTheme.radius.pill))
            .background(if (selected) colors.accent else colors.surfaceSunken)
            .havnPress(scaleDown = 0.94f, onClick = onClick)
            .padding(horizontal = HavnTheme.spacing.lg, vertical = HavnTheme.spacing.sm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.onAccent else colors.textSecondary,
        )
    }
}

@Composable
private fun MedicationRow(
    medication: Medication,
    index: Int,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val accent = medAccent(medication.colorTag)
    val reveal = rememberRevealProgress(
        key = medication.id,
        delayMs = HavnMotion.staggerDelay(index),
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (medication.isActive) 1f else 0.45f,
        animationSpec = HavnMotion.standard(),
        label = "medAlpha",
    )

    Column(modifier = modifier.havnReveal(reveal)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .havnPress(scaleDown = 0.99f, onClickLabel = "Edit ${medication.name}", onClick = onClick)
                .padding(vertical = HavnTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .graphicsLayer { alpha = contentAlpha }
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (colors.isDark) 0.18f else 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                MedIcon(type = medication.iconType, size = 21.dp, tint = accent)
            }

            Spacer(Modifier.width(HavnTheme.spacing.lg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary.copy(alpha = contentAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = scheduleSummary(medication),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary.copy(alpha = contentAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.width(HavnTheme.spacing.md))

            HavnSwitch(
                checked = medication.isActive,
                onCheckedChange = { onToggleActive() },
                contentDescription = if (medication.isActive) {
                    "Pause ${medication.name}"
                } else {
                    "Resume ${medication.name}"
                },
            )
        }

        Row(
            modifier = Modifier.padding(bottom = HavnTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(HavnTheme.spacing.md),
        ) {
            HavnButton(
                text = "Delete",
                onClick = onDelete,
                tone = HavnButtonTone.Ghost,
                size = HavnButtonSize.Small,
                fillWidth = false,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.hairline)
        )
    }
}

private fun scheduleSummary(med: Medication): String {
    if (!med.isActive) return "Paused"
    if (med.reminderTimes.isEmpty()) return "No reminder set"
    val times = med.reminderTimes.joinToString(" · ") { raw ->
        runCatching {
            LocalTime.parse(raw)
                .format(DateTimeFormatter.ofPattern("h:mm a"))
                .replace("AM", "am").replace("PM", "pm")
        }.getOrDefault(raw)
    }
    return if (med.dosage.isBlank()) times else "$times  ·  ${med.dosage}"
}

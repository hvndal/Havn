package com.havn.app.ui.screens.organizer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.domain.model.DayPeriod
import com.havn.app.domain.model.TodayDose
import com.havn.app.ui.components.HavnAmbientField
import com.havn.app.ui.components.HavnBreathingMark
import com.havn.app.ui.components.HavnDoseCheck
import com.havn.app.ui.components.HavnEmptyState
import com.havn.app.ui.components.HavnOrganizerView
import com.havn.app.ui.components.MedIcon
import com.havn.app.ui.components.havnPress
import com.havn.app.ui.components.havnReveal
import com.havn.app.ui.components.rememberRevealProgress
import com.havn.app.ui.screens.home.medAccent
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType

/**
 * The organizer, at full size.
 *
 * The 3D object is the screen — it runs edge to edge with the compartment
 * selector beneath it and the selected compartment's doses below that.
 *
 * Structurally corrected: this screen used to show seven weekday chips above a
 * 3D model that only ever had four compartments, because the view model fed it
 * the four-period payload. Selecting a day past Thursday sent an out-of-range
 * index the model ignored, and the list below filtered by a `medsForDay` helper
 * that returned the same medications for every weekday anyway. Both halves now
 * speak in day periods, which is what the physical object actually divides.
 */
@Composable
fun OrganizerScreen(
    contentPadding: PaddingValues,
    viewModel: OrganizerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HavnTheme.colors
    val gutter = HavnTheme.spacing.gutter

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        HavnAmbientField(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.9f),
            intensity = 0.75f,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = contentPadding.calculateBottomPadding() + HavnTheme.spacing.xxl,
            ),
        ) {
            item(key = "header") {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = gutter)
                        .padding(top = HavnTheme.spacing.xl),
                ) {
                    Text(
                        text = "MODEL H.01",
                        style = HavnType.Eyebrow,
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.sm))
                    Text(
                        text = "Organizer",
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.textPrimary,
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.xl))
            }

            if (!uiState.hasMedications && !uiState.isLoading) {
                item(key = "empty") {
                    HavnEmptyState(
                        eyebrow = "Empty compartments",
                        headline = "Nothing to organize yet",
                        body = "Add a medication with a time and it will appear in the " +
                            "compartment for that part of the day.",
                        illustration = { HavnBreathingMark() },
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                }
                return@LazyColumn
            }

            item(key = "model") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.15f)
                ) {
                    HavnOrganizerView(
                        dataJson = uiState.payloadJson,
                        selectedSlot = uiState.selectedPeriod.ordinal,
                        onSlotTapped = viewModel::selectSlotIndex,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.xl))
            }

            item(key = "periods") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = gutter),
                    horizontalArrangement = Arrangement.spacedBy(HavnTheme.spacing.sm),
                ) {
                    DayPeriod.entries.forEach { period ->
                        PeriodChip(
                            period = period,
                            count = uiState.count(period),
                            selected = period == uiState.selectedPeriod,
                            isNow = period == DayPeriod.current(),
                            onClick = { viewModel.selectPeriod(period) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(HavnTheme.spacing.section))
            }

            item(key = "period-header") {
                Column(Modifier.padding(horizontal = gutter)) {
                    Text(
                        text = uiState.selectedPeriod.range,
                        style = HavnType.Eyebrow,
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.xs))
                    Text(
                        text = uiState.selectedPeriod.label,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary,
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.lg))
            }

            val doses = uiState.selectedDoses
            if (doses.isEmpty()) {
                item(key = "period-empty") {
                    Text(
                        text = "Nothing in this compartment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                }
            } else {
                itemsIndexed(doses, key = { _, d -> "${d.medication.id}-${d.slot}" }) { index, dose ->
                    OrganizerDoseRow(
                        dose = dose,
                        index = index,
                        onToggle = { viewModel.toggleDose(dose) },
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodChip(
    period: DayPeriod,
    count: Int,
    selected: Boolean,
    isNow: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val background by animateColorAsState(
        targetValue = if (selected) colors.accent else colors.surfaceSunken,
        animationSpec = HavnMotion.standard(),
        label = "chipBg",
    )
    val content by animateColorAsState(
        targetValue = when {
            selected -> colors.onAccent
            isNow -> colors.accent
            else -> colors.textTertiary
        },
        animationSpec = HavnMotion.standard(),
        label = "chipContent",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(HavnTheme.radius.md))
            .background(background)
            .havnPress(scaleDown = 0.94f, onClickLabel = period.label, onClick = onClick)
            .padding(vertical = HavnTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = period.label.take(3).uppercase(),
            style = HavnType.EyebrowQuiet,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(HavnTheme.spacing.xs))
        Text(
            text = if (count == 0) "—" else "$count",
            style = MaterialTheme.typography.titleMedium,
            color = content,
        )
    }
}

@Composable
private fun OrganizerDoseRow(
    dose: TodayDose,
    index: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val accent = medAccent(dose.medication.colorTag)
    val reveal = rememberRevealProgress(
        key = "${dose.medication.id}-${dose.slot}",
        delayMs = HavnMotion.staggerDelay(index),
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (dose.isPending) 1f else 0.5f,
        animationSpec = HavnMotion.standard(),
        label = "organizerDoseAlpha",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .havnReveal(reveal)
            .padding(vertical = HavnTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer { alpha = contentAlpha }
                .clip(CircleShape)
                .background(accent.copy(alpha = if (colors.isDark) 0.18f else 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            MedIcon(type = dose.medication.iconType, size = 20.dp, tint = accent)
        }
        Spacer(Modifier.width(HavnTheme.spacing.lg))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dose.medication.name,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    append(dose.displayTime())
                    if (dose.medication.dosage.isNotBlank()) {
                        append("  ·  ").append(dose.medication.dosage)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HavnDoseCheck(
            status = dose.status,
            onToggle = onToggle,
            accent = accent,
            contentDescription = dose.medication.name,
        )
    }
}

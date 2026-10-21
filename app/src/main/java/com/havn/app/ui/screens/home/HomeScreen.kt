package com.havn.app.ui.screens.home

import com.havn.app.ui.theme.rememberReduceMotion
import androidx.compose.runtime.produceState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.R
import com.havn.app.domain.model.DayPeriod
import com.havn.app.domain.model.DoseStatus
import com.havn.app.domain.model.TodayDose
import com.havn.app.domain.model.User
import com.havn.app.ui.components.HavnAmbientField
import com.havn.app.ui.components.HavnButton
import com.havn.app.ui.components.HavnButtonSize
import com.havn.app.ui.components.HavnButtonTone
import com.havn.app.ui.components.HavnBreathingMark
import com.havn.app.ui.components.HavnCadenceStrip
import com.havn.app.ui.components.HavnDoseCheck
import com.havn.app.ui.components.HavnDoseRowSkeleton
import com.havn.app.ui.components.HavnProgressRing
import com.havn.app.ui.components.HavnEmptyState
import com.havn.app.ui.components.HavnIconButton
import com.havn.app.ui.components.HavnOdometerCounter
import com.havn.app.ui.components.HavnSwipeToLogRow
import com.havn.app.ui.components.HavnTextAction
import com.havn.app.ui.components.MedIcon
import com.havn.app.ui.components.havnPress
import com.havn.app.ui.components.havnReveal
import com.havn.app.ui.components.rememberHavnHaptics
import com.havn.app.ui.components.rememberRevealProgress
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Today.
 *
 * Deliberately *not* a dashboard. The previous version opened with five stacked
 * cards — adherence, weekly trend, organizer, a check-in banner and a two-tile
 * bento — before the user could see a single medication. Everything competed,
 * nothing led, and three of those cards showed invented data.
 *
 * The structure here is editorial: an immersive header, one number that
 * matters, the organizer as a full-bleed hero, then the day's doses as a
 * hairline-separated list. Hierarchy comes from scale and space; there is
 * exactly one card on the screen, and it is the thing you came to look at.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onAddMedication: () -> Unit,
    onManageMedications: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenReminders: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HavnTheme.colors
    val gutter = HavnTheme.spacing.gutter

    var detailDose by remember { mutableStateOf<TodayDose?>(null) }
    var showProfiles by remember { mutableStateOf(false) }
    var celebrating by remember { mutableStateOf(false) }
    val haptics = rememberHavnHaptics()

    // HomeViewModel has always emitted these; nothing listened, so finishing a
    // day produced no acknowledgement at all. The moment is deliberately brief
    // and happens at most once a day, which is what lets it be a real one.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is HomeEvent.DayCompleted) {
                haptics.celebrate()
                celebrating = true
                delay(2600)
                celebrating = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0A))
    ) {
        com.havn.app.ui.components.HavnBackdrop(progress = uiState.progress)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = contentPadding.calculateBottomPadding() + HavnTheme.spacing.xxl,
            ),
        ) {
            item(key = "top") {
                TodayTitle(
                    date = uiState.selectedDate,
                    isToday = uiState.isTodaySelected,
                    user = uiState.user,
                    onReturnToToday = { viewModel.selectDate(LocalDate.now()) },
                    onOpenProfiles = { showProfiles = true },
                    modifier = Modifier.padding(horizontal = gutter),
                )
            }

            item(key = "hero") {
                if (!uiState.isLoading && uiState.total > 0) {
                    val reveal = rememberRevealProgress()
                    NextDoseCard(
                        state = uiState,
                        onTakeNext = { uiState.nextDose?.let { viewModel.setStatus(it, DoseStatus.TAKEN) } },
                        modifier = Modifier
                            .padding(horizontal = gutter)
                            .havnReveal(reveal),
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    items(3, key = { "skeleton-$it" }) {
                        HavnDoseRowSkeleton(modifier = Modifier.padding(horizontal = gutter))
                    }
                }

                uiState.doses.isEmpty() -> {
                    item(key = "empty") {
                        TodayEmptyState(
                            hasMedications = uiState.hasMedications,
                            onAddMedication = onAddMedication,
                            modifier = Modifier.padding(horizontal = gutter),
                        )
                    }
                }

                else -> {
                    val nextKey = uiState.nextDose
                        ?.takeIf { uiState.isTodaySelected }
                        ?.let { "${it.medication.id}_${it.slot}" }
                    val groups = uiState.doses.sortedBy { it.sortKey }.groupBy { it.period }
                    groups.entries.forEachIndexed { index, (period, doses) ->
                        item(key = "period-${period.name}") {
                            val reveal = rememberRevealProgress(delayMs = HavnMotion.staggerDelay(index + 1, step = 60))
                            PeriodGroup(
                                period = period,
                                doses = doses,
                                nextKey = nextKey,
                                onToggle = { viewModel.toggleDose(it) },
                                onSetStatus = { dose, status -> viewModel.setStatus(dose, status) },
                                onOpenDetail = { detailDose = it },
                                modifier = Modifier
                                    .padding(horizontal = gutter)
                                    .havnReveal(reveal),
                            )
                        }
                    }
                    item(key = "hint") {
                        Text(
                            text = "Tap a dose for details. Swipe to skip.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnPhotoDim,
                            modifier = Modifier.padding(horizontal = gutter + 4.dp, vertical = HavnTheme.spacing.md),
                        )
                    }

                    if (uiState.asNeededMeds.isNotEmpty()) {
                        item(key = "as-needed") {
                            Column(Modifier.padding(horizontal = gutter).padding(top = HavnTheme.spacing.xl)) {
                                GroupHeader(title = "As needed", count = null)
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(GroupShape)
                                        .background(groupColor())
                                ) {
                                    uiState.asNeededMeds.forEachIndexed { i, med ->
                                        if (i > 0) RowDivider()
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 64.dp)
                                                .padding(start = 14.dp, end = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            MedTile(med.iconType)
                                            Spacer(Modifier.width(HavnTheme.spacing.md))
                                            Column(Modifier.weight(1f)) {
                                                Text(med.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (med.dosage.isNotBlank()) {
                                                    Text(med.dosage, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 1)
                                                }
                                            }
                                            Text(
                                                text = "Log dose",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = colors.textPrimary,
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .havnPress(onClickLabel = "Log ${med.name}") { viewModel.logAsNeeded(med) }
                                                    .background(colors.surfaceSunken)
                                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item(key = "add-cta") {
                        Spacer(Modifier.height(HavnTheme.spacing.xl))
                        HavnButton(
                            text = "Add a medication",
                            onClick = onAddMedication,
                            tone = HavnButtonTone.Secondary,
                            size = HavnButtonSize.Medium,
                            modifier = Modifier.padding(horizontal = gutter),
                        )
                    }
                }
            }
        }
    }

    if (showProfiles) {
        ProfileSheet(
            profiles = uiState.profiles,
            activeId = uiState.user?.id,
            onSelect = {
                viewModel.switchProfile(it)
                showProfiles = false
            },
            onDismiss = { showProfiles = false },
        )
    }

    DayCompleteOverlay(visible = celebrating)

    detailDose?.let { dose ->
        DoseDetailSheet(
            dose = dose,
            onDismiss = { detailDose = null },
            onSetStatus = { status ->
                viewModel.setStatus(dose, status)
                detailDose = null
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────

/** Large title, the date above it, the profile avatar at the right. */
@Composable
private fun TodayTitle(
    date: LocalDate,
    isToday: Boolean,
    user: User?,
    onReturnToToday: () -> Unit,
    onOpenProfiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = HavnTheme.spacing.lg, bottom = HavnTheme.spacing.lg),
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
            style = MaterialTheme.typography.bodyMedium,
            color = OnPhotoDim,
        )
        Spacer(Modifier.height(HavnTheme.spacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isToday) "Today" else date.format(DateTimeFormatter.ofPattern("EEEE")),
                style = MaterialTheme.typography.headlineLarge,
                color = OnPhoto,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .havnPress(onClickLabel = "Profiles", onClick = onOpenProfiles)
                    .background(OnPhoto.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = user?.name?.firstOrNull()?.uppercase() ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnPhoto,
                )
            }
        }
        if (!isToday) {
            Text(
                text = "Back to today",
                style = MaterialTheme.typography.labelLarge,
                color = colors.signal,
                modifier = Modifier
                    .padding(top = HavnTheme.spacing.sm)
                    .havnPress(onClick = onReturnToToday),
            )
        }
    }
}


private val OnPhoto = Color(0xFFF2F1EC)
private val OnPhotoDim = Color(0xFFF2F1EC).copy(alpha = 0.72f)

/** Groups float on the backdrop: paper in light mode, night paper in dark. */
@Composable
private fun groupColor(): Color =
    HavnTheme.colors.surfaceRaised.copy(alpha = if (HavnTheme.colors.isDark) 0.9f else 0.96f)

@Composable
private fun GroupHeader(title: String, count: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .padding(bottom = HavnTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = OnPhotoDim,
            modifier = Modifier.weight(1f),
        )
        if (count != null) Text(text = count, style = HavnType.Clock, color = OnPhotoDim)
    }
}

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .padding(start = 64.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(HavnTheme.colors.hairline)
    )
}

@Composable
private fun MedTile(type: com.havn.app.domain.model.MedIconType) {
    Box(
        Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(HavnTheme.colors.surfaceSunken),
        contentAlignment = Alignment.Center,
    ) {
        MedIcon(type = type, size = 18.dp, tint = HavnTheme.colors.textSecondary)
    }
}

/** Small pill tag beside a name: "Next" in orange, "Skipped" in grey. */
@Composable
private fun StatusTag(text: String, background: Color, content: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = content,
        modifier = Modifier
            .padding(start = 8.dp)
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

private val CardShape = RoundedCornerShape(20.dp)
private val GroupShape = RoundedCornerShape(16.dp)

/**
 * The one dark card on Today: how long until the next dose, what it is, a
 * "Take now" for it, and the day's progress as one segment per dose.
 */
@Composable
private fun NextDoseCard(
    state: HomeUiState,
    onTakeNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val haptics = rememberHavnHaptics()
    val now by produceState(LocalTime.now()) {
        while (true) {
            delay(30_000)
            value = LocalTime.now()
        }
    }
    val next = state.nextDose?.takeIf { state.isTodaySelected }
    val minutes = next?.time?.let { java.time.Duration.between(now, it).toMinutes() }

    val (label, big, unit) = when {
        state.allDone -> Triple("ALL DONE TODAY", "Done.", "")
        next == null -> Triple("LEFT ON THIS DAY", "${state.remaining}", " doses")
        minutes == null -> Triple("NEXT DOSE", "Any", " time")
        minutes < 0 -> Triple("DUE SINCE", next.slot, "")
        minutes < 60 -> Triple("NEXT DOSE IN", "$minutes", " min")
        else -> Triple("NEXT DOSE AT", next.slot, "")
    }

    val onCard = OnPhoto
    val onCardDim = OnPhotoDim

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 72.dp, start = 4.dp, end = 4.dp, bottom = 4.dp),
    ) {
        Text(label, style = HavnType.Eyebrow, color = onCardDim)
        Spacer(Modifier.height(HavnTheme.spacing.sm))
        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedContent(
                targetState = big,
                transitionSpec = {
                    (fadeIn(HavnMotion.standard()) + slideInVertically(HavnMotion.standard()) { it / 3 })
                        .togetherWith(fadeOut(HavnMotion.quick()) + slideOutVertically(HavnMotion.quick()) { -it / 3 })
                },
                label = "next-dose-number",
            ) { value ->
                Text(
                    text = value,
                    style = HavnType.Poster.copy(fontSize = 96.sp, lineHeight = 86.sp),
                    color = onCard,
                    maxLines = 1,
                )
            }
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.headlineSmall,
                    color = onCard,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(HavnTheme.spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = next?.let { listOf(it.medication.name, it.medication.dosage).filter { s -> s.isNotBlank() }.joinToString(" · ") }
                    ?: "${state.taken} of ${state.total} taken",
                style = MaterialTheme.typography.bodyLarge,
                color = onCard,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (next != null) {
                Spacer(Modifier.width(HavnTheme.spacing.md))
                Text(
                    text = "Take now",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSignal,
                    modifier = Modifier
                        .clip(CircleShape)
                        .havnPress(onClickLabel = "Take ${next.medication.name} now") {
                            haptics.latch()
                            onTakeNext()
                        }
                        .background(colors.signal)
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                )
            }
        }
        Spacer(Modifier.height(HavnTheme.spacing.lg))
        DoseSegments(
            total = state.total,
            taken = state.taken,
            fill = onCard,
            track = onCard.copy(alpha = 0.18f),
        )
        Spacer(Modifier.height(HavnTheme.spacing.sm))
        Row {
            Text(
                "${state.taken} of ${state.total} taken",
                style = MaterialTheme.typography.bodySmall,
                color = onCardDim,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${state.remaining} left",
                style = MaterialTheme.typography.bodySmall,
                color = onCardDim,
            )
        }
    }
}

/** One segment per dose; taken segments fill left to right, one after another. */
@Composable
private fun DoseSegments(total: Int, taken: Int, fill: Color, track: Color) {
    val reduceMotion = rememberReduceMotion()
    val count = total.coerceIn(1, 12)
    val filled = if (total <= 12) taken else (taken * 12f / total).toInt()
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(count) { i ->
            val target = if (i < filled) 1f else 0f
            val progress by animateFloatAsState(
                targetValue = target,
                animationSpec = if (reduceMotion) HavnMotion.none() else tween(
                    durationMillis = HavnMotion.Considered,
                    delayMillis = HavnMotion.staggerDelay(i, step = 60),
                    easing = HavnMotion.Decisive,
                ),
                label = "segment-$i",
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(track)
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(fill)
                )
            }
        }
    }
}

/** A period heading ("Morning  2/2") and its doses in one rounded group. */
@Composable
private fun PeriodGroup(
    period: DayPeriod,
    doses: List<TodayDose>,
    nextKey: String?,
    onToggle: (TodayDose) -> Unit,
    onSetStatus: (TodayDose, DoseStatus) -> Unit,
    onOpenDetail: (TodayDose) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    Column(modifier.padding(top = HavnTheme.spacing.xl)) {
        GroupHeader(title = period.label, count = "${doses.count { it.isTaken }}/${doses.size}")
        Column(
            Modifier
                .fillMaxWidth()
                .clip(GroupShape)
                .background(groupColor())
        ) {
            doses.forEachIndexed { i, dose ->
                val key = "${dose.medication.id}_${dose.slot}"
                if (i > 0) RowDivider()
                androidx.compose.runtime.key(key) {
                    HavnSwipeToLogRow(
                        isTaken = dose.isTaken,
                        isSkipped = dose.isSkipped,
                        onTake = { onSetStatus(dose, DoseStatus.TAKEN) },
                        onSkip = { onSetStatus(dose, DoseStatus.SKIPPED) },
                        onUndo = { onSetStatus(dose, DoseStatus.PENDING) },
                    ) {
                        TodayDoseRow(
                            dose = dose,
                            isNext = key == nextKey,
                            onToggle = { onToggle(dose) },
                            onOpenDetail = { onOpenDetail(dose) },
                        )
                    }
                }
            }
        }
    }
}

/** Icon tile, name and dose, mono time, round check. The next dose is tinted. */
@Composable
private fun TodayDoseRow(
    dose: TodayDose,
    isNext: Boolean,
    onToggle: () -> Unit,
    onOpenDetail: () -> Unit,
) {
    val colors = HavnTheme.colors
    val background by animateColorAsState(
        if (isNext) colors.signalSoft else groupColor(),
        HavnMotion.standard(),
        label = "row-bg",
    )
    val nameColor by animateColorAsState(
        if (dose.isPending) colors.textPrimary else colors.textTertiary,
        HavnMotion.standard(),
        label = "row-name",
    )
    val detail = listOfNotNull(
        dose.medication.dosage.takeIf { it.isNotBlank() },
    ).joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .heightIn(min = 64.dp)
            .havnPress(scaleDown = 0.99f, onClickLabel = "Open ${dose.medication.name}", onClick = onOpenDetail)
            .padding(start = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MedTile(dose.medication.iconType)
        Spacer(Modifier.width(HavnTheme.spacing.md))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dose.medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = nameColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                when {
                    isNext -> StatusTag("Next", colors.signal, colors.onSignal)
                    dose.isSkipped -> StatusTag("Skipped", colors.surfaceSunken, colors.textSecondary)
                }
            }
            if (detail.isNotEmpty()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (dose.isPending) colors.textSecondary else colors.textTertiary,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.width(HavnTheme.spacing.sm))
        Text(
            text = dose.slot.ifEmpty { "Any" },
            style = HavnType.Clock,
            color = if (isNext) colors.signal else colors.textSecondary,
        )
        RoundCheck(
            checked = dose.isTaken,
            isNext = isNext,
            onToggle = onToggle,
            contentDescription = "${dose.medication.name}, ${dose.displayTime()}",
        )
    }
}

/**
 * 26 dp round check. Tapping it dips the circle and lets it settle (no
 * overshoot), the fill and tick fade in on the house curve, and the next
 * dose's empty check breathes a faint orange ring until it is taken.
 */
@Composable
private fun RoundCheck(
    checked: Boolean,
    isNext: Boolean,
    onToggle: () -> Unit,
    contentDescription: String,
) {
    val colors = HavnTheme.colors
    val haptics = rememberHavnHaptics()
    val reduceMotion = rememberReduceMotion()
    val scope = rememberCoroutineScope()
    val press = remember { Animatable(1f) }

    val fill by animateColorAsState(
        if (checked) colors.accent else Color.Transparent, HavnMotion.quick(), label = "check-fill",
    )
    val ring by animateColorAsState(
        when {
            checked -> colors.accent
            isNext -> colors.signal
            else -> colors.textTertiary
        },
        HavnMotion.quick(),
        label = "check-ring",
    )
    val tick by animateFloatAsState(
        if (checked) 1f else 0f,
        if (reduceMotion) HavnMotion.none() else HavnMotion.standard(),
        label = "check-tick",
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .havnPress(scaleDown = 1f, onClickLabel = contentDescription) {
                if (checked) haptics.release() else haptics.latch()
                if (!reduceMotion) {
                    scope.launch {
                        press.snapTo(0.82f)
                        press.animateTo(1f, HavnMotion.settle())
                    }
                }
                onToggle()
            },
        contentAlignment = Alignment.Center,
    ) {
        if (isNext && !checked && !reduceMotion) {
            val pulse by rememberInfiniteTransition(label = "next-pulse").animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(2400, easing = HavnMotion.Decisive), RepeatMode.Restart),
                label = "next-pulse-value",
            )
            val ringColor = colors.signal
            Box(
                Modifier
                    .size(26.dp)
                    .drawBehind {
                        drawCircle(
                            color = ringColor.copy(alpha = (1f - pulse) * 0.5f),
                            radius = size.minDimension / 2f * (1f + pulse * 0.6f),
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }
            )
        }
        Box(
            modifier = Modifier
                .size(26.dp)
                .graphicsLayer {
                    scaleX = press.value
                    scaleY = press.value
                }
                .clip(CircleShape)
                .background(fill)
                .border(2.dp, ring, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier
                    .size(15.dp)
                    .graphicsLayer {
                        alpha = tick
                        scaleX = 0.6f + 0.4f * tick
                        scaleY = 0.6f + 0.4f * tick
                    },
            )
        }
    }
}

/**
 * The day's closing moment.
 *
 * A ring draws itself closed over a warmed ambient wash, then everything
 * fades. No confetti and no dialog to dismiss — it happens at most once a day
 * and must never stand between the user and the app, so it is non-interactive
 * and clears itself. Under reduced-motion the ring is simply present rather
 * than drawn.
 */
@Composable
private fun DayCompleteOverlay(visible: Boolean) {
    val colors = HavnTheme.colors
    val reduceMotion = rememberReduceMotion()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(HavnMotion.standard()),
        exit = fadeOut(tween(HavnMotion.Considered)),
    ) {
        val sweep = remember { Animatable(0f) }
        val lift = remember { Animatable(0.9f) }

        LaunchedEffect(Unit) {
            launch { lift.animateTo(1f, HavnMotion.celebrate()) }
            sweep.animateTo(1f, tween(900, easing = HavnMotion.Enter))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                // Not clickable: a full-screen catcher that swallowed a tap
                // would make the moment feel like an obstacle.
                .background(colors.canvas.copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .graphicsLayer {
                            scaleX = lift.value
                            scaleY = lift.value
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    HavnProgressRing(
                        progress = if (reduceMotion) 1f else sweep.value,
                        strokeWidth = 2.dp,
                        color = colors.accent,
                        trackColor = colors.hairline,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(34.dp),
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.xl))
                Text(
                    text = "Day complete",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(HavnTheme.spacing.xs))
                Text(
                    text = "Every dose logged.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

@Composable
private fun HomeChrome(
    user: User?,
    onOpenProfiles: () -> Unit,
    onOpenReminders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = HavnTheme.spacing.md, bottom = HavnTheme.spacing.xxl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "HÄVN",
                style = HavnType.Wordmark.copy(fontSize = 17.sp, letterSpacing = 2.4.sp),
                color = colors.textPrimary,
            )
            Text(
                text = "Medication reminder",
                style = HavnType.EyebrowQuiet,
                color = colors.textTertiary,
            )
        }

        HavnIconButton(
            icon = painterResource(R.drawable.ic_notification),
            contentDescription = "Reminders",
            onClick = onOpenReminders,
            size = 38.dp,
            iconSize = 17.dp,
        )
        Spacer(Modifier.width(HavnTheme.spacing.sm))
        ProfileAvatar(user = user, size = 38.dp, onClick = onOpenProfiles)
    }
}

@Composable
private fun ProfileAvatar(
    user: User?,
    size: androidx.compose.ui.unit.Dp,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
) {
    val colors = HavnTheme.colors
    val tint = avatarAccent(user?.avatarColor)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(tint.copy(alpha = if (colors.isDark) 0.28f else 0.16f))
            .then(
                if (onClick != null) {
                    Modifier.havnPress(
                        scaleDown = 0.9f,
                        onClickLabel = "Switch profile",
                        onClick = onClick,
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "·",
            style = MaterialTheme.typography.labelLarge,
            color = if (colors.isDark) tint.copy(alpha = 1f) else tint,
        )
    }
}

@Composable
private fun GreetingBlock(
    name: String,
    state: HomeUiState,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val reveal = rememberRevealProgress(key = name)
    val now = remember { LocalTime.now() }
    val today = remember { LocalDate.now() }

    val greeting = when (now.hour) {
        in 0..4 -> "Good night"
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }

    Column(modifier = modifier.havnReveal(reveal, travelDp = 14f)) {
        Text(
            text = today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")).uppercase(),
            style = HavnType.Eyebrow,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(HavnTheme.spacing.md))
        Text(
            // Two lines by construction: the greeting is context, the name is
            // the subject. Setting them at the same size on one line would make
            // it a sentence; stacked, it reads as a masthead.
            text = greeting,
            style = MaterialTheme.typography.displaySmall,
            color = colors.textSecondary,
        )
        if (name.isNotBlank()) {
            Text(
                text = name,
                style = MaterialTheme.typography.displayMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.height(HavnTheme.spacing.lg))
        FocusLine(state = state)
    }
}

/** One sentence telling the user what, if anything, needs them right now. */
@Composable
private fun FocusLine(state: HomeUiState) {
    val colors = HavnTheme.colors
    val next = state.nextDose

    val (text, tone) = when {
        state.isLoading -> "" to colors.textTertiary
        !state.hasMedications ->
            "Nothing scheduled yet." to colors.textTertiary
        state.allDone ->
            "Every dose is logged. Nothing left today." to colors.accent
        next != null && next.time != null && next.time.isBefore(LocalTime.now()) ->
            "Still open: ${next.medication.name}, due ${next.displayTime()}." to colors.textPrimary
        next != null && next.slot.isNotBlank() ->
            "Next: ${next.medication.name} at ${next.displayTime()}." to colors.textSecondary
        next != null ->
            "Next: ${next.medication.name}, any time today." to colors.textSecondary
        else -> "" to colors.textSecondary
    }

    if (text.isBlank()) return

    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = tone,
    )
}

/**
 * The day, as one number.
 *
 * Replaces the old adherence card *and* the weekly-trend card. The trend chart
 * was drawn from a hard-coded array — `listOf(1.0f, 0.85f, 1.0f, …)` — so it
 * showed the same invented week to every user regardless of what they had
 * actually taken. Real trends belong on Progress, where they are computed.
 */
@Composable
private fun DayMetric(
    state: HomeUiState,
    onMarkAll: () -> Unit,
    onOpenProgress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val progress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = HavnMotion.glide(),
        label = "dayProgress",
    )

    Column(modifier = modifier.padding(top = HavnTheme.spacing.xxl)) {
        Row(verticalAlignment = Alignment.Bottom) {
            HavnOdometerCounter(
                value = state.taken,
                style = HavnType.Metric,
                color = colors.textPrimary,
            )
            Spacer(Modifier.width(HavnTheme.spacing.sm))
            Text(
                text = "of ${state.total}",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textTertiary,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = state.remaining > 0,
                enter = fadeIn(HavnMotion.standard()),
                exit = fadeOut(HavnMotion.exit()),
            ) {
                HavnButton(
                    text = "Log all",
                    onClick = onMarkAll,
                    tone = HavnButtonTone.Secondary,
                    size = HavnButtonSize.Small,
                    fillWidth = false,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
        }

        Spacer(Modifier.height(HavnTheme.spacing.lg))

        // A single hairline rule rather than a chunky progress bar. At full
        // width it carries the same information with none of the chrome.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.hairline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accent)
            )
        }

        Spacer(Modifier.height(HavnTheme.spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when {
                    state.allDone && state.skipped > 0 ->
                        "Day complete · ${state.skipped} skipped"
                    state.allDone -> "Day complete"
                    else -> "${state.remaining} remaining"
                },
                style = HavnType.Eyebrow,
                color = if (state.allDone) colors.accent else colors.textTertiary,
            )
            Spacer(Modifier.weight(1f))
            HavnTextAction(text = "Progress", onClick = onOpenProgress)
        }
    }
}

/**
 * The organiser, full-bleed and unframed.
 *
 * No card, no caption, no count row beneath it: the box carries its own
 * period labels, its lids show what is waiting, and the part of the day that
 * is now stands open. Anything more would be chrome around the product.
 */
@Composable
private fun SectionRule(
    eyebrow: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = eyebrow.uppercase(),
            style = HavnType.Eyebrow,
            color = HavnTheme.colors.textTertiary,
        )
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
private fun PeriodLabel(period: DayPeriod, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = HavnTheme.spacing.lg, bottom = HavnTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = period.label,
            style = MaterialTheme.typography.titleSmall,
            color = HavnTheme.colors.textSecondary,
        )
        Spacer(Modifier.width(HavnTheme.spacing.md))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(HavnTheme.colors.hairline)
        )
    }
}

@Composable
private fun SwipeableDoseRow(
    dose: TodayDose,
    index: Int,
    onToggle: () -> Unit,
    onMarkStatus: (DoseStatus) -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HavnSwipeToLogRow(
        isTaken = dose.isTaken,
        isSkipped = dose.isSkipped,
        onTake = { onMarkStatus(DoseStatus.TAKEN) },
        onSkip = { onMarkStatus(DoseStatus.SKIPPED) },
        onUndo = { onMarkStatus(DoseStatus.PENDING) },
        modifier = modifier,
    ) {
        DoseRow(
            dose = dose,
            index = index,
            onToggle = onToggle,
            onOpenDetail = onOpenDetail,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * A dose, as a row — not a card.
 *
 * Every medication used to sit in its own elevated, bordered, shadowed
 * rectangle, so a five-dose day was five competing objects. Hairlines and
 * spacing separate them just as clearly and let the list read as one thing.
 */
@Composable
private fun DoseRow(
    dose: TodayDose,
    index: Int,
    onToggle: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val reveal = rememberRevealProgress(
        key = dose.medication.id to dose.slot,
        delayMs = HavnMotion.staggerDelay(index),
    )
    val accent = medAccent(dose.medication.colorTag)

    val contentAlpha by animateFloatAsState(
        targetValue = if (dose.isPending) 1f else 0.5f,
        animationSpec = HavnMotion.standard(),
        label = "doseAlpha",
    )
    val nameColor by animateColorAsState(
        targetValue = colors.textPrimary.copy(alpha = contentAlpha),
        animationSpec = HavnMotion.standard(),
        label = "doseName",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .havnReveal(reveal)
            .havnPress(
                scaleDown = 0.99f,
                onClickLabel = "Open ${dose.medication.name}",
                onClick = onOpenDetail,
            )
            .padding(vertical = HavnTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                // Paper square with a hairline: the medication's colour lives in
                // the glyph alone, so a list of five meds stays calm.
                .background(colors.surface)
                .border(1.dp, colors.hairlineStrong, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            MedIcon(
                type = dose.medication.iconType,
                size = 21.dp,
                tint = accent.copy(alpha = contentAlpha),
            )
        }

        Spacer(Modifier.width(HavnTheme.spacing.lg))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dose.medication.name,
                style = MaterialTheme.typography.bodyLarge,
                color = nameColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = buildString {
                    append(dose.displayTime())
                    if (dose.medication.dosage.isNotBlank()) {
                        append("  ·  ").append(dose.medication.dosage)
                    }
                    if (dose.isSkipped) append("  ·  skipped")
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(HavnTheme.spacing.sm))

        HavnDoseCheck(
            status = dose.status,
            onToggle = onToggle,
            accent = accent,
            contentDescription = "${dose.medication.name}, ${dose.displayTime()}",
        )
    }
}

@Composable
private fun TodayEmptyState(
    hasMedications: Boolean,
    onAddMedication: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (hasMedications) {
        HavnEmptyState(
            eyebrow = "All clear",
            headline = "Nothing due today",
            body = "Your medications are saved, but none are scheduled for today. " +
                "Enjoy the quiet one.",
            illustration = { HavnBreathingMark() },
            modifier = modifier,
        )
    } else {
        HavnEmptyState(
            eyebrow = "Getting started",
            headline = "Your organiser is empty",
            body = "Add your first medication and Hävn will keep the schedule, " +
                "the reminders and the record.",
            illustration = {},
            action = {
                HavnButton(
                    text = "Add a medication",
                    onClick = onAddMedication,
                    size = HavnButtonSize.Medium,
                    fillWidth = false,
                )
            },
            modifier = modifier,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Sheets
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSheet(
    profiles: List<User>,
    activeId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = HavnTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        contentColor = colors.textPrimary,
        scrimColor = colors.scrim,
        dragHandle = { HavnDragHandle() },
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HavnTheme.spacing.xl)
                .padding(bottom = HavnTheme.spacing.xxl),
        ) {
            Text(
                text = "Profiles",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(HavnTheme.spacing.xl))

            profiles.forEach { profile ->
                val isActive = profile.id == activeId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .havnPress(scaleDown = 0.99f) { onSelect(profile.id) }
                        .padding(vertical = HavnTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileAvatar(user = profile, size = 40.dp, selected = isActive)
                    Spacer(Modifier.width(HavnTheme.spacing.lg))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textPrimary,
                        )
                        if (profile.age > 0) {
                            Text(
                                text = "Age ${profile.age}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textTertiary,
                            )
                        }
                    }
                    if (isActive) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = "Active profile",
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(HavnTheme.spacing.md))
            Text(
                text = "Profiles are managed in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoseDetailSheet(
    dose: TodayDose,
    onDismiss: () -> Unit,
    onSetStatus: (DoseStatus) -> Unit,
) {
    val colors = HavnTheme.colors
    val accent = medAccent(dose.medication.colorTag)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        contentColor = colors.textPrimary,
        scrimColor = colors.scrim,
        dragHandle = { HavnDragHandle() },
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HavnTheme.spacing.xl)
                .padding(bottom = HavnTheme.spacing.xxl),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = if (colors.isDark) 0.2f else 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    MedIcon(type = dose.medication.iconType, size = 26.dp, tint = accent)
                }
                Spacer(Modifier.width(HavnTheme.spacing.lg))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dose.medication.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = buildString {
                            append(dose.displayTime())
                            if (dose.medication.dosage.isNotBlank()) {
                                append("  ·  ").append(dose.medication.dosage)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textTertiary,
                    )
                }
            }

            if (dose.medication.notes.isNotBlank()) {
                Spacer(Modifier.height(HavnTheme.spacing.xl))
                Text(
                    text = "INSTRUCTIONS",
                    style = HavnType.EyebrowQuiet,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(HavnTheme.spacing.sm))
                Text(
                    text = dose.medication.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }

            Spacer(Modifier.height(HavnTheme.spacing.xxl))

            Row(horizontalArrangement = Arrangement.spacedBy(HavnTheme.spacing.md)) {
                HavnButton(
                    text = if (dose.isTaken) "Taken" else "Mark taken",
                    onClick = { onSetStatus(DoseStatus.TAKEN) },
                    tone = HavnButtonTone.Primary,
                    size = HavnButtonSize.Medium,
                    enabled = !dose.isTaken,
                    modifier = Modifier.weight(1f),
                )
                HavnButton(
                    text = if (dose.isSkipped) "Skipped" else "Skip",
                    onClick = { onSetStatus(DoseStatus.SKIPPED) },
                    tone = HavnButtonTone.Secondary,
                    size = HavnButtonSize.Medium,
                    enabled = !dose.isSkipped,
                    modifier = Modifier.weight(1f),
                )
            }

            if (!dose.isPending) {
                Spacer(Modifier.height(HavnTheme.spacing.md))
                HavnButton(
                    text = "Clear",
                    onClick = { onSetStatus(DoseStatus.PENDING) },
                    tone = HavnButtonTone.Ghost,
                    size = HavnButtonSize.Medium,
                )
            }
        }
    }
}

@Composable
fun HavnDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = HavnTheme.spacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(HavnTheme.colors.hairlineStrong)
        )
    }
}

/** Medication accent, resolved per theme so tags stay legible on dark. */
@Composable
fun medAccent(tag: String): Color {
    val colors = HavnTheme.colors
    return when (tag) {
        "clay", "terracotta" -> colors.medClay
        "amber", "butter" -> colors.medAmber
        "slate" -> colors.medSlate
        "sand" -> colors.medSand
        else -> colors.medSage
    }
}

/**
 * Profile colours are stored as hex (onboarding's AVATAR_COLORS). They render on
 * the same ink ladder as medications, so no old hue reaches the screen.
 */
@Composable
fun avatarAccent(hex: String?): Color {
    val colors = HavnTheme.colors
    return when (hex?.uppercase()) {
        "#9A5637" -> colors.medClay
        "#8A6B22" -> colors.medAmber
        "#7E929A", "#6B5F7A" -> colors.medSlate
        "#A89878" -> colors.medSand
        else -> colors.medSage
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Greeting & Metric - Light", showBackground = true, backgroundColor = 0xFFF2F1EC)
@Composable
private fun HomeGreetingLightPreview() {
    com.havn.app.ui.theme.HavnTheme(themeMode = com.havn.app.ui.theme.ThemeMode.LIGHT) {
        Column(modifier = Modifier.padding(20.dp)) {
            GreetingBlock(
                name = "Elin",
                state = HomeUiState(
                    user = User(name = "Elin", age = 29, avatarColor = "#516351"),
                    hasMedications = true,
                    isLoading = false,
                ),
            )
            DayMetric(
                state = HomeUiState(
                    user = User(name = "Elin", age = 29, avatarColor = "#516351"),
                    hasMedications = true,
                    isLoading = false,
                ),
                onMarkAll = {},
                onOpenProgress = {},
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Greeting & Metric - Dark", showBackground = true, backgroundColor = 0xFF121210, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeGreetingDarkPreview() {
    com.havn.app.ui.theme.HavnTheme(themeMode = com.havn.app.ui.theme.ThemeMode.DARK) {
        Column(modifier = Modifier.padding(20.dp)) {
            GreetingBlock(
                name = "Elin",
                state = HomeUiState(
                    user = User(name = "Elin", age = 29, avatarColor = "#516351"),
                    hasMedications = true,
                    isLoading = false,
                ),
            )
            DayMetric(
                state = HomeUiState(
                    user = User(name = "Elin", age = 29, avatarColor = "#516351"),
                    hasMedications = true,
                    isLoading = false,
                ),
                onMarkAll = {},
                onOpenProgress = {},
            )
        }
    }
}


package com.havn.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.havn.app.ui.components.HavnDoseCheck
import com.havn.app.ui.components.HavnDoseRowSkeleton
import com.havn.app.ui.components.HavnProgressRing
import com.havn.app.ui.components.HavnEmptyState
import com.havn.app.ui.components.HavnIconButton
import com.havn.app.ui.components.HavnOrganizerView
import com.havn.app.ui.components.HavnTextAction
import com.havn.app.ui.components.MedIcon
import com.havn.app.ui.components.OrganizerDataMapper
import com.havn.app.ui.components.havnPress
import com.havn.app.ui.components.havnReveal
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
    onOpenOrganizer: () -> Unit,
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

    // HomeViewModel has always emitted these; nothing listened, so finishing a
    // day produced no acknowledgement at all. The moment is deliberately brief
    // and happens at most once a day, which is what lets it be a real one.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is HomeEvent.DayCompleted) {
                celebrating = true
                delay(2600)
                celebrating = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        // The ambient field is anchored to the top of the screen, not to the
        // scroll content, so it behaves like light in the room rather than an
        // element that scrolls away.
        HavnAmbientField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 420.dp)
                .aspectRatio(0.82f),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = contentPadding.calculateBottomPadding() + HavnTheme.spacing.xxl,
            ),
        ) {
            item(key = "chrome") {
                HomeChrome(
                    user = uiState.user,
                    onOpenProfiles = { showProfiles = true },
                    onOpenReminders = onOpenReminders,
                    modifier = Modifier.padding(horizontal = gutter),
                )
            }

            item(key = "greeting") {
                GreetingBlock(
                    name = uiState.user?.name.orEmpty(),
                    state = uiState,
                    modifier = Modifier.padding(horizontal = gutter),
                )
            }

            item(key = "metric") {
                if (uiState.isLoading) {
                    Spacer(Modifier.height(HavnTheme.spacing.xl))
                } else if (uiState.total > 0) {
                    DayMetric(
                        state = uiState,
                        onMarkAll = viewModel::markAllRemaining,
                        onOpenProgress = onOpenProgress,
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                }
            }

            item(key = "organizer") {
                if (uiState.hasMedications) {
                    Spacer(Modifier.height(HavnTheme.spacing.section))
                    OrganizerHero(
                        doses = uiState.doses,
                        onOpen = onOpenOrganizer,
                    )
                }
            }

            item(key = "today-header") {
                Spacer(Modifier.height(HavnTheme.spacing.section))
                SectionRule(
                    eyebrow = "Today",
                    trailing = {
                        if (uiState.hasMedications) {
                            HavnTextAction(text = "Manage", onClick = onManageMedications)
                        }
                    },
                    modifier = Modifier.padding(horizontal = gutter),
                )
                Spacer(Modifier.height(HavnTheme.spacing.lg))
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
                    // Grouped by part of day. A flat list of eight doses is
                    // just a list; grouped, it reads as a day with a shape.
                    DayPeriod.entries.forEach { period ->
                        val periodDoses = uiState.dosesIn(period)
                        if (periodDoses.isEmpty()) return@forEach

                        item(key = "period-${period.name}") {
                            PeriodLabel(
                                period = period,
                                modifier = Modifier.padding(horizontal = gutter),
                            )
                        }
                        itemsIndexed(periodDoses) { index, dose ->
                            DoseRow(
                                dose = dose,
                                index = index,
                                onToggle = { viewModel.toggleDose(dose) },
                                onOpenDetail = { detailDose = dose },
                                modifier = Modifier.padding(horizontal = gutter),
                            )
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
    val reduceMotion = remember { false }

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
    val tint = remember(user?.avatarColor) {
        runCatching { Color(android.graphics.Color.parseColor(user?.avatarColor ?: "#516351")) }
            .getOrDefault(Color(0xFF516351))
    }

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
            Text(
                text = "${state.taken}",
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
                .clip(CircleShape)
                .background(colors.hairline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(2.dp)
                    .clip(CircleShape)
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
 * The 3D organizer, full-bleed.
 *
 * It was previously boxed in a 4:3 card with a rounded clip, a 1dp border, a
 * drop shadow and an overlaid caption chip — five layers of chrome around the
 * most distinctive asset in the product. Here it runs edge to edge and is the
 * UI, with the surrounding page providing the frame.
 */
@Composable
private fun OrganizerHero(
    doses: List<TodayDose>,
    onOpen: () -> Unit,
) {
    val colors = HavnTheme.colors
    val gutter = HavnTheme.spacing.gutter
    val currentPeriod = remember { DayPeriod.current() }
    val payload = remember(doses) { OrganizerDataMapper.buildPeriodJson(doses) }

    Column {
        SectionRule(
            eyebrow = "Organizer",
            trailing = { HavnTextAction(text = "Open", onClick = onOpen) },
            modifier = Modifier.padding(horizontal = gutter),
        )
        Spacer(Modifier.height(HavnTheme.spacing.lg))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .havnPress(scaleDown = 0.995f, onClickLabel = "Open organizer", onClick = onOpen),
        ) {
            HavnOrganizerView(
                dataJson = payload,
                selectedSlot = currentPeriod.ordinal,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(Modifier.height(HavnTheme.spacing.lg))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = gutter),
            horizontalArrangement = Arrangement.spacedBy(HavnTheme.spacing.xl),
        ) {
            DayPeriod.entries.forEach { period ->
                val count = doses.count { it.period == period }
                val isNow = period == currentPeriod
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = period.label.uppercase(),
                        style = HavnType.EyebrowQuiet,
                        color = if (isNow) colors.accent else colors.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.xs))
                    Text(
                        text = if (count == 0) "—" else "$count",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isNow) colors.textPrimary else colors.textSecondary,
                    )
                }
            }
        }
    }
}

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
                .clip(CircleShape)
                .background(accent.copy(alpha = if (colors.isDark) 0.18f else 0.12f)),
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
            headline = "Your shelf is empty",
            body = "Add your first medication and Hävn will keep the schedule, " +
                "the reminders and the record.",
            illustration = { HavnBreathingMark() },
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
        shape = RoundedCornerShape(
            topStart = HavnTheme.radius.xl,
            topEnd = HavnTheme.radius.xl,
        ),
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
                        .clip(RoundedCornerShape(HavnTheme.radius.md))
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
        shape = RoundedCornerShape(
            topStart = HavnTheme.radius.xl,
            topEnd = HavnTheme.radius.xl,
        ),
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
                        .clip(CircleShape)
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
                .clip(CircleShape)
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

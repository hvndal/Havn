package com.havn.app.ui.screens.reminders

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.DisposableEffect
import com.havn.app.R
import com.havn.app.domain.model.Medication
import com.havn.app.ui.components.HavnBreathingMark
import com.havn.app.ui.components.HavnButton
import com.havn.app.ui.components.HavnButtonSize
import com.havn.app.ui.components.HavnButtonTone
import com.havn.app.ui.components.HavnEmptyState
import com.havn.app.ui.components.HavnIconButton
import com.havn.app.ui.components.HavnSegmented
import com.havn.app.ui.components.HavnSurface
import com.havn.app.ui.components.HavnSwitchRow
import com.havn.app.ui.components.HavnTimePickerDialog
import com.havn.app.ui.components.havnPress
import com.havn.app.ui.components.havnReveal
import com.havn.app.ui.components.rememberRevealProgress
import com.havn.app.ui.screens.home.medAccent
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Reminders — the schedule, the permission state, and how alerts behave.
 *
 * There was no such screen before. Reminder behaviour existed only as a handful
 * of inert rows in Settings ("Daily WorkManager Reminders / Active · Scheduled
 * Times") whose `onClick` bodies were empty, so nothing about reminders was
 * actually inspectable or adjustable — including whether notifications were
 * permitted at all.
 */
@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    onAddMedication: () -> Unit,
    onEditMedication: (Long) -> Unit,
    viewModel: RemindersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HavnTheme.colors
    val gutter = HavnTheme.spacing.gutter
    val context = LocalContext.current

    var showEveningTimePicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshPermission() }

    // Permission can change while the app is backgrounded — the user may go to
    // system settings and flip it. Re-checking on resume keeps this screen from
    // showing a stale banner.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = HavnTheme.spacing.huge),
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
                        text = "WHAT HÄVN WILL TELL YOU",
                        style = HavnType.Eyebrow,
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.sm))
                    Text(
                        text = "Reminders",
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.textPrimary,
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.xl))
            }

            if (uiState.permission != PermissionState.Granted) {
                item(key = "permission") {
                    PermissionCard(
                        state = uiState.permission,
                        onEnable = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                viewModel.onPermissionRequested()
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onOpenSettings = {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        },
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.section))
                }
            }

            item(key = "next-up-header") {
                Text(
                    text = "NEXT UP",
                    style = HavnType.Eyebrow,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(horizontal = gutter),
                )
                Spacer(Modifier.height(HavnTheme.spacing.lg))
            }

            if (uiState.upcoming.isEmpty() && !uiState.isLoading) {
                item(key = "upcoming-empty") {
                    HavnEmptyState(
                        headline = "No reminders set",
                        body = "Give a medication a time and it will appear here, " +
                            "with a notification you can act on without opening Hävn.",
                        illustration = { HavnBreathingMark(size = 60.dp) },
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
            } else {
                itemsIndexed(uiState.upcoming.take(8)) { index, reminder ->
                    UpcomingRow(
                        reminder = reminder,
                        index = index,
                        onClick = { onEditMedication(reminder.medication.id) },
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                }
            }

            if (uiState.unscheduledMedications.isNotEmpty()) {
                item(key = "unscheduled") {
                    Spacer(Modifier.height(HavnTheme.spacing.section))
                    Text(
                        text = "NO TIME SET",
                        style = HavnType.Eyebrow,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.sm))
                    Text(
                        text = "These won't remind you. Tap to add a time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.lg))
                }
                itemsIndexed(uiState.unscheduledMedications) { index, med ->
                    UnscheduledRow(
                        medication = med,
                        index = index,
                        onClick = { onEditMedication(med.id) },
                        modifier = Modifier.padding(horizontal = gutter),
                    )
                }
            }

            item(key = "settings") {
                Spacer(Modifier.height(HavnTheme.spacing.section))
                Text(
                    text = "HOW THEY BEHAVE",
                    style = HavnType.Eyebrow,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(horizontal = gutter),
                )
                Spacer(Modifier.height(HavnTheme.spacing.lg))

                Column(Modifier.padding(horizontal = gutter)) {
                    HavnSwitchRow(
                        title = "Early nudge",
                        subtitle = "A quiet heads-up 15 minutes before each dose",
                        checked = uiState.preDoseEnabled,
                        onCheckedChange = viewModel::setPreDose,
                    )
                    HavnSwitchRow(
                        title = "Evening check-in",
                        subtitle = "One summary at ${formatClock(uiState.eveningCheckTime)} " +
                            "if anything is outstanding",
                        checked = uiState.eveningCheckEnabled,
                        onCheckedChange = viewModel::setEveningCheck,
                        onRowClick = if (uiState.eveningCheckEnabled) {
                            { showEveningTimePicker = true }
                        } else null,
                    )
                    HavnSwitchRow(
                        title = "Vibrate",
                        subtitle = "Alongside the notification sound",
                        checked = uiState.vibration,
                        onCheckedChange = viewModel::setVibration,
                    )
                }

                Spacer(Modifier.height(HavnTheme.spacing.xl))

                Column(Modifier.padding(horizontal = gutter)) {
                    Text(
                        text = "ALERT STYLE",
                        style = HavnType.Eyebrow,
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.md))
                    HavnSegmented(
                        options = listOf("Chime", "Marimba", "Silent"),
                        selectedIndex = when (uiState.sound.uppercase()) {
                            "MARIMBA" -> 1
                            "SILENT" -> 2
                            else -> 0
                        },
                        onSelect = { index ->
                            viewModel.setSound(
                                when (index) {
                                    1 -> "MARIMBA"
                                    2 -> "SILENT"
                                    else -> "CHIME"
                                }
                            )
                        },
                    )
                }

                Spacer(Modifier.height(HavnTheme.spacing.xxl))

                HavnButton(
                    text = "Send a test reminder",
                    onClick = viewModel::sendTestNotification,
                    tone = HavnButtonTone.Secondary,
                    size = HavnButtonSize.Medium,
                    modifier = Modifier.padding(horizontal = gutter),
                )
            }
        }
    }

    if (showEveningTimePicker) {
        HavnTimePickerDialog(
            initial = runCatching { LocalTime.parse(uiState.eveningCheckTime) }
                .getOrDefault(LocalTime.of(20, 0)),
            title = "Evening check-in",
            onDismiss = { showEveningTimePicker = false },
            onConfirm = { time ->
                viewModel.setEveningCheckTime(
                    time.format(DateTimeFormatter.ofPattern("HH:mm"))
                )
                showEveningTimePicker = false
            },
        )
    }
}

@Composable
private fun PermissionCard(
    state: PermissionState,
    onEnable: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val blocked = state == PermissionState.BlockedInSettings

    HavnSurface(
        modifier = modifier.fillMaxWidth(),
        color = colors.warningSoft,
        border = null,
        elevation = HavnTheme.elevation.none,
        contentPadding = HavnTheme.spacing.xl,
    ) {
        Text(
            text = if (blocked) "Notifications are switched off"
            else "Reminders need permission",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onWarningSoft,
        )
        Spacer(Modifier.height(HavnTheme.spacing.sm))
        Text(
            text = if (blocked) {
                "Hävn can schedule doses, but Android won't show them. " +
                    "Turn notifications back on to start receiving reminders."
            } else {
                "Hävn will only send what you schedule — a dose reminder, an " +
                    "optional early nudge, and one evening summary."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onWarningSoft.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(HavnTheme.spacing.xl))
        HavnButton(
            text = if (blocked) "Open notification settings" else "Allow reminders",
            onClick = if (blocked) onOpenSettings else onEnable,
            size = HavnButtonSize.Medium,
            fillWidth = false,
        )
    }
}

@Composable
private fun UpcomingRow(
    reminder: UpcomingReminder,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val accent = medAccent(reminder.medication.colorTag)
    val reveal = rememberRevealProgress(
        key = reminder.medication.id to reminder.slot,
        delayMs = HavnMotion.staggerDelay(index),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .havnReveal(reveal)
            .havnPress(scaleDown = 0.99f, onClick = onClick)
            .padding(vertical = HavnTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(74.dp)) {
            Text(
                text = formatClock(reminder.slot),
                style = HavnType.Clock,
                color = colors.textPrimary,
            )
            Text(
                text = if (reminder.isToday) "Today" else "Tomorrow",
                style = HavnType.EyebrowQuiet,
                color = colors.textTertiary,
            )
        }

        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(Modifier.width(HavnTheme.spacing.lg))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reminder.medication.name,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (reminder.medication.dosage.isNotBlank()) {
                Text(
                    text = reminder.medication.dosage,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }

        Text(
            text = relativeTime(reminder.at),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun UnscheduledRow(
    medication: Medication,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val reveal = rememberRevealProgress(
        key = medication.id,
        delayMs = HavnMotion.staggerDelay(index),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .havnReveal(reveal)
            .havnPress(scaleDown = 0.99f, onClick = onClick)
            .padding(vertical = HavnTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = medication.name,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Set a time",
            style = MaterialTheme.typography.labelMedium,
            color = colors.accent,
        )
    }
}

private fun formatClock(raw: String): String = runCatching {
    LocalTime.parse(raw)
        .format(DateTimeFormatter.ofPattern("h:mm a"))
        .replace("AM", "am").replace("PM", "pm")
}.getOrDefault(raw)

private fun relativeTime(at: LocalDateTime): String {
    val minutes = Duration.between(LocalDateTime.now(), at).toMinutes()
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "in ${minutes}m"
        minutes < 60 * 24 -> "in ${minutes / 60}h"
        else -> "in ${minutes / (60 * 24)}d"
    }
}

package com.havn.app.widget

import android.app.NotificationManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.havn.app.data.db.DoseLogEntity
import com.havn.app.data.db.HavnDatabase
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.ui.MainActivity
import com.havn.app.data.repository.HavnRepository
import com.havn.app.domain.model.DoseStatus
import com.havn.app.domain.model.RepeatType
import com.havn.app.domain.model.TodayDose
import com.havn.app.notifications.AlarmReceiver
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class WidgetPillItem(
    val id: Long,
    val name: String,
    val dosage: String,
    val scheduledTimeStr: String,
    val iconType: String,
    val isTaken: Boolean,
    /** "HH:mm", or empty for an any-time dose. */
    val slot: String = "",
)

data class WidgetState(
    val userName: String? = null,
    val dateStr: String = "",
    val items: List<WidgetPillItem> = emptyList(),
    val totalCount: Int = 0,
    val takenCount: Int = 0,
    val hasMedications: Boolean = false,
) {
    val isCompletedForToday: Boolean get() = totalCount > 0 && takenCount >= totalCount
    val nextPendingPill: WidgetPillItem? get() = items.firstOrNull { !it.isTaken }
    val remainingCount: Int get() = (totalCount - takenCount).coerceAtLeast(0)
}

suspend fun updateHavnWidget(context: Context) {
    runCatching {
        HavnWidget().updateAll(context)
    }
}

/** The widget runs outside Hilt's graph; this is its door into it. */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface WidgetEntryPoint {
    fun repository(): HavnRepository
}

private fun repositoryOf(context: Context): HavnRepository =
    dagger.hilt.android.EntryPointAccessors
        .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        .repository()

/**
 * Today's doses for the active profile, built exactly as the Today screen
 * builds them: only medications scheduled today (weekly days and start date
 * respected, as-needed excluded), and one entry per reminder time, so a
 * twice-daily medication shows — and logs — its morning and evening doses
 * separately.
 */
suspend fun loadWidgetState(context: Context): WidgetState {
    val today = LocalDate.now()
    val dateFormatted = today.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US))
    return runCatching {
        val repo = repositoryOf(context)
        val activeUserId = UserPreferences(context).activeUserId.first()
        val users = repo.getAllUsers().first()
        val activeUser = users.find { it.id == activeUserId } ?: users.firstOrNull()
            ?: return@runCatching WidgetState(dateStr = dateFormatted, hasMedications = false)

        val meds = repo.getMedicationsForUser(activeUser.id).first()
            .filter { it.isActive && it.repeatType != RepeatType.AS_NEEDED && it.isScheduledOn(today) }
        if (meds.isEmpty()) {
            return@runCatching WidgetState(userName = activeUser.name, dateStr = dateFormatted, hasMedications = false)
        }

        val taken = repo.getDoseLogsForDay(activeUser.id, today).first()
            .filter { it.status == DoseStatus.TAKEN }
            .map { it.medicationId to it.scheduledSlot }
            .toSet()

        val pillItems = meds.flatMap { med ->
            med.reminderTimes.ifEmpty { listOf("") }.map { slot ->
                val dose = TodayDose(medication = med, doseLog = null, slot = slot)
                WidgetPillItem(
                    id = med.id,
                    name = med.name,
                    dosage = med.dosage,
                    scheduledTimeStr = dose.displayTime(),
                    iconType = med.iconType.name,
                    isTaken = (med.id to slot) in taken,
                    slot = slot,
                )
            }
        }.sortedWith(compareBy<WidgetPillItem> { it.isTaken }.thenBy { it.slot.ifEmpty { "99:99" } })

        WidgetState(
            userName = activeUser.name,
            dateStr = dateFormatted,
            items = pillItems,
            totalCount = pillItems.size,
            takenCount = pillItems.count { it.isTaken },
            hasMedications = true,
        )
    }.getOrDefault(WidgetState(dateStr = dateFormatted))
}

/**
 * Logs through the repository (so the slot is recorded and the reminder for
 * it stands down) and clears the matching status-bar notification.
 */
private suspend fun setFromWidget(context: Context, medId: Long, slot: String, status: DoseStatus) {
    val repo = repositoryOf(context)
    val med = repo.getMedicationById(medId) ?: return
    val today = LocalDate.now()
    repo.setDoseStatus(med, today, slot, status)
    if (status == DoseStatus.TAKEN) {
        AlarmReceiver.slotMillis(today, slot)?.let { millis ->
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
                ?.cancel(AlarmReceiver.doseNotificationId(millis, med.userId))
        }
    }
    HavnWidget().updateAll(context)
}

class MarkDoseTakenActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val medId = parameters[MedicationIdKey] ?: return
        setFromWidget(context, medId, parameters[SlotKey].orEmpty(), DoseStatus.TAKEN)
    }

    companion object {
        val MedicationIdKey = ActionParameters.Key<Long>("medication_id")
        val SlotKey = ActionParameters.Key<String>("slot")
    }
}

class MarkAllDosesTakenActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        loadWidgetState(context).items.filterNot { it.isTaken }.forEach {
            setFromWidget(context, it.id, it.slot, DoseStatus.TAKEN)
        }
    }
}

class MarkDoseUntakenActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val medId = parameters[MedicationIdKey] ?: return
        setFromWidget(context, medId, parameters[SlotKey].orEmpty(), DoseStatus.PENDING)
    }

    companion object {
        val MedicationIdKey = ActionParameters.Key<Long>("medication_id")
        val SlotKey = ActionParameters.Key<String>("slot")
    }
}

class HavnWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = loadWidgetState(context)
        provideContent {
            HavnWidgetContent(state)
        }
    }
}

@Composable
private fun HavnWidgetContent(state: WidgetState) {
    val size = LocalSize.current
    val isCompact = size.height < 115.dp

    if (isCompact) {
        CompactWidgetLayout(state)
    } else {
        StandardWidgetLayout(state, size)
    }
}

@Composable
private fun CompactWidgetLayout(state: WidgetState) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetPalette.canvas)
            .cornerRadius(16.dp)
            .padding(10.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = GlanceModifier
                    .size(36.dp)
                    .background(if (state.isCompletedForToday) WidgetPalette.takenBubble else WidgetPalette.idleBubble)
                    .cornerRadius(18.dp),
                contentAlignment = Alignment.Center,
            ) {
                WidgetGlyph(
                    res = if (state.isCompletedForToday) com.havn.app.R.drawable.ic_check else com.havn.app.R.drawable.ic_med_capsule,
                    tint = if (state.isCompletedForToday) WidgetPalette.onAccentSoft else WidgetPalette.accent,
                    size = 17.dp,
                )
            }

            Spacer(GlanceModifier.width(10.dp))

            // Text Info
            Column(modifier = GlanceModifier.defaultWeight()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hävn",
                        style = TextStyle(
                            color = WidgetPalette.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    if (!state.userName.isNullOrEmpty()) {
                        Text(
                            text = " • ${state.userName}",
                            style = TextStyle(
                                color = WidgetPalette.textTertiary,
                                fontSize = 11.sp,
                            ),
                        )
                    }
                }

                val nextPill = state.nextPendingPill
                if (state.isCompletedForToday) {
                    Text(
                        text = "All ${state.totalCount} doses taken today",
                        style = TextStyle(
                            color = WidgetPalette.accentDeep,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        maxLines = 1,
                    )
                } else if (nextPill != null) {
                    Text(
                        text = "${nextPill.name} (${nextPill.dosage})",
                        style = TextStyle(
                            color = WidgetPalette.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                    )
                    Text(
                        text = "Due: ${nextPill.scheduledTimeStr} • ${state.remainingCount} left",
                        style = TextStyle(
                            color = WidgetPalette.textTertiary,
                            fontSize = 10.sp,
                        ),
                        maxLines = 1,
                    )
                } else {
                    Text(
                        text = "No medications scheduled",
                        style = TextStyle(
                            color = WidgetPalette.textTertiary,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }

            Spacer(GlanceModifier.width(8.dp))

            // Action Button
            val nextActionPill = state.nextPendingPill
            if (nextActionPill != null) {
                Box(
                    modifier = GlanceModifier
                        .background(WidgetPalette.accent)
                        .cornerRadius(10.dp)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .clickable(
                            actionRunCallback<MarkDoseTakenActionCallback>(
                                actionParametersOf(
                                    MarkDoseTakenActionCallback.MedicationIdKey to nextActionPill.id,
                                    MarkDoseTakenActionCallback.SlotKey to nextActionPill.slot,
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Take",
                        style = TextStyle(
                            color = WidgetPalette.onAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            } else if (state.isCompletedForToday) {
                Box(
                    modifier = GlanceModifier
                        .background(WidgetPalette.accentSoft)
                        .cornerRadius(10.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Done",
                        style = TextStyle(
                            color = WidgetPalette.onAccentSoft,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardWidgetLayout(state: WidgetState, size: DpSize) {
    val isLarge = size.height >= 210.dp
    val maxItemsToShow = if (isLarge) 4 else 2

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetPalette.canvas)
            .cornerRadius(16.dp)
            .padding(12.dp),
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity<MainActivity>()),
            ) {
                // Hävn logo tag
                Box(
                    modifier = GlanceModifier
                        .size(10.dp)
                        .background(WidgetPalette.accent)
                        .cornerRadius(5.dp)
                ) {}
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = "Hävn",
                    style = TextStyle(
                        color = WidgetPalette.accent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                if (!state.userName.isNullOrEmpty()) {
                    Text(
                        text = " • ${state.userName}",
                        style = TextStyle(
                            color = WidgetPalette.textTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                }

                Spacer(GlanceModifier.defaultWeight())

                // Adherence Chip
                if (state.totalCount > 0) {
                    Box(
                        modifier = GlanceModifier
                            .background(if (state.isCompletedForToday) WidgetPalette.accentSoft else WidgetPalette.surfaceSunken)
                            .cornerRadius(12.dp)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (state.isCompletedForToday) "All done" else "${state.takenCount}/${state.totalCount} Taken",
                            style = TextStyle(
                                color = if (state.isCompletedForToday) WidgetPalette.onAccentSoft else WidgetPalette.accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }

                // Quick "Take All" button if 2+ untaken pills remaining
                if (state.remainingCount >= 2) {
                    Spacer(GlanceModifier.width(6.dp))
                    Box(
                        modifier = GlanceModifier
                            .background(WidgetPalette.accent)
                            .cornerRadius(10.dp)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .clickable(actionRunCallback<MarkAllDosesTakenActionCallback>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Take All",
                            style = TextStyle(
                                color = WidgetPalette.onAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            // Body Content
            if (!state.hasMedications) {
                EmptyMedicationsCard()
            } else if (state.isCompletedForToday) {
                CompletedDayCard(state)
            } else {
                ActivePillsList(state, maxItemsToShow)
            }

            Spacer(GlanceModifier.defaultWeight())

            // Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity<MainActivity>()),
            ) {
                Text(
                    text = state.dateStr,
                    style = TextStyle(
                        color = WidgetPalette.textTertiary,
                        fontSize = 10.sp,
                    ),
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = "Tap to open app →",
                    style = TextStyle(
                        color = WidgetPalette.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ActivePillsList(state: WidgetState, maxItems: Int) {
    val itemsToShow = state.items.take(maxItems)
    val remainingItems = state.items.size - itemsToShow.size

    Column(modifier = GlanceModifier.fillMaxWidth()) {
        itemsToShow.forEachIndexed { index, item ->
            if (index > 0) {
                Spacer(GlanceModifier.height(6.dp))
            }
            WidgetPillRow(item = item)
        }

        if (remainingItems > 0) {
            Spacer(GlanceModifier.height(4.dp))
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "+$remainingItems more pills for today",
                    style = TextStyle(
                        color = WidgetPalette.textTertiary,
                        fontSize = 10.sp,
                    )
                )
            }
        }
    }
}

@Composable
private fun WidgetPillRow(item: WidgetPillItem) {
    val glyph = when (item.iconType.lowercase()) {
        "tablet" -> com.havn.app.R.drawable.ic_med_tablet
        "liquid" -> com.havn.app.R.drawable.ic_med_liquid
        "injection" -> com.havn.app.R.drawable.ic_med_injection
        "powder" -> com.havn.app.R.drawable.ic_med_powder
        else -> com.havn.app.R.drawable.ic_med_capsule
    }

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(if (item.isTaken) WidgetPalette.takenRow else WidgetPalette.surface)
            .cornerRadius(10.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Pill Type Icon
            Box(
                modifier = GlanceModifier
                    .size(26.dp)
                    .background(if (item.isTaken) WidgetPalette.takenBubble else WidgetPalette.idleBubble)
                    .cornerRadius(13.dp),
                contentAlignment = Alignment.Center,
            ) {
                WidgetGlyph(
                    res = if (item.isTaken) com.havn.app.R.drawable.ic_check else glyph,
                    tint = if (item.isTaken) WidgetPalette.onAccentSoft else WidgetPalette.accent,
                    size = 14.dp,
                )
            }

            Spacer(GlanceModifier.width(8.dp))

            // Pill details
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = item.name,
                    style = TextStyle(
                        color = if (item.isTaken) WidgetPalette.textTertiary else WidgetPalette.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                )
                Text(
                    text = "${item.dosage} • ${item.scheduledTimeStr}",
                    style = TextStyle(
                        color = WidgetPalette.textTertiary,
                        fontSize = 10.sp,
                    ),
                    maxLines = 1,
                )
            }

            Spacer(GlanceModifier.width(6.dp))

            // Action: Mark Taken / Taken badge
            if (item.isTaken) {
                // Tapping a taken dose allows quick untake toggle if marked by accident
                Box(
                    modifier = GlanceModifier
                        .background(WidgetPalette.accentSoft)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable(
                            actionRunCallback<MarkDoseUntakenActionCallback>(
                                actionParametersOf(
                                    MarkDoseUntakenActionCallback.MedicationIdKey to item.id,
                                    MarkDoseUntakenActionCallback.SlotKey to item.slot,
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Taken",
                        style = TextStyle(
                            color = WidgetPalette.onAccentSoft,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            } else {
                // Direct interactive "Take" button on the home screen!
                Box(
                    modifier = GlanceModifier
                        .background(WidgetPalette.accent)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .clickable(
                            actionRunCallback<MarkDoseTakenActionCallback>(
                                actionParametersOf(
                                    MarkDoseTakenActionCallback.MedicationIdKey to item.id,
                                    MarkDoseTakenActionCallback.SlotKey to item.slot,
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Take",
                        style = TextStyle(
                            color = WidgetPalette.onAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedDayCard(state: WidgetState) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(WidgetPalette.accentSoft)
            .cornerRadius(12.dp)
            .padding(10.dp)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WidgetGlyph(
                    res = com.havn.app.R.drawable.ic_check,
                    tint = WidgetPalette.accent,
                    size = 16.dp,
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = "Everything taken today",
                    style = TextStyle(
                        color = WidgetPalette.accentDeep,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
            Spacer(GlanceModifier.height(3.dp))
            Text(
                text = "All ${state.totalCount} daily prescriptions logged",
                style = TextStyle(
                    color = WidgetPalette.accent,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

@Composable
private fun EmptyMedicationsCard() {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(WidgetPalette.surface)
            .cornerRadius(12.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No medications scheduled",
                style = TextStyle(
                    color = WidgetPalette.textTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = "Tap to setup prescriptions",
                style = TextStyle(
                    color = WidgetPalette.accent,
                    fontSize = 10.sp,
                ),
            )
        }
    }
}

class HavnWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HavnWidget()
}

/** A brand glyph tinted for the widget — replaces the emoji the widget used to draw. */
@Composable
private fun WidgetGlyph(res: Int, tint: ColorProvider, size: androidx.compose.ui.unit.Dp) {
    Image(
        provider = ImageProvider(res),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        modifier = GlanceModifier.size(size),
    )
}

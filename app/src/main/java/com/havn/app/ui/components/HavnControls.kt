package com.havn.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import java.time.LocalTime

/**
 * A labelled switch row.
 *
 * The whole row is the target for the switch, but [onRowClick] lets a row also
 * open something (a time picker, say) — in which case the switch keeps its own
 * hit area and the rest of the row does the other thing.
 */
@Composable
fun HavnSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onRowClick: (() -> Unit)? = null,
) {
    val colors = HavnTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HavnTheme.radius.sm))
            .then(
                if (onRowClick != null && enabled) {
                    Modifier.havnPress(scaleDown = 0.995f, role = null, onClick = onRowClick)
                } else Modifier
            )
            .padding(vertical = HavnTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) colors.textPrimary else colors.textDisabled,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
        Spacer(Modifier.width(HavnTheme.spacing.lg))
        HavnSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            contentDescription = title,
        )
    }
}

/**
 * The house switch.
 *
 * Material's switch carries an icon inside the thumb and a heavy outline; this
 * one is a plain track and thumb so it reads as a physical toggle rather than a
 * control with a glyph in it.
 */
@Composable
fun HavnSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    val colors = HavnTheme.colors

    val track by animateColorAsState(
        targetValue = when {
            !enabled -> colors.surfaceSunken
            checked -> colors.accent
            else -> colors.hairlineStrong
        },
        animationSpec = HavnMotion.standard(),
        label = "switchTrack",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = HavnMotion.settle(),
        label = "switchThumb",
    )

    Box(
        modifier = modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(CircleShape)
            .background(track)
            .havnPress(
                scaleDown = 0.94f,
                enabled = enabled,
                role = Role.Switch,
                onClickLabel = contentDescription,
                onClick = { onCheckedChange(!checked) },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(if (checked) colors.onAccent else colors.surfaceRaised)
        )
    }
}

/**
 * A time picker in a Hävn-styled dialog.
 *
 * This exists because medication times were previously typed into a free-text
 * field. Anything that did not parse as "HH:mm" was accepted by the form,
 * stored, and then silently dropped by the scheduler — so a typo produced a
 * medication that looked scheduled and never reminded anyone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HavnTimePickerDialog(
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    title: String = "Pick a time",
) {
    val colors = HavnTheme.colors
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = false,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        shape = RoundedCornerShape(HavnTheme.radius.xl),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
        },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = colors.surfaceSunken,
                    clockDialSelectedContentColor = colors.onAccent,
                    clockDialUnselectedContentColor = colors.textSecondary,
                    selectorColor = colors.accent,
                    containerColor = colors.surfaceRaised,
                    periodSelectorBorderColor = colors.hairlineStrong,
                    periodSelectorSelectedContainerColor = colors.accentSoft,
                    periodSelectorUnselectedContainerColor = Color.Transparent,
                    periodSelectorSelectedContentColor = colors.onAccentSoft,
                    periodSelectorUnselectedContentColor = colors.textTertiary,
                    timeSelectorSelectedContainerColor = colors.accentSoft,
                    timeSelectorUnselectedContainerColor = colors.surfaceSunken,
                    timeSelectorSelectedContentColor = colors.onAccentSoft,
                    timeSelectorUnselectedContentColor = colors.textSecondary,
                ),
            )
        },
        confirmButton = {
            HavnButton(
                text = "Set",
                onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) },
                size = HavnButtonSize.Medium,
                fillWidth = false,
            )
        },
        dismissButton = {
            HavnButton(
                text = "Cancel",
                onClick = onDismiss,
                tone = HavnButtonTone.Ghost,
                size = HavnButtonSize.Medium,
                fillWidth = false,
            )
        },
    )
}

/** A confirmation dialog in the app's own voice. */
@Composable
fun HavnConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    val colors = HavnTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        shape = RoundedCornerShape(HavnTheme.radius.xl),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
        },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        },
        confirmButton = {
            HavnButton(
                text = confirmLabel,
                onClick = onConfirm,
                tone = if (destructive) HavnButtonTone.Danger else HavnButtonTone.Primary,
                size = HavnButtonSize.Medium,
                fillWidth = false,
            )
        },
        dismissButton = {
            HavnButton(
                text = "Cancel",
                onClick = onDismiss,
                tone = HavnButtonTone.Ghost,
                size = HavnButtonSize.Medium,
                fillWidth = false,
            )
        },
    )
}

/** A settings row that navigates or cycles a value. */
@Composable
fun HavnListRow(
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    subtitle: String? = null,
    destructive: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = HavnTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HavnTheme.radius.sm))
            .then(
                if (onClick != null) {
                    Modifier.havnPress(scaleDown = 0.995f, onClick = onClick)
                } else Modifier
            )
            .padding(vertical = HavnTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (destructive) colors.danger else colors.textPrimary,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
        if (value != null) {
            Spacer(Modifier.width(HavnTheme.spacing.md))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onClick != null) {
            Spacer(Modifier.width(HavnTheme.spacing.sm))
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textDisabled,
            )
        }
    }
}

/** A hairline divider used between rows in a list. */
@Composable
fun HavnDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(HavnTheme.colors.hairline)
    )
}

/** A small tracked section heading. */
@Composable
fun HavnSectionHeading(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text.uppercase(),
            style = com.havn.app.ui.theme.HavnType.Eyebrow,
            color = HavnTheme.colors.textTertiary,
        )
        trailing?.invoke()
    }
}

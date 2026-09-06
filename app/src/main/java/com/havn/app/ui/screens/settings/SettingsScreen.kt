package com.havn.app.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.domain.model.User
import com.havn.app.ui.theme.*

@Composable
fun SettingsScreen(
    onNavigateToOnboarding: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddUserSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = Charcoal,
        )
        Spacer(Modifier.height(32.dp))

        // ── PROFILES ────────────────────────────────────────────
        SectionHeader("PROFILES")
        Spacer(Modifier.height(10.dp))

        uiState.users.forEach { user ->
            val isActive = user.id == uiState.activeUserId
            UserProfileCard(
                user = user,
                isActive = isActive,
                onSelect = { viewModel.switchUser(user.id) },
                onDelete = { showDeleteConfirm = user },
            )
            Spacer(Modifier.height(8.dp))
        }

        // Add user
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(White)
                .border(1.dp, SurfaceHighest, RoundedCornerShape(14.dp))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    onNavigateToOnboarding()
                }
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SagePale)
                        .border(1.dp, Sage.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", color = Sage, fontSize = 18.sp, fontWeight = FontWeight.Light)
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    text = "Add profile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalMid,
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── REMINDERS ─────────────────────────────────────────
        SectionHeader("REMINDERS")
        Spacer(Modifier.height(10.dp))
        SettingsCard {
            SettingsRow(
                label = "Sound",
                value = uiState.reminderSound.lowercase().replaceFirstChar { it.uppercase() },
                onClick = {
                    val next = when (uiState.reminderSound) {
                        "CHIME" -> "MARIMBA"
                        "MARIMBA" -> "SILENT"
                        else -> "CHIME"
                    }
                    viewModel.setReminderSound(next)
                },
            )
            HorizontalDivider(color = SurfaceHighest, thickness = 0.5.dp)
            SettingsToggle(
                label = "Vibration",
                checked = uiState.reminderVibration,
                onCheckedChange = { viewModel.setVibration(it) },
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── APPEARANCE ───────────────────────────────────────
        SectionHeader("APPEARANCE")
        Spacer(Modifier.height(10.dp))
        SettingsCard {
            SettingsRow(
                label = "Theme",
                value = uiState.theme.lowercase().replaceFirstChar { it.uppercase() },
                onClick = {
                    val next = when (uiState.theme) {
                        "LIGHT" -> "DARK"
                        "DARK" -> "SYSTEM"
                        else -> "LIGHT"
                    }
                    viewModel.setTheme(next)
                },
            )
            HorizontalDivider(color = SurfaceHighest, thickness = 0.5.dp)
            SettingsRow(
                label = "App icon",
                value = "Default",
                onClick = {},
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── GENERAL ──────────────────────────────────────────
        SectionHeader("GENERAL")
        Spacer(Modifier.height(10.dp))
        SettingsCard {
            SettingsRow(label = "Backup (Local)", value = "", onClick = {})
            HorizontalDivider(color = SurfaceHighest, thickness = 0.5.dp)
            SettingsRow(label = "About H\u00e4vn", value = "v1.0", onClick = {})
        }

        Spacer(Modifier.height(48.dp))
    }

    // Delete confirm dialog
    showDeleteConfirm?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = {
                Text("Remove ${user.name}?", style = MaterialTheme.typography.titleMedium, color = Charcoal)
            },
            text = {
                Text(
                    "This will delete all of ${user.name}'s medication data. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalMid,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUser(user)
                    showDeleteConfirm = null
                }) {
                    Text("Remove", color = Terracotta, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel", color = StoneGrey)
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(20.dp),
        )
    }
}

@Composable
private fun UserProfileCard(user: User, isActive: Boolean, onSelect: () -> Unit, onDelete: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) Sage else SurfaceHighest,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "border",
    )
    val color = runCatching { Color(android.graphics.Color.parseColor(user.avatarColor)) }.getOrDefault(Sage)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(White)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onSelect)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "H",
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Charcoal,
            )
            if (user.age > 0) {
                Text(
                    text = "Age ${user.age}",
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGrey,
                )
            }
        }
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Sage),
                contentAlignment = Alignment.Center,
            ) {
                Text("\u2713", color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SurfaceHigh)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDelete,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("\u00d7", color = StoneGrey, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = StoneGrey,
        letterSpacing = 0.12.sp,
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Sage.copy(alpha = 0.08f),
                spotColor = Sage.copy(alpha = 0.05f),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .border(1.dp, SurfaceHighest, RoundedCornerShape(16.dp)),
        content = content,
    )
}

@Composable
private fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Charcoal)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = StoneGrey)
            Spacer(Modifier.width(4.dp))
            Text("\u203A", color = StoneLight, fontSize = 18.sp)
        }
    }
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Charcoal)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = Sage,
                uncheckedThumbColor = White,
                uncheckedTrackColor = SurfaceHigh,
            ),
        )
    }
}

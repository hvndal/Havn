package com.havn.app.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import android.net.Uri
import com.havn.app.data.backup.BackupManager
import com.havn.app.domain.model.User
import com.havn.app.ui.components.HavnBrandLogo
import com.havn.app.ui.components.HavnButton
import com.havn.app.ui.components.HavnButtonSize
import com.havn.app.ui.components.HavnButtonTone
import com.havn.app.ui.components.HavnConfirmDialog
import com.havn.app.ui.components.HavnDivider
import com.havn.app.ui.components.HavnListRow
import com.havn.app.ui.components.HavnPulseDots
import com.havn.app.ui.components.HavnSegmented
import com.havn.app.ui.components.HavnSwitchRow
import com.havn.app.ui.components.HavnTextField
import com.havn.app.ui.components.havnPress
import com.havn.app.ui.screens.onboarding.AVATAR_COLORS
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType
import com.havn.app.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    onManageMedications: () -> Unit,
    onOpenReminders: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HavnTheme.colors
    val gutter = HavnTheme.spacing.gutter
    val uriHandler = LocalUriHandler.current

    var showAddProfile by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<User?>(null) }
    var confirmSignOut by remember { mutableStateOf(false) }
    var confirmClearHistory by remember { mutableStateOf(false) }
    var pendingRestore by remember { mutableStateOf<Uri?>(null) }
    var banner by remember { mutableStateOf<BackupEvent.Message?>(null) }

    val context = LocalContext.current
    val busy by viewModel.busy.collectAsStateWithLifecycle()

    // CreateDocument lets the user place the file themselves — Drive, Downloads,
    // an SD card — without Hävn needing any storage permission.
    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BackupManager.MIME_TYPE)
    ) { uri -> uri?.let(viewModel::saveBackupTo) }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { pendingRestore = it } }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BackupEvent.Share -> runCatching { context.startActivity(event.intent) }
                    .onFailure {
                        banner = BackupEvent.Message("No app available to receive the file.", true)
                    }
                is BackupEvent.Message -> banner = event
            }
        }
    }

    // The banner clears itself; a result the user has read should not need
    // dismissing, and a stale "Restored 3 profiles" sitting there forever
    // reads as a bug.
    LaunchedEffect(banner) {
        if (banner != null) {
            delay(5000)
            banner = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
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
                        text = uiState.activeUser?.name?.uppercase() ?: "PROFILE",
                        style = HavnType.Eyebrow,
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.sm))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.textPrimary,
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.section))
            }

            // ── Appearance ───────────────────────────────────────────────────
            item(key = "appearance") {
                Column(Modifier.padding(horizontal = gutter)) {
                    Text("APPEARANCE", style = HavnType.Eyebrow, color = colors.textTertiary)
                    Spacer(Modifier.height(HavnTheme.spacing.lg))
                    // A three-way control rather than a row that cycles through
                    // Light → Dark → System on tap. Cycling hid two of the three
                    // options and gave no sense of what came next.
                    HavnSegmented(
                        options = listOf("Light", "Dark", "System"),
                        selectedIndex = when (uiState.theme) {
                            ThemeMode.LIGHT -> 0
                            ThemeMode.DARK -> 1
                            ThemeMode.SYSTEM -> 2
                        },
                        onSelect = { index ->
                            viewModel.setTheme(
                                when (index) {
                                    0 -> ThemeMode.LIGHT
                                    1 -> ThemeMode.DARK
                                    else -> ThemeMode.SYSTEM
                                }
                            )
                        },
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.md))
                    HavnSwitchRow(
                        title = "Interface sounds",
                        subtitle = "Soft feedback when you log a dose",
                        checked = uiState.interfaceSound,
                        onCheckedChange = viewModel::setInterfaceSound,
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.section))
            }

            // ── Medications & reminders ──────────────────────────────────────
            item(key = "meds") {
                Column(Modifier.padding(horizontal = gutter)) {
                    Text("YOUR ROUTINE", style = HavnType.Eyebrow, color = colors.textTertiary)
                    Spacer(Modifier.height(HavnTheme.spacing.sm))
                    HavnListRow(
                        title = "Medications",
                        subtitle = "Add, edit, pause or remove",
                        onClick = onManageMedications,
                    )
                    HavnDivider()
                    HavnListRow(
                        title = "Reminders",
                        subtitle = "Schedule, permissions and alert style",
                        onClick = onOpenReminders,
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.section))
            }

            // ── Profiles ─────────────────────────────────────────────────────
            item(key = "profiles-header") {
                Column(Modifier.padding(horizontal = gutter)) {
                    Text("PROFILES", style = HavnType.Eyebrow, color = colors.textTertiary)
                    Spacer(Modifier.height(HavnTheme.spacing.sm))
                    Text(
                        text = "Each profile keeps its own medications and history, " +
                            "all on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.lg))
                }
            }

            items(uiState.profiles, key = { it.id }) { profile ->
                ProfileRow(
                    profile = profile,
                    isActive = profile.id == uiState.activeUser?.id,
                    onSelect = { viewModel.switchProfile(profile.id) },
                    onDelete = { pendingDelete = profile },
                    modifier = Modifier.padding(horizontal = gutter),
                )
            }

            item(key = "add-profile") {
                Spacer(Modifier.height(HavnTheme.spacing.md))
                HavnButton(
                    text = "Add a profile",
                    onClick = { showAddProfile = true },
                    tone = HavnButtonTone.Secondary,
                    size = HavnButtonSize.Medium,
                    modifier = Modifier.padding(horizontal = gutter),
                )
                Spacer(Modifier.height(HavnTheme.spacing.md))
                HavnButton(
                    text = "Sign out",
                    onClick = { confirmSignOut = true },
                    tone = HavnButtonTone.Ghost,
                    size = HavnButtonSize.Medium,
                    modifier = Modifier.padding(horizontal = gutter),
                )
                Spacer(Modifier.height(HavnTheme.spacing.section))
            }

            // ── Data ─────────────────────────────────────────────────────────
            item(key = "data") {
                Column(Modifier.padding(horizontal = gutter)) {
                    Text("YOUR DATA", style = HavnType.Eyebrow, color = colors.textTertiary)
                    Spacer(Modifier.height(HavnTheme.spacing.sm))
                    Text(
                        text = "Hävn has no account and no servers — it doesn't ask for " +
                            "internet access at all. Android's own backup can restore your " +
                            "history to a new phone; a file you keep yourself is the copy " +
                            "nothing can take away.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.lg))

                    HavnListRow(
                        title = "Back up to Drive, Files or email",
                        subtitle = "Sends a single backup file to whichever app you choose",
                        onClick = viewModel::exportBackup,
                    )
                    HavnDivider()
                    HavnListRow(
                        title = "Save a copy to this device",
                        subtitle = "Pick a folder and write the backup straight there",
                        onClick = { saveLauncher.launch(viewModel.suggestedFileName()) },
                    )
                    HavnDivider()
                    HavnListRow(
                        title = "Restore from a backup",
                        subtitle = "Adds the backup's profiles alongside what's already here",
                        onClick = { openLauncher.launch(BackupManager.IMPORT_MIME_TYPES) },
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.section))
            }

            item(key = "maintenance") {
                Column(Modifier.padding(horizontal = gutter)) {
                    Text("MAINTENANCE", style = HavnType.Eyebrow, color = colors.textTertiary)
                    Spacer(Modifier.height(HavnTheme.spacing.sm))
                    HavnListRow(
                        title = "Fill in sample history",
                        subtitle = "Writes 30 days of example doses so Progress has " +
                            "something to show",
                        onClick = viewModel::seedSampleData,
                    )
                    HavnDivider()
                    HavnListRow(
                        title = "Clear dose history",
                        subtitle = "Keeps your medications, removes every recorded dose",
                        destructive = true,
                        onClick = { confirmClearHistory = true },
                    )
                }
                Spacer(Modifier.height(HavnTheme.spacing.section))
            }

            // ── About ────────────────────────────────────────────────────────
            item(key = "about") {
                Column(Modifier.padding(horizontal = gutter)) {
                    Text("ABOUT", style = HavnType.Eyebrow, color = colors.textTertiary)
                    Spacer(Modifier.height(HavnTheme.spacing.sm))
                    HavnListRow(
                        title = "Support Hävn",
                        subtitle = "Buy the developer a coffee",
                        onClick = {
                            runCatching {
                                uriHandler.openUri("https://buymeacoffee.com/hermanify")
                            }
                        },
                    )
                    HavnDivider()
                    HavnListRow(title = "Version", value = "1.0.1")
                }

                Spacer(Modifier.height(HavnTheme.spacing.section))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    HavnBrandLogo(
                        iconSize = 44.dp,
                        showWordmark = false,
                        showTagline = false,
                    )
                    Spacer(Modifier.height(HavnTheme.spacing.md))
                    Text(
                        text = "No account. No servers. No tracking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                    )
                }
            }
        }

        // Result banner, anchored to the bottom above the tab bar so it never
        // displaces the list the user is reading.
        AnimatedVisibility(
            visible = banner != null || busy,
            enter = fadeIn(HavnMotion.standard()) +
                slideInVertically(tween(HavnMotion.Standard, easing = HavnMotion.Enter)) { it / 2 },
            exit = fadeOut(HavnMotion.exit()),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = contentPadding.calculateBottomPadding() + HavnTheme.spacing.lg)
                .padding(horizontal = gutter),
        ) {
            val message = banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(HavnTheme.radius.md))
                    .background(
                        when {
                            message?.isError == true -> colors.dangerSoft
                            message != null -> colors.accentSoft
                            else -> colors.surfaceRaised
                        }
                    )
                    .padding(HavnTheme.spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (message == null) {
                    HavnPulseDots(color = colors.textSecondary)
                    Spacer(Modifier.width(HavnTheme.spacing.md))
                    Text(
                        text = "Working…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                } else {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.isError) colors.onDangerSoft else colors.onAccentSoft,
                    )
                }
            }
        }
    }

    if (showAddProfile) {
        AddProfileDialog(
            onDismiss = { showAddProfile = false },
            onCreate = { name, age, color ->
                viewModel.createProfile(name, age, color)
                showAddProfile = false
            },
        )
    }

    pendingDelete?.let { profile ->
        val isLast = uiState.profiles.size == 1
        HavnConfirmDialog(
            title = "Delete ${profile.name}?",
            body = if (isLast) {
                "This removes the only profile on this device, along with every " +
                    "medication and recorded dose. You'll be signed out."
            } else {
                "This permanently removes ${profile.name}'s medications and dose " +
                    "history. It cannot be undone."
            },
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                viewModel.deleteProfile(profile)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }

    if (confirmSignOut) {
        HavnConfirmDialog(
            title = "Sign out?",
            body = "Your medications and history stay on this device. Reminders " +
                "pause until you sign back in.",
            confirmLabel = "Sign out",
            onConfirm = {
                viewModel.signOut()
                confirmSignOut = false
            },
            onDismiss = { confirmSignOut = false },
        )
    }

    pendingRestore?.let { uri ->
        HavnConfirmDialog(
            title = "Restore this backup?",
            body = "Its profiles and history are added alongside what's already on this " +
                "device — nothing currently here is removed or overwritten.",
            confirmLabel = "Restore",
            onConfirm = {
                viewModel.restoreBackup(uri)
                pendingRestore = null
            },
            onDismiss = { pendingRestore = null },
        )
    }

    if (confirmClearHistory) {
        HavnConfirmDialog(
            title = "Clear dose history?",
            body = "Every recorded dose is removed and your adherence resets to zero. " +
                "Your medications are kept.",
            confirmLabel = "Clear",
            destructive = true,
            onConfirm = {
                viewModel.clearHistory()
                confirmClearHistory = false
            },
            onDismiss = { confirmClearHistory = false },
        )
    }
}

@Composable
private fun ProfileRow(
    profile: User,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val tint = remember(profile.avatarColor) {
        runCatching { Color(android.graphics.Color.parseColor(profile.avatarColor)) }
            .getOrDefault(Color(0xFF516351))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .havnPress(scaleDown = 0.99f, enabled = !isActive, onClick = onSelect)
            .padding(vertical = HavnTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = if (colors.isDark) 0.3f else 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = profile.name.firstOrNull()?.uppercaseChar()?.toString() ?: "·",
                style = MaterialTheme.typography.labelLarge,
                color = tint,
            )
        }
        Spacer(Modifier.width(HavnTheme.spacing.lg))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = when {
                    isActive -> "Signed in"
                    profile.age > 0 -> "Age ${profile.age}"
                    else -> "Tap to switch"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) colors.accent else colors.textTertiary,
            )
        }
        HavnButton(
            text = "Delete",
            onClick = onDelete,
            tone = HavnButtonTone.Ghost,
            size = HavnButtonSize.Small,
            fillWidth = false,
        )
    }
}

@Composable
private fun AddProfileDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int?, String) -> Unit,
) {
    val colors = HavnTheme.colors
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(AVATAR_COLORS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(HavnTheme.radius.xl),
        title = {
            Text(
                text = "Add a profile",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
        },
        text = {
            Column(modifier = Modifier.imePadding()) {
                HavnTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    placeholder = "Who is this for?",
                    imeAction = ImeAction.Next,
                )
                Spacer(Modifier.height(HavnTheme.spacing.lg))
                HavnTextField(
                    value = age,
                    onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) age = it },
                    label = "Age",
                    placeholder = "Optional",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                )
                Spacer(Modifier.height(HavnTheme.spacing.lg))
                Text("COLOUR", style = HavnType.Eyebrow, color = colors.textTertiary)
                Spacer(Modifier.height(HavnTheme.spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(HavnTheme.spacing.md)) {
                    AVATAR_COLORS.forEach { hex ->
                        val tint = runCatching {
                            Color(android.graphics.Color.parseColor(hex))
                        }.getOrDefault(Color(0xFF516351))
                        Box(
                            modifier = Modifier
                                .size(if (hex == color) 34.dp else 28.dp)
                                .clip(CircleShape)
                                .background(tint)
                                .havnPress(scaleDown = 0.9f) { color = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            HavnButton(
                text = "Create",
                onClick = { onCreate(name, age.toIntOrNull(), color) },
                enabled = name.isNotBlank(),
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

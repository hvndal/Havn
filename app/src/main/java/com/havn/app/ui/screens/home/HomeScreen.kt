package com.havn.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.R
import com.havn.app.audio.SoundManager
import com.havn.app.domain.model.*
import com.havn.app.ui.components.HavnOrganizerView
import com.havn.app.ui.components.MedIcon
import com.havn.app.ui.components.OrganizerWeekMapper
import com.havn.app.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onAddMedication: () -> Unit,
    onOpenOrganizer: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Smooth Slow 2.5s Luxury Fade-In Animation
    val fadeAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.97f) }

    LaunchedEffect(Unit) {
        fadeAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = EaseOutCubic)
        )
    }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = EaseOutQuart)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .alpha(fadeAnim.value)
            .scale(scaleAnim.value)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            // ── Top App Bar ──────────────────────────────────────
            item {
                HomeTopBar(user = uiState.user)
            }

            // ── Greeting ─────────────────────────────────────────
            item {
                GreetingSection(
                    name = uiState.user?.name ?: "",
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(20.dp))
            }

            // ── 3D Organizer (WebView hero) ───────────────────────
            item {
                val todayDow = LocalDate.now().dayOfWeek.value - 1
                OrganizerHeroSection(
                    weekDataJson = OrganizerWeekMapper.buildWeekDataJson(uiState.allMeds),
                    selectedDay = todayDow,
                    onTap = onOpenOrganizer,
                    soundManager = viewModel.soundManager,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(32.dp))
            }

            // ── Today Section Header ──────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.titleMedium,
                            color = Charcoal,
                        )
                        val count = uiState.todayMeds.size
                        Text(
                            text = if (count == 0) "Nothing waiting today." else "$count medication${if (count > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = StoneGrey,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    AddMedButton(onClick = onAddMedication)
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Medication List ───────────────────────────────────
            if (uiState.todayMeds.isEmpty()) {
                item {
                    EmptyTodayState(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                    )
                }
            } else {
                items(uiState.todayMeds, key = { it.medication.id }) { todayMed ->
                    MedicationCard(
                        todayMed = todayMed,
                        onToggle = { viewModel.toggleMedication(todayMed.medication) },
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(user: User?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val color = user?.avatarColor?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrDefault(Sage)
        } ?: Sage

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.25f))
                .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "H",
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = "Hävn",
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 32.sp,
                letterSpacing = (-1).sp,
            ),
            color = Charcoal,
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_notification),
                contentDescription = "Notifications",
                tint = CharcoalMid,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun GreetingSection(name: String, modifier: Modifier = Modifier) {
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else       -> "Good evening"
    }
    Column(modifier = modifier) {
        Text(
            text = (greeting + ",").uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = StoneGrey,
            letterSpacing = 1.2.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = name.ifBlank { "there" },
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
            color = Charcoal,
        )
    }
}

@Composable
private fun OrganizerHeroSection(
    weekDataJson: String,
    selectedDay: Int,
    onTap: () -> Unit,
    soundManager: SoundManager,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Sage.copy(alpha = 0.10f),
                spotColor = Sage.copy(alpha = 0.08f),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLow)
            .border(1.dp, SurfaceHighest, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
    ) {
        HavnOrganizerView(
            weekDataJson = weekDataJson,
            selectedDay = selectedDay,
            modifier = Modifier.fillMaxSize(),
            onSlotTapped = { soundManager.playCeramicClick() },
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(White.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Weekly organizer",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGrey,
            )
        }
    }
}

@Composable
private fun AddMedButton(onClick: () -> Unit) {
    val pressed = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed.value) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f),
        label = "addBtnScale",
    )
    Box(
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Sage)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = "Add medication",
            tint = White,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun MedicationCard(
    todayMed: TodayMedication,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isTaken = todayMed.doseLog?.status == DoseStatus.TAKEN
    val bgColor by animateColorAsState(
        targetValue = if (isTaken) SagePale.copy(alpha = 0.4f) else White,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardBg",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (isTaken) 0.55f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "alpha",
    )
    val checkScale by animateFloatAsState(
        targetValue = if (isTaken) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "checkScale",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Sage.copy(alpha = 0.08f),
                spotColor = Sage.copy(alpha = 0.06f),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, SurfaceHighest, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val swatchColor = medIconColor(todayMed.medication.colorTag)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(swatchColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            MedIcon(
                type = todayMed.medication.iconType,
                size = 22.dp,
                tint = swatchColor,
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = todayMed.medication.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Charcoal.copy(alpha = contentAlpha),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = todayMed.medication.dosage,
                style = MaterialTheme.typography.labelMedium,
                color = StoneGrey.copy(alpha = contentAlpha),
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTime(todayMed.scheduledTime),
                style = MaterialTheme.typography.labelMedium,
                color = CharcoalMid.copy(alpha = contentAlpha),
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, StoneLight, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(checkScale)
                        .clip(CircleShape)
                        .background(Sage),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTodayState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_leaf),
            contentDescription = null,
            tint = SageLight,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Nothing waiting today.",
            style = MaterialTheme.typography.titleMedium,
            color = Charcoal,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Enjoy your morning.",
            style = MaterialTheme.typography.bodyMedium,
            color = StoneGrey,
        )
    }
}

private fun medIconColor(tag: String): Color = when (tag) {
    "sage"       -> Sage
    "terracotta" -> Terracotta
    "butter"     -> ButterAmber
    "slate"      -> Color(0xFF9BAEB5)
    "sand"       -> Color(0xFFBEB09A)
    else         -> Sage
}

private fun formatTime(t: String): String {
    return try {
        val parsed = java.time.LocalTime.parse(t, DateTimeFormatter.ofPattern("HH:mm"))
        parsed.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)).lowercase()
    } catch (e: Exception) { t }
}

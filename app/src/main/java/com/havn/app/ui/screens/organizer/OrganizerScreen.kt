package com.havn.app.ui.screens.organizer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.R
import com.havn.app.domain.model.Medication
import com.havn.app.ui.components.HavnOrganizerView
import com.havn.app.ui.components.HavnSkeletonBox
import com.havn.app.ui.components.MedIcon
import com.havn.app.ui.components.OrganizerWeekMapper
import com.havn.app.ui.components.pressScale
import com.havn.app.ui.theme.*
import java.time.LocalDate

@Composable
fun OrganizerScreen(
    onBack: () -> Unit,
    viewModel: OrganizerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val fadeAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.97f) }

    LaunchedEffect(Unit) {
        fadeAnim.animateTo(1f, animationSpec = tween(1200, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, animationSpec = tween(1000, easing = EaseOutQuart))
    }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val todayDow = LocalDate.now().dayOfWeek.value - 1
    var selectedDay by remember { mutableIntStateOf(uiState.selectedDayIndex) }
    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.selectedDayIndex) {
        selectedDay = uiState.selectedDayIndex
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .systemBarsPadding()
            .alpha(fadeAnim.value)
            .scale(scaleAnim.value)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val backInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .pressScale(targetScale = 0.90f, interactionSource = backInteraction)
                    .clip(CircleShape)
                    .background(SurfaceHigh)
                    .clickable(indication = null, interactionSource = backInteraction) {
                        viewModel.soundManager.playSoftTap()
                        onBack()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = CharcoalMid,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Organizer",
                    style = MaterialTheme.typography.titleMedium,
                    color = Charcoal,
                )
                Text(
                    text = "Your weekly ritual",
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGrey,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 24.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Sage.copy(alpha = 0.10f),
                    spotColor = Sage.copy(alpha = 0.08f),
                )
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceLow)
                .border(1.dp, SurfaceHighest, RoundedCornerShape(24.dp)),
        ) {
            if (!isLoaded) {
                HavnSkeletonBox(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp)
                )
            }

            HavnOrganizerView(
                weekDataJson = uiState.weekDataJson,
                selectedDay = selectedDay,
                modifier = Modifier.fillMaxSize(),
                onSlotTapped = { slot ->
                    selectedDay = slot
                    viewModel.selectDay(slot)
                    viewModel.onSlotTapped(slot)
                },
                onPageLoaded = { isLoaded = true }
            )
        }

        Spacer(Modifier.height(20.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(days.indices.toList()) { i ->
                DayChip(
                    label = days[i],
                    isToday = i == todayDow,
                    isSelected = i == selectedDay,
                    medCount = OrganizerWeekMapper.medsForDay(uiState.allMeds, i).size,
                    onClick = {
                        selectedDay = i
                        viewModel.selectDay(i)
                        viewModel.soundManager.playCeramicClick()
                    },
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "${days[selectedDay]}'s ritual",
            style = MaterialTheme.typography.labelMedium,
            color = StoneGrey,
            modifier = Modifier.padding(horizontal = 24.dp),
            letterSpacing = 0.06.sp,
        )
        Spacer(Modifier.height(12.dp))

        AnimatedContent(
            targetState = selectedDay,
            transitionSpec = {
                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                 slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { if (targetState > initialState) it / 6 else -it / 6 }) togetherWith
                (fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                 slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { if (targetState > initialState) -it / 6 else it / 6 })
            },
            label = "dayRitualList"
        ) { dayIdx ->
            val dayMeds = OrganizerWeekMapper.medsForDay(uiState.allMeds, dayIdx)
            Column {
                if (dayMeds.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_leaf),
                            contentDescription = null,
                            tint = SageLight,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Nothing scheduled.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StoneGrey,
                        )
                    }
                } else {
                    dayMeds.forEach { med ->
                        OrganizerMedRow(
                            med = med,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun DayChip(
    label: String,
    isToday: Boolean,
    isSelected: Boolean,
    medCount: Int,
    onClick: () -> Unit,
) {
    val chipInteraction = remember { MutableInteractionSource() }
    val bg by animateColorAsState(
        targetValue = when {
            isSelected -> Sage
            isToday -> SagePale
            else -> SurfaceHigh
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chipBg"
    )
    val text by animateColorAsState(
        targetValue = when {
            isSelected -> White
            isToday -> SageDeep
            else -> CharcoalMid
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chipText"
    )
    val chipScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chipScale"
    )

    Column(
        modifier = Modifier
            .scale(chipScale)
            .pressScale(targetScale = 0.92f, interactionSource = chipInteraction)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(indication = null, interactionSource = chipInteraction, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = text,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
        )
        if (medCount > 0) {
            Text(
                text = "${medCount}",
                style = MaterialTheme.typography.labelSmall,
                color = text.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun OrganizerMedRow(med: Medication, modifier: Modifier = Modifier) {
    val swatch = OrganizerWeekMapper.colorTagToHex(med.colorTag)
    val swatchColor = runCatching {
        androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(swatch))
    }.getOrDefault(Sage)
    val rowInteraction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(targetScale = 0.98f, interactionSource = rowInteraction)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Sage.copy(alpha = 0.06f),
                spotColor = Sage.copy(alpha = 0.04f),
            )
            .clip(RoundedCornerShape(14.dp))
            .background(White)
            .border(1.dp, SurfaceHighest, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(swatchColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            MedIcon(type = med.iconType, size = 22.dp, tint = swatchColor)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = med.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Charcoal,
            )
            Text(
                text = med.dosage,
                style = MaterialTheme.typography.labelSmall,
                color = StoneGrey,
            )
        }
        Text(
            text = med.reminderTimes.firstOrNull() ?: "Any time",
            style = MaterialTheme.typography.labelMedium,
            color = CharcoalMid,
        )
    }
}

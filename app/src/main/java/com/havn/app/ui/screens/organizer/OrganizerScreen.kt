package com.havn.app.ui.screens.organizer

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.audio.SoundManager
import com.havn.app.domain.model.*
import com.havn.app.ui.theme.*
import java.time.LocalDate

@Composable
fun OrganizerScreen(
    onBack: () -> Unit,
    viewModel: OrganizerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var webView: WebView? by remember { mutableStateOf(null) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .systemBarsPadding()
            .alpha(fadeAnim.value)
            .scale(scaleAnim.value)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceHigh)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                        viewModel.soundManager.playSoftTap()
                        onBack()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "\u2190", color = CharcoalMid, fontSize = 16.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Organizer",
                    style = MaterialTheme.typography.titleMedium,
                    color = Charcoal,
                )
                Text(
                    text = "This week",
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGrey,
                )
            }
        }

        // 3D Organizer — full screen hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceLow)
                .border(1.dp, SurfaceHighest, RoundedCornerShape(24.dp)),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccessFromFileURLs = true
                        settings.allowUniversalAccessFromFileURLs = true
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        webViewClient = WebViewClient()
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onSlotTapped(slotIndex: Int, isOpen: Boolean) {
                                viewModel.onSlotTapped(slotIndex)
                            }
                        }, "AndroidOrganizer")
                        loadUrl("file:///android_asset/organizer/organizer.html")
                        webView = this
                    }
                },
            )
        }

        Spacer(Modifier.height(24.dp))

        // Day selector pills
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val todayDow = LocalDate.now().dayOfWeek.value - 1
        var selectedDay by remember { mutableIntStateOf(todayDow) }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(days.indices.toList()) { i ->
                DayChip(
                    label = days[i],
                    isToday = i == todayDow,
                    isSelected = i == selectedDay,
                    onClick = {
                        selectedDay = i
                        viewModel.selectDay(i)
                        viewModel.soundManager.playCeramicClick()
                        webView?.evaluateJavascript("HavnOrganizer.toggleSlot($i)", null)
                    },
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Selected day medication list
        Text(
            text = "${days[selectedDay]}'s ritual",
            style = MaterialTheme.typography.labelMedium,
            color = StoneGrey,
            modifier = Modifier.padding(horizontal = 24.dp),
            letterSpacing = 0.06.sp,
        )
        Spacer(Modifier.height(12.dp))

        if (uiState.selectedDayMeds.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nothing scheduled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StoneGrey,
                )
            }
        } else {
            uiState.selectedDayMeds.forEach { med ->
                OrganizerMedRow(
                    med = med,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun DayChip(label: String, isToday: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val bg = when {
        isSelected -> Sage
        isToday    -> SagePale
        else       -> SurfaceHigh
    }
    val text = when {
        isSelected -> White
        isToday    -> SageDeep
        else       -> CharcoalMid
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = text,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun OrganizerMedRow(med: Medication, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(White)
            .border(1.dp, SurfaceHighest, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Sage.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "💊", fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
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
        Spacer(Modifier.weight(1f))
        Text(
            text = med.reminderTimes.firstOrNull() ?: "Any time",
            style = MaterialTheme.typography.labelMedium,
            color = CharcoalMid,
        )
    }
}

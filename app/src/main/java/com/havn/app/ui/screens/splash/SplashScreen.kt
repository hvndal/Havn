package com.havn.app.ui.screens.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.havn.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val startAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.88f) }

    LaunchedEffect(Unit) {
        // Stagger: fade in + scale up with spring
        kotlinx.coroutines.launch {
            startAnim.animateTo(1f, animationSpec = tween(800, easing = EaseOut))
        }
        scaleAnim.animateTo(1f, animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 200f,
        ))
        delay(1000)
        // Check if onboarding needed
        if (viewModel.needsOnboarding()) {
            onNavigateToOnboarding()
        } else {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentAlignment = Alignment.Center,
    ) {
        // Warm shader background (canvas wave)
        HavnShaderBackground()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scaleAnim.value)
                .alpha(startAnim.value),
        ) {
            Text(
                text = "H\u00e4vn",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-1.5).sp,
                    fontSize = 64.sp,
                ),
                color = Charcoal,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Pill organizer & reminders.",
                style = MaterialTheme.typography.labelMedium,
                color = StoneGrey,
                letterSpacing = 0.05.sp,
            )
        }
    }
}

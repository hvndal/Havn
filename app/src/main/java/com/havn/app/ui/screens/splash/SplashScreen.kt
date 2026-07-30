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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.havn.app.ui.components.StaggeredTextReveal
import com.havn.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(800)
        taglineAlpha.animateTo(1f, animationSpec = tween(1200, easing = EaseOutCubic))
        delay(1200)
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
        HavnShaderBackground()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Editorial Staggered Letter Text Reveal Logo Animation
            StaggeredTextReveal(
                text = "Hävn",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-1.5).sp,
                    fontSize = 68.sp,
                ),
                color = Charcoal,
                letterDelayMs = 80L,
                initialDelayMs = 300L,
            )

            Spacer(Modifier.height(12.dp))

            // Subtitle Reveal
            Text(
                text = "Pill organizer & reminders.",
                style = MaterialTheme.typography.labelMedium,
                color = StoneGrey,
                letterSpacing = 0.08.sp,
                modifier = Modifier.alpha(taglineAlpha.value),
            )
        }
    }
}

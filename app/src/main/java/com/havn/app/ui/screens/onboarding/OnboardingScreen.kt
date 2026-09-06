package com.havn.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.domain.model.User
import com.havn.app.ui.components.StaggeredTextReveal
import com.havn.app.ui.screens.splash.HavnShaderBackground
import androidx.compose.ui.res.painterResource
import com.havn.app.R
import com.havn.app.ui.components.StaggeredFadeIn
import com.havn.app.ui.components.havnPressFeedback
import com.havn.app.ui.theme.*

val AVATAR_COLORS = listOf(
    "#8DA08C", "#B49759", "#C07050", "#9BAEB5", "#BEB09A", "#A89AAA",
)

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val existingUsers by viewModel.existingUsers.collectAsStateWithLifecycle()
    var page by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(AVATAR_COLORS[0]) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        HavnShaderBackground(modifier = Modifier.fillMaxSize())

        AnimatedContent(
            targetState = page,
            transitionSpec = {
                (fadeIn() + slideInHorizontally { it / 4 }) togetherWith
                (fadeOut() + slideOutHorizontally { -it / 4 })
            },
            label = "onboarding",
        ) { currentPage ->
            when (currentPage) {
                0 -> OnboardingPage1(
                    name = name,
                    age = age,
                    existingUsers = existingUsers,
                    onNameChange = { name = it },
                    onAgeChange = { age = it },
                    onContinue = { if (name.isNotBlank()) page = 1 },
                    onSelectUser = { user ->
                        viewModel.signInAsUser(user.id, onDone)
                    }
                )
                1 -> OnboardingPage2(
                    name = name,
                    selectedColor = selectedColor,
                    onColorSelect = { selectedColor = it },
                    onDone = {
                        viewModel.createUser(
                            name = name.trim(),
                            age = age.toIntOrNull() ?: 0,
                            color = selectedColor,
                            onComplete = onDone,
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage1(
    name: String,
    age: String,
    existingUsers: List<User>,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onContinue: () -> Unit,
    onSelectUser: (User) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Center,
    ) {
        // Onboarding Visual Illustration
        StaggeredFadeIn(index = 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.il_onboarding_ritual),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(130.dp),
                )
            }
        }

        // Text Reveal Animation for Brand / Welcome Title
        StaggeredTextReveal(
            text = "Welcome.",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp),
            color = Charcoal,
            letterDelayMs = 70L,
            initialDelayMs = 150L,
        )

        Spacer(Modifier.height(6.dp))
        Text(
            text = if (existingUsers.isNotEmpty()) "Sign in or create a profile" else "What's your name?",
            style = MaterialTheme.typography.headlineMedium,
            color = CharcoalMid,
        )
        Spacer(Modifier.height(36.dp))

        // Sign In Option (If existing local profiles exist)
        if (existingUsers.isNotEmpty()) {
            Text(
                text = "SIGN IN TO PROFILE",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGrey,
                letterSpacing = 0.12.sp,
            )
            Spacer(Modifier.height(12.dp))
            existingUsers.forEach { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(White)
                        .border(1.dp, SurfaceHighest, RoundedCornerShape(16.dp))
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            onSelectUser(user)
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val avatarColor = runCatching { Color(android.graphics.Color.parseColor(user.avatarColor)) }.getOrDefault(Sage)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(avatarColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "H",
                            style = MaterialTheme.typography.labelLarge,
                            color = avatarColor,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Charcoal,
                    )
                    Spacer(Modifier.weight(1f))
                    Text("Sign In →", style = MaterialTheme.typography.labelMedium, color = Sage)
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "OR CREATE NEW",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGrey,
                letterSpacing = 0.12.sp,
            )
            Spacer(Modifier.height(12.dp))
        }

        // Name input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(16.dp))
                .border(1.dp, SurfaceHighest, RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.titleMedium.copy(color = Charcoal, fontSize = 18.sp),
                cursorBrush = SolidColor(Sage),
                singleLine = true,
                decorationBox = { inner ->
                    if (name.isEmpty()) Text("Your name", style = MaterialTheme.typography.titleMedium.copy(color = StoneGrey, fontSize = 18.sp))
                    inner()
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        // Age input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(16.dp))
                .border(1.dp, SurfaceHighest, RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            BasicTextField(
                value = age,
                onValueChange = { if (it.length <= 3) onAgeChange(it) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium.copy(color = Charcoal, fontSize = 18.sp),
                cursorBrush = SolidColor(Sage),
                singleLine = true,
                decorationBox = { inner ->
                    if (age.isEmpty()) Text("Your age (optional)", style = MaterialTheme.typography.titleMedium.copy(color = StoneGrey, fontSize = 18.sp))
                    inner()
                }
            )
        }

        Spacer(Modifier.height(32.dp))

        HavnButton(text = "Continue", onClick = onContinue, enabled = name.isNotBlank())
    }
}

@Composable
private fun OnboardingPage2(
    name: String,
    selectedColor: String,
    onColorSelect: (String) -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Center,
    ) {
        StaggeredFadeIn(index = 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.il_onboarding_mindful),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(130.dp),
                )
            }
        }

        StaggeredTextReveal(
            text = "Hello, $name.",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
            color = Charcoal,
            letterDelayMs = 60L,
            initialDelayMs = 150L,
        )

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your organizer is ready.",
            style = MaterialTheme.typography.headlineMedium,
            color = CharcoalMid,
        )
        Spacer(Modifier.height(40.dp))

        Text(
            text = "CHOOSE YOUR COLOUR",
            style = MaterialTheme.typography.labelSmall,
            color = StoneGrey,
            letterSpacing = 0.12.sp,
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AVATAR_COLORS.forEach { hex ->
                val color = Color(android.graphics.Color.parseColor(hex))
                val isSelected = hex == selectedColor
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 44.dp else 36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(if (isSelected) 2.dp else 0.dp, Charcoal.copy(alpha = 0.4f), CircleShape)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onColorSelect(hex) },
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        HavnButton(text = "Begin", onClick = onDone)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Everything stays on your device. Local only.",
            style = MaterialTheme.typography.bodySmall,
            color = StoneLight,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun HavnButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.96f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale",
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(56.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Sage, contentColor = White,
            disabledContainerColor = SurfaceHigh, disabledContentColor = StoneGrey,
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.04.sp))
    }
}

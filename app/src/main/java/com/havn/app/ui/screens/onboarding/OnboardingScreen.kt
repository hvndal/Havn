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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.havn.app.ui.theme.*
import com.havn.app.ui.screens.splash.HavnShaderBackground

val AVATAR_COLORS = listOf(
    "#8DA08C", // sage
    "#B49759", // amber
    "#C07050", // terracotta
    "#9BAEB5", // slate
    "#BEB09A", // sand
    "#A89AAA", // mist
)

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
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
                    onNameChange = { name = it },
                    onAgeChange = { age = it },
                    onContinue = { if (name.isNotBlank()) page = 1 },
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
                        ) { onDone() }
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
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Hello.",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp),
            color = Charcoal,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "What's your name?",
            style = MaterialTheme.typography.headlineMedium,
            color = CharcoalMid,
        )
        Spacer(Modifier.height(48.dp))

        // Name field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(16.dp))
                .border(1.dp, SurfaceHighest, RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = Charcoal,
                    fontSize = 20.sp,
                ),
                cursorBrush = SolidColor(Sage),
                singleLine = true,
                decorationBox = { inner ->
                    if (name.isEmpty()) {
                        Text(
                            "Your name",
                            style = MaterialTheme.typography.titleMedium.copy(color = StoneGrey, fontSize = 20.sp),
                        )
                    }
                    inner()
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Age field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(16.dp))
                .border(1.dp, SurfaceHighest, RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            BasicTextField(
                value = age,
                onValueChange = { if (it.length <= 3) onAgeChange(it) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = Charcoal,
                    fontSize = 20.sp,
                ),
                cursorBrush = SolidColor(Sage),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                decorationBox = { inner ->
                    if (age.isEmpty()) {
                        Text(
                            "Your age (optional)",
                            style = MaterialTheme.typography.titleMedium.copy(color = StoneGrey, fontSize = 20.sp),
                        )
                    }
                    inner()
                }
            )
        }

        Spacer(Modifier.height(48.dp))

        // Continue button
        HavnButton(
            text = "Continue",
            onClick = onContinue,
            enabled = name.isNotBlank(),
        )
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
        Text(
            text = "Hello, $name.",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp),
            color = Charcoal,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your organizer is ready.",
            style = MaterialTheme.typography.headlineMedium,
            color = CharcoalMid,
        )
        Spacer(Modifier.height(48.dp))

        Text(
            text = "CHOOSE YOUR COLOUR",
            style = MaterialTheme.typography.labelSmall,
            color = StoneGrey,
            letterSpacing = 0.12.sp,
        )
        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AVATAR_COLORS.forEach { hex ->
                val color = Color(android.graphics.Color.parseColor(hex))
                val isSelected = hex == selectedColor
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 44.dp else 36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = Charcoal.copy(alpha = 0.4f),
                            shape = CircleShape,
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onColorSelect(hex) },
                )
            }
        }

        Spacer(Modifier.height(64.dp))

        HavnButton(text = "Begin", onClick = onDone)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Everything stays on your device. No account needed.",
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
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Sage,
            contentColor = White,
            disabledContainerColor = SurfaceHigh,
            disabledContentColor = StoneGrey,
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.04.sp),
        )
    }
}

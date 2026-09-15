package com.havn.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havn.app.domain.model.User
import com.havn.app.ui.components.HavnAmbientField
import com.havn.app.ui.components.HavnBrandLogo
import com.havn.app.ui.components.HavnButton
import com.havn.app.ui.components.HavnButtonSize
import com.havn.app.ui.components.HavnButtonTone
import com.havn.app.ui.components.HavnTextField
import com.havn.app.ui.components.HavnTextReveal
import com.havn.app.ui.components.havnPress
import com.havn.app.ui.components.havnReveal
import com.havn.app.ui.components.rememberRevealProgress
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType

/** The palette a profile can be identified by. */
val AVATAR_COLORS = listOf(
    "#516351", // sage
    "#9A5637", // clay
    "#8A6B22", // amber
    "#7E929A", // slate
    "#A89878", // sand
    "#6B5F7A", // plum
)

/**
 * Sign in, or create the first local profile.
 *
 * Structurally different from the previous version in one important way: every
 * page scrolls and respects the keyboard. Before, each page was a `Column` with
 * `verticalArrangement = Center` and no scroll container, so on a small phone —
 * or with three or more saved profiles, or simply with the keyboard open — the
 * name field and the Continue button were pushed off-screen with no way to
 * reach them.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HavnTheme.colors

    var page by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(AVATAR_COLORS.first()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        HavnAmbientField(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f),
        )

        AnimatedContent(
            targetState = page,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally(tween(HavnMotion.Considered, easing = HavnMotion.Enter)) { it / 5 } +
                        fadeIn(tween(HavnMotion.Standard))) togetherWith
                        (slideOutHorizontally(tween(HavnMotion.Standard, easing = HavnMotion.Exit)) { -it / 5 } +
                            fadeOut(tween(HavnMotion.Quick)))
                } else {
                    (slideInHorizontally(tween(HavnMotion.Considered, easing = HavnMotion.Enter)) { -it / 5 } +
                        fadeIn(tween(HavnMotion.Standard))) togetherWith
                        (slideOutHorizontally(tween(HavnMotion.Standard, easing = HavnMotion.Exit)) { it / 5 } +
                            fadeOut(tween(HavnMotion.Quick)))
                }
            },
            label = "onboardingPage",
        ) { currentPage ->
            when (currentPage) {
                0 -> IdentityPage(
                    name = name,
                    age = age,
                    profiles = uiState.existingProfiles,
                    isSubmitting = uiState.isSubmitting,
                    error = uiState.error,
                    onNameChange = { name = it },
                    onAgeChange = { input -> if (input.length <= 3 && input.all(Char::isDigit)) age = input },
                    onContinue = { if (name.isNotBlank()) page = 1 },
                    onSignIn = viewModel::signIn,
                )

                else -> ColourPage(
                    name = name,
                    selectedColor = selectedColor,
                    isSubmitting = uiState.isSubmitting,
                    onSelectColor = { selectedColor = it },
                    onBack = { page = 0 },
                    onFinish = {
                        viewModel.createProfile(
                            name = name,
                            age = age.toIntOrNull(),
                            avatarColor = selectedColor,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun IdentityPage(
    name: String,
    age: String,
    profiles: List<User>,
    isSubmitting: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onContinue: () -> Unit,
    onSignIn: (Long) -> Unit,
) {
    val colors = HavnTheme.colors
    val gutter = HavnTheme.spacing.gutter
    val reveal = rememberRevealProgress()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = gutter),
    ) {
        Spacer(Modifier.height(HavnTheme.spacing.xxxl))

        HavnBrandLogo(
            iconSize = 56.dp,
            showWordmark = false,
            showTagline = false,
            modifier = Modifier.havnReveal(reveal),
        )

        Spacer(Modifier.height(HavnTheme.spacing.xxl))

        HavnTextReveal(
            text = if (profiles.isEmpty()) "Welcome." else "Welcome back.",
            style = MaterialTheme.typography.displayMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(HavnTheme.spacing.md))
        Text(
            text = "No account, no cloud, no sign-up. Your medication history stays on this phone.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textTertiary,
            modifier = Modifier.havnReveal(rememberRevealProgress(delayMs = 200)),
        )

        Spacer(Modifier.height(HavnTheme.spacing.section))

        if (profiles.isNotEmpty()) {
            Text(
                text = "CONTINUE AS",
                style = HavnType.Eyebrow,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(HavnTheme.spacing.md))

            profiles.forEachIndexed { index, profile ->
                ProfileRow(
                    profile = profile,
                    index = index,
                    enabled = !isSubmitting,
                    onClick = { onSignIn(profile.id) },
                )
            }

            Spacer(Modifier.height(HavnTheme.spacing.xxl))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(colors.hairline)
                )
                Text(
                    text = "OR ADD SOMEONE",
                    style = HavnType.Eyebrow,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(horizontal = HavnTheme.spacing.md),
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(colors.hairline)
                )
            }
            Spacer(Modifier.height(HavnTheme.spacing.xxl))
        }

        HavnTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Name",
            placeholder = "Who is this for?",
            imeAction = ImeAction.Next,
            enabled = !isSubmitting,
        )

        Spacer(Modifier.height(HavnTheme.spacing.lg))

        HavnTextField(
            value = age,
            onValueChange = onAgeChange,
            label = "Age",
            placeholder = "Optional",
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            enabled = !isSubmitting,
        )

        if (error != null) {
            Spacer(Modifier.height(HavnTheme.spacing.lg))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = colors.danger,
            )
        }

        Spacer(Modifier.height(HavnTheme.spacing.xxl))

        HavnButton(
            text = "Continue",
            onClick = onContinue,
            enabled = name.isNotBlank() && !isSubmitting,
        )

        Spacer(Modifier.height(HavnTheme.spacing.xxxl))
    }
}

@Composable
private fun ProfileRow(
    profile: User,
    index: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = HavnTheme.colors
    val reveal = rememberRevealProgress(
        key = profile.id,
        delayMs = HavnMotion.staggerDelay(index, step = 60),
    )
    val tint = remember(profile.avatarColor) {
        runCatching { Color(android.graphics.Color.parseColor(profile.avatarColor)) }
            .getOrDefault(Color(0xFF516351))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .havnReveal(reveal)
            .clip(RoundedCornerShape(HavnTheme.radius.md))
            .background(colors.surface)
            .border(1.dp, colors.hairline, RoundedCornerShape(HavnTheme.radius.md))
            .havnPress(scaleDown = 0.985f, enabled = enabled, onClick = onClick)
            .padding(HavnTheme.spacing.lg),
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
            if (profile.age > 0) {
                Text(
                    text = "Age ${profile.age}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
        Text(
            text = "Open",
            style = MaterialTheme.typography.labelMedium,
            color = colors.accent,
        )
    }
    Spacer(Modifier.height(HavnTheme.spacing.sm))
}

@Composable
private fun ColourPage(
    name: String,
    selectedColor: String,
    isSubmitting: Boolean,
    onSelectColor: (String) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
) {
    val colors = HavnTheme.colors
    val gutter = HavnTheme.spacing.gutter

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = gutter),
    ) {
        Spacer(Modifier.height(HavnTheme.spacing.xxxl))

        Text(
            text = "STEP 2 OF 2",
            style = HavnType.Eyebrow,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(HavnTheme.spacing.md))

        HavnTextReveal(
            text = "Hello, $name.",
            style = MaterialTheme.typography.displayMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(HavnTheme.spacing.md))
        Text(
            text = "Pick a colour. It marks your profile wherever it appears.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textTertiary,
        )

        Spacer(Modifier.height(HavnTheme.spacing.section))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HavnTheme.spacing.md),
        ) {
            AVATAR_COLORS.forEach { hex ->
                ColourSwatch(
                    hex = hex,
                    selected = hex == selectedColor,
                    onClick = { onSelectColor(hex) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(HavnTheme.spacing.section))

        HavnButton(
            text = "Begin",
            onClick = onFinish,
            loading = isSubmitting,
            enabled = !isSubmitting,
        )
        Spacer(Modifier.height(HavnTheme.spacing.md))
        HavnButton(
            text = "Back",
            onClick = onBack,
            tone = HavnButtonTone.Ghost,
            size = HavnButtonSize.Medium,
            enabled = !isSubmitting,
        )

        Spacer(Modifier.height(HavnTheme.spacing.xxxl))
    }
}

@Composable
private fun ColourSwatch(
    hex: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val tint = remember(hex) {
        runCatching { Color(android.graphics.Color.parseColor(hex)) }
            .getOrDefault(Color(0xFF516351))
    }
    // The ring grows rather than the swatch: changing the swatch's own size
    // would reflow the row and make its neighbours twitch on every selection.
    val ring by animateDpAsState(
        targetValue = if (selected) 3.dp else 0.dp,
        animationSpec = HavnMotion.settle(),
        label = "swatchRing",
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .havnPress(scaleDown = 0.9f, onClickLabel = "Select colour", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(tint)
                .border(ring, colors.canvas, CircleShape)
                .border(
                    width = if (selected) 1.5.dp else 0.dp,
                    color = if (selected) colors.textPrimary else Color.Transparent,
                    shape = CircleShape,
                )
        )
    }
}

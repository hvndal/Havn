package com.havn.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.havn.app.R
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType

/** Height of the bar itself, excluding the navigation-bar inset beneath it. */
private val BarHeight = 60.dp

@Composable
fun HavnScaffold(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val navInset = WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        // Content runs full-bleed *under* the bar rather than being inset by a
        // Scaffold. That is what lets the home hero and the organizer reach the
        // bottom edge; screens apply the padding to their scroll content, so
        // the last row can still scroll clear of the bar.
        content(PaddingValues(bottom = BarHeight + navInset))

        HavnBottomBar(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun HavnBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // A short gradient above the bar so scrolling content dissolves into it
        // instead of being cut by a hard edge.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, colors.canvas)
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.canvas)
                .navigationBarsPadding()
                .height(BarHeight)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            bottomNavItems.forEach { item ->
                HavnNavItem(
                    item = item,
                    selected = currentRoute == item.screen.route,
                    onClick = { onNavigate(item.screen) },
                    // weight, not fixed padding: the bar now divides the
                    // available width evenly and fits every phone size rather
                    // than overflowing on small ones.
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HavnNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HavnTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val tint by animateColorAsState(
        targetValue = if (selected) colors.accent else colors.textTertiary,
        animationSpec = HavnMotion.standard(),
        label = "navTint",
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = HavnMotion.press(),
        label = "navScale",
    )
    // The active indicator is a single dot, not a filled pill. At four tabs a
    // pill behind the label is most of the bar's width and reads as chrome;
    // a dot reads as punctuation.
    val dotScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = HavnMotion.settle(),
        label = "navDot",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(HavnTheme.radius.md))
            .selectable(
                selected = selected,
                interactionSource = interaction,
                indication = null,
                role = Role.Tab,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(iconRes(if (selected) item.activeIcon else item.icon)),
            contentDescription = null, // the label below already names it
            tint = tint,
            modifier = Modifier.size(21.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = item.label,
            style = HavnType.EyebrowQuiet,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(3.dp)
                .graphicsLayer {
                    scaleX = dotScale
                    scaleY = dotScale
                    alpha = dotScale
                }
                .clip(CircleShape)
                .background(colors.accent)
        )
    }
}

private fun iconRes(name: String): Int = when (name) {
    "home" -> R.drawable.ic_home
    "home_fill" -> R.drawable.ic_home_fill
    "pill" -> R.drawable.ic_pill
    "pill_fill" -> R.drawable.ic_pill_fill
    "chart" -> R.drawable.ic_chart
    "chart_fill" -> R.drawable.ic_chart_fill
    "calendar_today" -> R.drawable.ic_calendar
    "settings" -> R.drawable.ic_settings
    "settings_fill" -> R.drawable.ic_settings_fill
    else -> R.drawable.ic_home
}

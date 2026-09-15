package com.havn.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme

/**
 * A segmented control with a thumb that *slides* between options.
 *
 * The sliding thumb is the point: it makes the two panes read as adjacent views
 * of the same data rather than two unrelated screens, which is exactly the
 * relationship between Trends and Calendar.
 */
@Composable
fun HavnSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 42.dp,
) {
    if (options.isEmpty()) return
    val colors = HavnTheme.colors
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(HavnTheme.radius.pill)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(colors.surfaceSunken)
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / options.size
        val thumbOffset by animateFloatAsState(
            targetValue = selectedIndex.toFloat(),
            animationSpec = HavnMotion.settle(),
            label = "segmentThumb",
        )

        Box(
            modifier = Modifier
                .offset(x = segmentWidth * thumbOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .shadow(
                    elevation = 3.dp,
                    shape = shape,
                    ambientColor = colors.shadowTint.copy(alpha = if (colors.isDark) 0.8f else 0.12f),
                    spotColor = colors.shadowTint.copy(alpha = if (colors.isDark) 0.8f else 0.10f),
                )
                .clip(shape)
                .background(colors.surfaceRaised)
        )

        Row(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (selected) colors.textPrimary else colors.textTertiary,
                    animationSpec = HavnMotion.standard(),
                    label = "segmentText",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(shape)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelect(index)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

package com.havn.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType

/**
 * The app's text input.
 *
 * A sunken well with a hairline that warms to the accent on focus — no floating
 * label, no filled Material box. The label sits above the field as a small
 * tracked eyebrow, which keeps the input itself uncluttered and means the label
 * never animates over the user's own text.
 */
@Composable
fun HavnTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    helper: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 54.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = HavnTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            error != null -> colors.danger
            focused -> colors.accent
            else -> colors.hairline
        },
        animationSpec = HavnMotion.standard(),
        label = "fieldBorder",
    )

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                style = HavnType.Eyebrow,
                color = if (error != null) colors.danger else colors.textTertiary,
            )
            Spacer(Modifier.height(HavnTheme.spacing.sm))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .clip(RoundedCornerShape(HavnTheme.radius.md))
                .background(colors.surfaceSunken)
                .border(
                    width = if (focused || error != null) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(HavnTheme.radius.md),
                )
                .padding(horizontal = HavnTheme.spacing.lg, vertical = HavnTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
                enabled = enabled,
                singleLine = singleLine,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = if (enabled) colors.textPrimary else colors.textDisabled,
                ),
                cursorBrush = SolidColor(colors.accent),
                interactionSource = interaction,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction,
                ),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textDisabled,
                            )
                        }
                        inner()
                    }
                },
            )
            if (trailing != null) {
                Spacer(Modifier.height(0.dp))
                trailing()
            }
        }

        val message = error ?: helper
        if (message != null) {
            Spacer(Modifier.height(HavnTheme.spacing.sm))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = if (error != null) colors.danger else colors.textTertiary,
            )
        }
    }
}

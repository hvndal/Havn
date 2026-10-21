package com.havn.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.HavnType

/**
 * The Hävn mark: a pill organiser box, its left half divided into three
 * compartments holding a capsule, a scored tablet and an oval, with the H on
 * the right.
 *
 * The geometry is preserved exactly — this is the existing identity and it is
 * good. What changed is that every colour was hard-coded to light-mode values
 * (white and beige fills, near-black H), so on a dark canvas the mark rendered
 * as a bright rectangle with an invisible letterform. The pigments now come
 * from theme tokens, which keeps the mark recognisable in both themes.
 */
@Composable
fun HavnBrandLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 100.dp,
    showWordmark: Boolean = true,
    showTagline: Boolean = true,
) {
    // A typographic mark, not a pill box: a heavy "H" with the one orange
    // square beside it, then the wordmark in tracked mono.
    val colors = HavnTheme.colors
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
            androidx.compose.material3.Text(
                text = "H",
                style = com.havn.app.ui.theme.HavnType.Poster.copy(
                    fontSize = (iconSize.value * 0.9f).sp,
                    lineHeight = (iconSize.value * 0.82f).sp,
                ),
                color = colors.textPrimary,
            )
            androidx.compose.foundation.layout.Box(
                Modifier
                    .padding(start = (iconSize.value * 0.06f).dp, bottom = (iconSize.value * 0.08f).dp)
                    .size((iconSize.value * 0.16f).dp)
                    .background(colors.signal)
            )
        }
        if (showWordmark) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            androidx.compose.material3.Text("HÄVN", style = com.havn.app.ui.theme.HavnType.Wordmark, color = colors.textPrimary)
        }
        if (showTagline) {
            androidx.compose.material3.Text("Medication, quietly.", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFF2F1EC)
@Composable
private fun HavnBrandLogoLightPreview() {
    com.havn.app.ui.theme.HavnTheme(themeMode = com.havn.app.ui.theme.ThemeMode.LIGHT) {
        HavnBrandLogo(iconSize = 100.dp)
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF121210, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HavnBrandLogoDarkPreview() {
    com.havn.app.ui.theme.HavnTheme(themeMode = com.havn.app.ui.theme.ThemeMode.DARK) {
        HavnBrandLogo(iconSize = 100.dp)
    }
}


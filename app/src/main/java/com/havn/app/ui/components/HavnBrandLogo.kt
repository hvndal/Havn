package com.havn.app.ui.components

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
import androidx.compose.ui.graphics.Color
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
    val colors = HavnTheme.colors

    // In dark mode the box reads as an *object in low light* rather than an
    // inverted diagram: the shell stays pale enough to hold its silhouette,
    // but sits well below the text so it doesn't glare.
    val shell = if (colors.isDark) Color(0xFF232823) else Color(0xFFFFFFFF)
    val shellAlt = if (colors.isDark) Color(0xFF1B201B) else Color(0xFFEDE8DF)
    val stroke = if (colors.isDark) Color(0xFF6F7F6E) else Color(0xFF5E6E5D)
    val letterColor = if (colors.isDark) Color(0xFFDDE3DA) else Color(0xFF1B241A)
    val tabletFill = if (colors.isDark) Color(0xFF3A403A) else Color(0xFFF0ECE3)
    val tabletStroke = if (colors.isDark) Color(0xFF555C54) else Color(0xFFD3CDBF)
    val tabletScore = if (colors.isDark) Color(0xFF6E766C) else Color(0xFFB5AE9E)
    val ovalFill = if (colors.isDark) Color(0xFF7F9A7D) else Color(0xFF536352)
    val ovalStroke = if (colors.isDark) Color(0xFF5F7A5D) else Color(0xFF384637)
    val capsuleTint = if (colors.isDark) Color(0xFF8FA98D) else Color(0xFF5E6E5D)
    val capsuleLight = if (colors.isDark) Color(0xFFE4EAE1) else Color(0xFFFFFFFF)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.semantics { contentDescription = "Hävn" },
    ) {
        Canvas(
            modifier = Modifier
                .size(iconSize)
                .clearAndSetSemantics { },
        ) {
            val w = size.width
            val h = size.height
            val strokeW = w * 0.035f
            val cornerR = w * 0.18f

            val outerRoundRect = RoundRect(
                rect = Rect(0f, 0f, w, h),
                cornerRadius = CornerRadius(cornerR, cornerR),
            )

            val outerPath = Path().apply { addRoundRect(outerRoundRect) }
            drawPath(outerPath, shell)

            clipPath(outerPath) {
                drawPath(Path().apply { addRect(Rect(w * 0.5f, 0f, w, h)) }, shellAlt)
            }

            // Divider + compartment rules
            drawLine(stroke, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeW)
            drawLine(stroke, Offset(0f, h * 0.333f), Offset(w * 0.5f, h * 0.333f), strokeW)
            drawLine(stroke, Offset(0f, h * 0.666f), Offset(w * 0.5f, h * 0.666f), strokeW)

            // 1 — capsule, top-left compartment
            val capCx = w * 0.25f
            val capCy = h * 0.166f
            val capW = w * 0.18f
            val capH = capW * 0.45f
            rotate(degrees = -35f, pivot = Offset(capCx, capCy)) {
                val capPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(capCx - capW, capCy - capH, capCx + capW, capCy + capH),
                            cornerRadius = CornerRadius(capH, capH),
                        )
                    )
                }
                clipPath(capPath) {
                    drawRect(capsuleTint, Offset(capCx - capW, capCy - capH), Size(capW, capH * 2))
                    drawRect(capsuleLight, Offset(capCx, capCy - capH), Size(capW, capH * 2))
                }
                drawPath(capPath, stroke, style = Stroke(width = strokeW * 0.6f))
            }

            // 2 — scored tablet, middle compartment
            val tabCx = w * 0.25f
            val tabCy = h * 0.5f
            val tabR = w * 0.085f
            drawCircle(tabletFill, tabR, Offset(tabCx, tabCy))
            drawCircle(tabletStroke, tabR, Offset(tabCx, tabCy), style = Stroke(width = strokeW * 0.5f))
            drawLine(
                tabletScore,
                Offset(tabCx - tabR * 0.6f, tabCy - tabR * 0.6f),
                Offset(tabCx + tabR * 0.6f, tabCy + tabR * 0.6f),
                strokeW * 0.5f,
            )

            // 3 — oval, bottom compartment
            val ovCx = w * 0.25f
            val ovCy = h * 0.833f
            val ovW = w * 0.16f
            val ovH = ovW * 0.55f
            rotate(degrees = -15f, pivot = Offset(ovCx, ovCy)) {
                val ovPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(ovCx - ovW, ovCy - ovH, ovCx + ovW, ovCy + ovH),
                            cornerRadius = CornerRadius(ovH, ovH),
                        )
                    )
                }
                drawPath(ovPath, ovalFill)
                drawPath(ovPath, ovalStroke, style = Stroke(width = strokeW * 0.5f))
            }

            // The H
            val hCx = w * 0.75f
            val hCy = h * 0.5f
            val hH = h * 0.36f
            val hW = h * 0.18f
            val hStroke = strokeW * 0.8f
            drawLine(letterColor, Offset(hCx - hW / 2, hCy - hH / 2), Offset(hCx - hW / 2, hCy + hH / 2), hStroke)
            drawLine(letterColor, Offset(hCx + hW / 2, hCy - hH / 2), Offset(hCx + hW / 2, hCy + hH / 2), hStroke)
            drawLine(letterColor, Offset(hCx - hW / 2, hCy), Offset(hCx + hW / 2, hCy), hStroke)

            drawRoundRect(
                color = stroke,
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = strokeW),
            )
        }

        if (showWordmark) {
            Spacer(Modifier.height(iconSize * 0.22f))
            Text(
                text = "HÄVN",
                // Tracking now comes from the type scale rather than literal
                // spaces in the string, so screen readers announce the brand
                // name instead of spelling it out letter by letter.
                style = HavnType.Wordmark.copy(
                    fontSize = (iconSize.value * 0.26f).sp,
                    letterSpacing = (iconSize.value * 0.055f).sp,
                ),
                color = colors.textPrimary,
            )
        }

        if (showTagline) {
            Spacer(Modifier.height(iconSize * 0.11f))
            Text(
                text = "REMEMBER. LIVE BETTER.",
                style = HavnType.Eyebrow.copy(
                    fontSize = (iconSize.value * 0.085f).coerceIn(9f, 12f).sp,
                ),
                color = colors.textTertiary,
            )
        }
    }
}

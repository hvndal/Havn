package com.havn.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.havn.app.domain.model.MedCoating
import com.havn.app.domain.model.MedColorPalette
import com.havn.app.domain.model.MedScoreLine
import com.havn.app.domain.model.MedShape
import com.havn.app.domain.model.MedSize
import com.havn.app.domain.model.MedicationVisualSpec

@Composable
fun MedicationObject(
    spec: MedicationVisualSpec,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    elevationDp: Dp = 4.dp,
) {
    val primaryColor = MedColorPalette.getColor(spec.primaryColorTag)
    val secondaryColor = spec.secondaryColorTag?.let { MedColorPalette.getColor(it) } ?: primaryColor

    val scaleFactor = when (spec.size) {
        MedSize.SMALL -> 0.8f
        MedSize.MEDIUM -> 1.0f
        MedSize.LARGE -> 1.2f
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasWidth = size.toPx()
            val canvasHeight = size.toPx()
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f

            // Dimension calculations according to shape
            val baseWidth: Float
            val baseHeight: Float
            val cornerRadius: Float

            when (spec.shape) {
                MedShape.ROUND_TABLET -> {
                    baseWidth = canvasWidth * 0.70f * scaleFactor
                    baseHeight = baseWidth
                    cornerRadius = baseWidth / 2f
                }
                MedShape.SMALL_TABLET -> {
                    baseWidth = canvasWidth * 0.52f * scaleFactor
                    baseHeight = baseWidth
                    cornerRadius = baseWidth / 2f
                }
                MedShape.LARGE_TABLET -> {
                    baseWidth = canvasWidth * 0.82f * scaleFactor
                    baseHeight = baseWidth
                    cornerRadius = baseWidth / 2f
                }
                MedShape.OVAL_TABLET -> {
                    baseWidth = canvasWidth * 0.85f * scaleFactor
                    baseHeight = canvasHeight * 0.50f * scaleFactor
                    cornerRadius = baseHeight / 2f
                }
                MedShape.CAPSULE -> {
                    baseWidth = canvasWidth * 0.88f * scaleFactor
                    baseHeight = canvasHeight * 0.44f * scaleFactor
                    cornerRadius = baseHeight / 2f
                }
                MedShape.SCORED_TABLET -> {
                    baseWidth = canvasWidth * 0.72f * scaleFactor
                    baseHeight = baseWidth
                    cornerRadius = baseWidth / 2f
                }
            }

            val left = centerX - baseWidth / 2f
            val top = centerY - baseHeight / 2f
            val rect = Rect(left, top, left + baseWidth, top + baseHeight)

            // 1. Soft Studio Ambient Drop Shadow
            val shadowPath = Path().apply {
                addRoundRect(RoundRect(rect, CornerRadius(cornerRadius, cornerRadius)))
            }

            drawContext.canvas.nativeCanvas.apply {
                val filter = android.graphics.BlurMaskFilter(
                    elevationDp.toPx().coerceAtLeast(1f),
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(45, 40, 45, 40)
                    maskFilter = filter
                    isAntiAlias = true
                }
                save()
                translate(0f, elevationDp.toPx() * 0.75f)
                drawPath(shadowPath.asAndroidPath(), paint)
                restore()
            }

            // 2. Main Body Clip Path
            val mainPath = Path().apply {
                addRoundRect(RoundRect(rect, CornerRadius(cornerRadius, cornerRadius)))
            }

            // Dual tone or single tone base color
            clipPath(mainPath) {
                if (spec.shape == MedShape.CAPSULE && spec.secondaryColorTag != null) {
                    // Split vertically down the middle (left half / right half)
                    val midX = centerX
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(left, top),
                        size = Size(baseWidth / 2f, baseHeight)
                    )
                    drawRect(
                        color = secondaryColor,
                        topLeft = Offset(midX, top),
                        size = Size(baseWidth / 2f, baseHeight)
                    )

                    // Capsule Band Highlight
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = Offset(midX, top),
                        end = Offset(midX, top + baseHeight),
                        strokeWidth = 1.5f * density
                    )
                } else {
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(left, top),
                        size = Size(baseWidth, baseHeight)
                    )
                }

                // 3. Studio Lighting Overlay (Soft Top-Left Soft Light -> Darker Bottom-Right Ambient Occlusion)
                val isDarkColor = (primaryColor.red * 0.299f + primaryColor.green * 0.587f + primaryColor.blue * 0.114f) < 0.5f

                val studioHighlightAlpha = when (spec.coating) {
                    MedCoating.MATTE -> 0.18f
                    MedCoating.SATIN -> 0.35f
                    MedCoating.GLOSSY -> 0.55f
                }

                val studioShadowAlpha = when (spec.coating) {
                    MedCoating.MATTE -> 0.12f
                    MedCoating.SATIN -> 0.22f
                    MedCoating.GLOSSY -> 0.32f
                }

                // Top-Left Soft Diffuse Light
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = studioHighlightAlpha),
                            Color.White.copy(alpha = studioHighlightAlpha * 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(rect.left + baseWidth * 0.25f, rect.top + baseHeight * 0.2f),
                        radius = baseWidth * 0.85f
                    )
                )

                // Bottom-Right Soft Ambient Occlusion
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = studioShadowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(rect.right - baseWidth * 0.15f, rect.bottom - baseHeight * 0.15f),
                        radius = baseWidth * 0.75f
                    )
                )

                // 4. Bevel Contour Edge Lighting
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDarkColor) 0.3f else 0.45f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.15f)
                        ),
                        startY = top,
                        endY = top + baseHeight
                    ),
                    topLeft = Offset(left, top),
                    size = Size(baseWidth, baseHeight),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = 1.25f * density)
                )

                // Glossy specular streak
                if (spec.coating == MedCoating.GLOSSY) {
                    drawOval(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.0f)
                            ),
                            start = Offset(rect.left + baseWidth * 0.15f, rect.top + baseHeight * 0.15f),
                            end = Offset(rect.left + baseWidth * 0.5f, rect.top + baseHeight * 0.4f)
                        ),
                        topLeft = Offset(rect.left + baseWidth * 0.15f, rect.top + baseHeight * 0.15f),
                        size = Size(baseWidth * 0.4f, baseHeight * 0.3f)
                    )
                }

                // 5. Score Line Detail
                val effectiveScoreLine = if (spec.shape == MedShape.SCORED_TABLET && spec.scoreLine == MedScoreLine.NONE) {
                    MedScoreLine.SINGLE
                } else {
                    spec.scoreLine
                }

                if (effectiveScoreLine != MedScoreLine.NONE) {
                    val scoreStrokeWidth = 1.8f * density
                    val shadowOffset = 0.8f * density

                    // Primary Vertical / Horizontal Score Line
                    if (effectiveScoreLine == MedScoreLine.SINGLE || effectiveScoreLine == MedScoreLine.CROSS) {
                        // Horizontal score line for oval/capsule or vertical for round
                        val isHorizontal = spec.shape == MedShape.OVAL_TABLET || spec.shape == MedShape.CAPSULE
                        if (isHorizontal) {
                            val startX = left + baseWidth * 0.15f
                            val endX = left + baseWidth * 0.85f
                            val lineY = centerY

                            // Deboss shadow (top edge)
                            drawLine(
                                color = Color.Black.copy(alpha = 0.25f),
                                start = Offset(startX, lineY - shadowOffset / 2),
                                end = Offset(endX, lineY - shadowOffset / 2),
                                strokeWidth = scoreStrokeWidth,
                                cap = StrokeCap.Round
                            )
                            // Highlight highlight (bottom edge)
                            drawLine(
                                color = Color.White.copy(alpha = 0.45f),
                                start = Offset(startX, lineY + shadowOffset),
                                end = Offset(endX, lineY + shadowOffset),
                                strokeWidth = scoreStrokeWidth * 0.8f,
                                cap = StrokeCap.Round
                            )
                        } else {
                            val startY = top + baseHeight * 0.15f
                            val endY = top + baseHeight * 0.85f
                            val lineX = centerX

                            // Deboss shadow
                            drawLine(
                                color = Color.Black.copy(alpha = 0.25f),
                                start = Offset(lineX - shadowOffset / 2, startY),
                                end = Offset(lineX - shadowOffset / 2, endY),
                                strokeWidth = scoreStrokeWidth,
                                cap = StrokeCap.Round
                            )
                            // Highlight
                            drawLine(
                                color = Color.White.copy(alpha = 0.45f),
                                start = Offset(lineX + shadowOffset, startY),
                                end = Offset(lineX + shadowOffset, endY),
                                strokeWidth = scoreStrokeWidth * 0.8f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    if (effectiveScoreLine == MedScoreLine.CROSS) {
                        // Perpendicular score line
                        val startX = left + baseWidth * 0.15f
                        val endX = left + baseWidth * 0.85f
                        val lineY = centerY

                        drawLine(
                            color = Color.Black.copy(alpha = 0.25f),
                            start = Offset(startX, lineY - shadowOffset / 2),
                            end = Offset(endX, lineY - shadowOffset / 2),
                            strokeWidth = scoreStrokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(startX, lineY + shadowOffset),
                            end = Offset(endX, lineY + shadowOffset),
                            strokeWidth = scoreStrokeWidth * 0.8f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 6. Imprint Detail Text (debossed studio appearance)
                if (spec.imprint.isNotBlank()) {
                    val paintShadow = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(100, 20, 25, 20)
                        textSize = baseHeight * 0.28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                    }
                    val paintHighlight = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(120, 255, 255, 255)
                        textSize = baseHeight * 0.28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                    }

                    val textY = centerY + (paintShadow.textSize * 0.35f)
                    val imprintText = spec.imprint.take(4).uppercase()

                    // Debossed text effect
                    drawContext.canvas.nativeCanvas.drawText(
                        imprintText,
                        centerX + 0.5f * density,
                        textY + 0.8f * density,
                        paintHighlight
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        imprintText,
                        centerX,
                        textY,
                        paintShadow
                    )
                }
            }
        }
    }
}

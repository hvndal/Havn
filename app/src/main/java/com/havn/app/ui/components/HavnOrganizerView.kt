package com.havn.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.havn.app.domain.model.DayPeriod
import com.havn.app.domain.model.DoseStatus
import com.havn.app.domain.model.TodayDose
import com.havn.app.ui.theme.HavnTheme

@Composable
fun HavnOrganizerView(
    doses: List<TodayDose>,
    selectedPeriod: DayPeriod,
    onSlotTapped: (DayPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periods = DayPeriod.entries
    val textMeasurer = rememberTextMeasurer()
    val brandAccent = HavnTheme.colors.accent
    val background = HavnTheme.colors.surface
    val textPrimary = HavnTheme.colors.textPrimary
    
    // Animate the selected index for a smooth camera shift effect
    val animatedSelectedIndex by animateFloatAsState(
        targetValue = selectedPeriod.ordinal.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "selectedIndex"
    )

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                // Basic hit detection (divide height by 4)
                val compartmentHeight = size.height / 4
                val tappedIndex = (offset.y / compartmentHeight).toInt().coerceIn(0, 3)
                onSlotTapped(periods[tappedIndex])
            }
        }
    ) {
        val width = size.width
        val height = size.height
        
        val compartmentHeight = height / 5f
        val cylinderWidth = width * 0.5f
        val startX = (width - cylinderWidth) / 2f
        
        // Draw 4 compartments from top to bottom
        for (i in 0 until 4) {
            val period = periods[i]
            val periodDoses = doses.filter { it.period == period }
            
            // Determine state
            val allTaken = periodDoses.isNotEmpty() && periodDoses.all { it.status == DoseStatus.TAKEN }
            val anyDue = periodDoses.any { it.status == DoseStatus.PENDING } && period == DayPeriod.current() // Simplification
            val isUpcoming = period > DayPeriod.current()
            val isSelected = selectedPeriod == period
            
            val yOffset = i * compartmentHeight + (height * 0.1f)
            
            // 3D pseudo-projection offset
            val popOutOffset = if (isSelected) -20f else 0f
            
            translate(left = popOutOffset) {
                // Base cylinder body
                val bodyRect = Rect(Offset(startX, yOffset), Size(cylinderWidth, compartmentHeight * 0.8f))
                
                // Lid color
                val lidColor = when {
                    allTaken -> brandAccent.copy(alpha = 0.8f)
                    anyDue -> brandAccent.copy(alpha = 0.4f)
                    else -> Color.Gray.copy(alpha = 0.2f)
                }
                
                // Draw compartment shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.1f),
                    topLeft = Offset(startX + 10f, yOffset + 10f),
                    size = Size(cylinderWidth, compartmentHeight * 0.8f),
                    cornerRadius = CornerRadius(20f, 20f)
                )
                
                // Draw compartment body
                drawRoundRect(
                    color = background,
                    topLeft = bodyRect.topLeft,
                    size = bodyRect.size,
                    cornerRadius = CornerRadius(20f, 20f)
                )
                drawRoundRect(
                    color = textPrimary.copy(alpha = 0.1f),
                    topLeft = bodyRect.topLeft,
                    size = bodyRect.size,
                    cornerRadius = CornerRadius(20f, 20f),
                    style = Stroke(width = 2f)
                )
                
                // Draw Lid
                val lidOpenAngle = if (anyDue) -15f else 0f
                withTransform({
                    translate(left = startX, top = yOffset)
                    rotate(lidOpenAngle, pivot = Offset(0f, 0f))
                    translate(left = -startX, top = -yOffset)
                }) {
                    drawRoundRect(
                        color = lidColor,
                        topLeft = Offset(startX, yOffset),
                        size = Size(cylinderWidth, compartmentHeight * 0.2f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                }
                
                // Text label
                drawText(
                    textMeasurer = textMeasurer,
                    text = period.name,
                    topLeft = Offset(startX + 20f, yOffset + compartmentHeight * 0.3f),
                    style = TextStyle(color = textPrimary, fontSize = 16.sp)
                )
                
                // "Now" tag
                if (anyDue) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "Now",
                        topLeft = Offset(startX + cylinderWidth - 60f, yOffset + compartmentHeight * 0.3f),
                        style = TextStyle(color = brandAccent, fontSize = 14.sp)
                    )
                }
            }
        }
    }
}

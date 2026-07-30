package com.havn.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.havn.app.ui.MainActivity

class HavnWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { HavnWidgetContent() }
    }
}

@Composable
private fun HavnWidgetContent() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5))
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier.padding(16.dp),
        ) {
            Text(
                text = "H\u00e4vn",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF516351)),
                    fontSize = androidx.glance.unit.sp(18),
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = "Tap to open",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF747872)),
                    fontSize = androidx.glance.unit.sp(11),
                ),
            )
        }
    }
}

class HavnWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HavnWidget()
}

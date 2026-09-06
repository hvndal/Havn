package com.havn.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.havn.app.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "havn_daily_ritual"
        const val CHANNEL_NAME = "Daily Ritual"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medName = intent.getStringExtra("med_name") ?: "Your medication"
        val medId   = intent.getLongExtra("med_id", -1L)

        createChannel(context)

        val notifTitle = calmTitle()
        val notifText  = calmBody(medName)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notifTitle)
            .setContentText(notifText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 80, 0))
            .build()

        manager.notify(medId.toInt(), notif)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Gentle reminders for your daily ritual"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 80, 0)
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    // ── Calm notification copywriting — Hävn brand voice ───────────────
    private fun calmTitle(): String = listOf(
        "H\u00e4vn",
        "Whenever you\u2019re ready.",
        "Ready when you are.",
        "One thing today.",
        "Your ritual awaits.",
    ).random()

    private fun calmBody(medName: String): String = listOf(
        "$medName is waiting, whenever you\u2019re ready.",
        "$medName. Whenever you have a moment.",
        "Today\u2019s organizer is ready.",
        "$medName — just one more thing today.",
        "Almost done. $medName remaining.",
        "Good morning. $medName is waiting.",
    ).random()
}

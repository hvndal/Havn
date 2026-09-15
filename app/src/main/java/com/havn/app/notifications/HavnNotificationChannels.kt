package com.havn.app.notifications

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Notification channels, declared once.
 *
 * Previously `AlarmReceiver` and `PillReminderWorker` each created channels
 * independently, with different ids, importances and vibration patterns — and
 * `AlarmReceiver` re-created its channel on *every* notification. Android
 * ignores changes to an existing channel, so whichever component happened to
 * run first silently decided the app's notification behaviour for good.
 *
 * Splitting doses from the evening summary also gives users a real choice:
 * they can silence the nightly nudge without silencing the dose reminders that
 * are the point of the app.
 */
object HavnNotificationChannels {

    const val GROUP_ID = "havn_reminders"

    /** Time-critical: an actual dose is due. */
    const val DOSE = "havn_dose_v2"

    /** Ahead-of-time nudge, 15 minutes before a dose. Quieter by design. */
    const val PRE_DOSE = "havn_pre_dose_v2"

    /** The single end-of-day summary. */
    const val EVENING = "havn_evening_v2"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannelGroup(
            NotificationChannelGroup(GROUP_ID, "Reminders")
        )

        val channels = listOf(
            NotificationChannel(
                DOSE,
                "Dose reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "When a scheduled dose is due."
                group = GROUP_ID
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 140, 90, 140)
                setShowBadge(true)
            },
            NotificationChannel(
                PRE_DOSE,
                "Early nudges",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "A quiet heads-up 15 minutes before a dose."
                group = GROUP_ID
                enableVibration(false)
                setShowBadge(false)
            },
            NotificationChannel(
                EVENING,
                "Evening check-in",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "One summary at the end of the day if doses are outstanding."
                group = GROUP_ID
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 120)
                setShowBadge(true)
            },
        )

        channels.forEach { manager.createNotificationChannel(it) }

        // Retire the earlier single mixed-purpose channels so they stop
        // appearing in system settings alongside the new ones.
        listOf(
            "havn_daily_ritual",
            "havn_channel_silent",
            "havn_channel_marimba",
        ).forEach { legacy ->
            runCatching { manager.deleteNotificationChannel(legacy) }
        }
    }

    fun channelFor(isPreDose: Boolean, isEveningCheck: Boolean): String = when {
        isEveningCheck -> EVENING
        isPreDose -> PRE_DOSE
        else -> DOSE
    }
}

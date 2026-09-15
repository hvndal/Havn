package com.havn.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val workManager = WorkManager.getInstance(context)

            // Re-schedule daily work after boot
            val periodicRequest = PeriodicWorkRequestBuilder<RescheduleWorker>(24, TimeUnit.HOURS)
                .build()
            workManager.enqueueUniquePeriodicWork(
                "havn_daily_sync",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest,
            )

            val immediateRequest = OneTimeWorkRequestBuilder<RescheduleWorker>()
                .build()
            workManager.enqueueUniqueWork(
                "havn_immediate_sync",
                ExistingWorkPolicy.REPLACE,
                immediateRequest,
            )
        }
    }
}

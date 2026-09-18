package com.havn.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.havn.app.data.session.SessionManager
import com.havn.app.notifications.HavnNotificationChannels
import com.havn.app.notifications.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HavnApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var reminderScheduler: ReminderScheduler

    /**
     * Injected purely so the session begins resolving at process start rather
     * than when the first screen asks for it. Without this the nav graph can
     * render one frame before the stored profile is known.
     */
    @Inject lateinit var sessionManager: SessionManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Channels are registered up front so they exist in system settings
        // before the first notification fires — a channel created lazily at
        // notify() time cannot have its importance changed afterwards.
        // No stale cache between launches: old export files and WebView
        // leftovers are cleared on every cold start.
        runCatching { cacheDir.listFiles()?.forEach { it.deleteRecursively() } }
        runCatching { codeCacheDir.listFiles()?.forEach { it.deleteRecursively() } }
        HavnNotificationChannels.ensureCreated(this)
        reminderScheduler.scheduleDailySyncWork()
    }
}

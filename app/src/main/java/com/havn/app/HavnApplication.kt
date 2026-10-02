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
        // Clear only the temporary export cache, NEVER codeCacheDir or all of cacheDir.
        // Deleting codeCacheDir wipes ART's active memory-mapped JIT/dex cache files,
        // causing an uncatchable native SIGSEGV/SIGBUS crash on launch.
        runCatching {
            java.io.File(cacheDir, "exports").deleteRecursively()
        }
        runCatching {
            HavnNotificationChannels.ensureCreated(this)
        }
        runCatching {
            reminderScheduler.scheduleDailySyncWork()
        }
    }
}

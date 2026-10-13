package com.havn.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject lateinit var alarmScheduler: HavnAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        // MY_PACKAGE_REPLACED: an app update clears every pending alarm, so
        // reminders must be rebuilt without waiting for the user to open Hävn.
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED || 
            intent.action == Intent.ACTION_TIMEZONE_CHANGED || 
            intent.action == Intent.ACTION_TIME_CHANGED) {
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    alarmScheduler.rebuildAlarms()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

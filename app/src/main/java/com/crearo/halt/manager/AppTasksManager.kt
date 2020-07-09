package com.crearo.halt.manager

import android.app.AppOpsManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class AppTasksManager @Inject constructor(@ApplicationContext val context: Context) {

    fun getCurrentlyRunningAppName(): String {
        if (!hasUsageStatsPermission()) return ""

        val usageStatsManager =
            context.getSystemService(Service.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()

        val usageEvents = usageStatsManager.queryEvents(now - 1000 * 3600, now)
        val event = UsageEvents.Event()
        var foregroundApp = ""
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                foregroundApp = event.packageName
            }
        }
        return foregroundApp
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
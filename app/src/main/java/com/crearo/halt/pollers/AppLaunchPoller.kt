package com.crearo.halt.pollers

import android.app.AppOpsManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppLaunchPoller @Inject constructor(@ApplicationContext context: Context) :
    Poller(context) {

    override fun start() {
        compositeDisposable.add(tickerObservable
            .map { getCurrentlyRunningAppName() }
            .distinctUntilChanged()
            .doOnNext { onAppLaunched(it) }
            .subscribe()
        )
    }

    private fun onAppLaunched(appName: String) {
        Timber.d("App Launched: $appName")
    }

    private fun getCurrentlyRunningAppName(): String {
        if (!hasUsageStatsPermission(context)) return ""

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

    companion object {
        /*todo move this out to a manager*/
        fun hasUsageStatsPermission(context: Context): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                "android:get_usage_stats",
                Process.myUid(), context.packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        }
    }

}
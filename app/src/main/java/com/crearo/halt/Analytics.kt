package com.crearo.halt

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import java.time.Instant

class Analytics(private val firebaseAnalytics: FirebaseAnalytics) {

    fun sendStartForegroundService(now: Instant) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "start_foreground_service")
        bundle.putLong("timestamp", now.toEpochMilli())
        bundle.putString("time", now.toReadableString())
        firebaseAnalytics.logEvent("session", bundle)
    }

    fun sendStopForegroundService(now: Instant) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "stop_foreground_service")
        bundle.putLong("timestamp", now.toEpochMilli())
        bundle.putString("time", now.toReadableString())
        firebaseAnalytics.logEvent("session", bundle)
    }

}
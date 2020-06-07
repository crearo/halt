package com.crearo.halt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import timber.log.Timber

class PhoneLockBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.d("PhoneLockBR %s", intent.toString())

        if (intent?.action.equals(Intent.ACTION_USER_PRESENT)) {
            val launchIntent = Intent(context, MainActivity::class.java)
            launchIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(launchIntent)
            Timber.d("Should've opened our activity")
        }
    }
}
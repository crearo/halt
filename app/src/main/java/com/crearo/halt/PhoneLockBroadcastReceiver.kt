package com.crearo.halt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.Intent.ACTION_USER_PRESENT
import com.crearo.halt.data.UnlockStatRepository
import timber.log.Timber
import java.time.Instant.now
import javax.inject.Inject

class PhoneLockBroadcastReceiver @Inject constructor() : BroadcastReceiver() {

    @Inject
    lateinit var unlockStatRepository: UnlockStatRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.d("broadcast: ${intent.toString()}")

        when (intent?.action) {
            ACTION_USER_PRESENT -> unlockStatRepository.addNewUnlock(now()).subscribe()
            ACTION_SCREEN_OFF -> unlockStatRepository.addNewUnlock(now())
                .subscribe() // todo rename to newLock
        }

    }
}
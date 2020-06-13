package com.crearo.halt.pollers

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import com.crearo.halt.data.UnlockStatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.time.Instant.now
import javax.inject.Inject

class PhoneLockStatePoller @Inject constructor(@ApplicationContext context: Context) :
    Poller(context) {

    @Inject
    lateinit var unlockStatRepository: UnlockStatRepository

    override fun start() {
        compositeDisposable.add(tickerObservable
            .map { isPhoneUnlocked() }
            .distinctUntilChanged()
            .doOnNext { value -> onPhoneStateChanged(value) }
            .subscribe()
        )
    }

    private fun onPhoneStateChanged(unlocked: Boolean) {
        Timber.d("Interactive: $unlocked")
        if (unlocked) unlockStatRepository.addNewUnlock(now()).subscribe()
        else unlockStatRepository.addNewLock(now()).subscribe()
    }

    /**
     * todo: check if km.isDeviceSecure for whether a screen lock is enabled.
     * also, move this out of here come on
     * */
    private fun isPhoneUnlocked(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return pm.isInteractive && !km.isDeviceLocked
    }

}
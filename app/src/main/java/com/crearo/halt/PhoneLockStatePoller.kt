package com.crearo.halt

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import com.crearo.halt.data.UnlockStatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.time.Instant.now
import javax.inject.Inject
import javax.inject.Named

class PhoneLockStatePoller @Inject constructor(@ApplicationContext val context: Context) {

    @Inject
    @Named("ticker")
    lateinit var tickerObservable: Observable<Long>

    @Inject
    lateinit var unlockStatRepository: UnlockStatRepository

    private val compositeDisposable = CompositeDisposable()

    fun start() {
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

    fun stop() {
        compositeDisposable.clear()
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
package com.crearo.halt.usecase

import android.content.Context
import com.crearo.halt.data.UnlockStatRepository
import com.crearo.halt.manager.FocusModeManager
import com.crearo.halt.pollers.Poller
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIXME: Hate that it is in a package called usecase. wtf.
 * Also, it shouldn't extend Poller.
 * It does however belong logically outside of pollers because its job is specific to my application.
 * The pollers are doing a job at a level below this.
 *
 * FocusModeSetter is responsible for setting FocusMode automatically based on application level
 * conditions. This is approx. at the View level in the arch hierarchy.
 **/
@Singleton
class FocusModeSetter @Inject constructor(@ApplicationContext context: Context) :
    Poller(context) {

    @Inject
    lateinit var unlockStatRepository: UnlockStatRepository

    @Inject
    lateinit var focusModeManager: FocusModeManager

    override fun start() {
        compositeDisposable.add(
            timeObservable
                .subscribe {
                    if (it.hour == 5 && it.minute == 0) {
                        focusModeManager.setFocusMode(true)
                    }

                    // todo move this out from here, this is shite. it should listen to the first
                    //  unlock event and then set an event for itself to listen to in the future
                    unlockStatRepository.getFirstUnlock(LocalDate.now())
                        .subscribe { unlockStat, _ ->
                            if (unlockStat != null
                                && Duration.between(unlockStat.unlockTime, Instant.now())
                                    .toMinutes() == 60L
                            ) {
                                focusModeManager.setFocusMode(false)
                            }
                        }
                }
        )
    }

}
package com.crearo.halt.pollers

import android.content.Context
import com.crearo.halt.manager.DndManager
import com.crearo.halt.manager.DndState
import com.crearo.halt.manager.FocusModeManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks the phone's DND state. Sets it back to DND if we are in Focus Mode.
 **/
@Singleton
class DndPoller @Inject constructor(@ApplicationContext context: Context) :
    Poller(context) {

    @Inject
    lateinit var dndManager: DndManager

    @Inject
    lateinit var focusModeManager: FocusModeManager

    override fun start() {
        compositeDisposable.add(tickerObservable
            .map { dndManager.isDndEnabled() }
            .distinctUntilChanged()
            .doOnNext { onDndStateChanged(it) }
            .subscribe()
        )
    }

    private fun onDndStateChanged(state: DndState) {
        Timber.d("DND State: $state")

        // this sets DND back to true if it changes when it should be maintained. For example when
        // the user tries to change it from UI
        if (focusModeManager.isFocusMode() && dndManager.isDndEnabled() == DndState.DISABLED) {
            dndManager.setDnd()
        }
    }

}


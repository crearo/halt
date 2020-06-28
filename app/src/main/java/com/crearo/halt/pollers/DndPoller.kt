package com.crearo.halt.pollers

import android.content.Context
import com.crearo.halt.DndRepository
import com.crearo.halt.rx.DndState
import com.crearo.halt.rx.DndStateBus
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks and broadcasts the phone's DND state. The broadcast event contains whether it was
 * triggered by the phone or the user.
 **/
@Singleton
class DndPoller @Inject constructor(@ApplicationContext context: Context) :
    Poller(context) {

    @Inject
    lateinit var dndStateBus: DndStateBus

    @Inject
    lateinit var dndRepository: DndRepository

    override fun start() {
        compositeDisposable.add(tickerObservable
            .map { dndRepository.isDndEnabled() }
            .distinctUntilChanged()
            .doOnNext { state ->
                val wasTriggeredByPhone = dndRepository.isTriggeredByPhone.getAndSet(false)
                onDndStateChanged(DndState(state, wasTriggeredByPhone))
            }
            .subscribe()
        )
    }

    private fun onDndStateChanged(state: DndState) {
        Timber.d("DND State: $state")
        dndStateBus.setState(state)
    }

}
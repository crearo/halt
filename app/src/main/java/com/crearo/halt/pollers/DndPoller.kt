package com.crearo.halt.pollers

import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class DndPoller @Inject constructor(@ApplicationContext context: Context) :
    Poller(context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun start() {
        compositeDisposable.add(tickerObservable
            .map { isDndEnabled() }
            .distinctUntilChanged()
            .doOnNext { state -> onDndStateChanged(state) }
            .subscribe()
        )
    }

    private fun onDndStateChanged(state: DndState) {
        Timber.d("DND State: $state")
    }

    private fun isDndEnabled(): DndState {
        if (!notificationManager.isNotificationPolicyAccessGranted) return DndState.PERMISSION_NOT_GRANTED
        return if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) DndState.DISABLED
        else DndState.ENABLED
    }

    private enum class DndState {
        PERMISSION_NOT_GRANTED,
        ENABLED,
        DISABLED
    }
}
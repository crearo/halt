package com.crearo.halt.pollers

import android.app.NotificationManager
import android.content.Context
import com.crearo.halt.rx.DndState
import com.crearo.halt.rx.DndStateBus
import com.crearo.halt.rx.DndStateEnum
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Tracks and broadcasts the phone's DND state. The broadcast event contains whether it was
 * triggered by the phone or the user.
 **/
class DndPoller @Inject constructor(@ApplicationContext context: Context) :
    Poller(context) {

    @Inject
    lateinit var dndStateBus: DndStateBus

    private val isTriggeredByPhone = AtomicBoolean(false)

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun start() {
        compositeDisposable.add(tickerObservable
            .map { isDndEnabled(isTriggeredByPhone.get()) }
            .distinctUntilChanged()
            .doOnNext { state -> onDndStateChanged(state) }
            .subscribe()
        )
    }

    private fun setDnd() {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            isTriggeredByPhone.set(true)
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
        }
    }

    private fun onDndStateChanged(state: DndState) {
        Timber.d("DND State: $state")
        dndStateBus.setState(state)
    }

    private fun isDndEnabled(phoneTriggered: Boolean): DndState {
        isTriggeredByPhone.set(false)
        if (!notificationManager.isNotificationPolicyAccessGranted) return DndState(
            DndStateEnum.PERMISSION_NOT_GRANTED,
            phoneTriggered
        )
        return if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) DndState(
            DndStateEnum.DISABLED,
            phoneTriggered
        )
        else DndState(DndStateEnum.ENABLED, phoneTriggered)
    }

}
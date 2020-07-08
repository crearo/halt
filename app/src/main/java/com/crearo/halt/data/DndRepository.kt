package com.crearo.halt.data

import android.app.NotificationManager
import android.content.Context
import com.crearo.halt.rx.DndStateEnum
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TODO: Fix name because this isn't just the data layer, but is also performing the task of setting DND
 * */
@Singleton
class DndRepository @Inject constructor(@ApplicationContext context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * This is a pretty bad way I'm using to track if the phone caused the DND, or if the user did
     * so by using the DND UI in Android Settings / Notification Panel
     **/
    val isTriggeredByPhone = AtomicBoolean(false)

    /**
     * This is so that the user can't use the DND UI to stop DND.
     **/
    val shouldBeMaintained = AtomicBoolean(false)

    fun setDnd() {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            isTriggeredByPhone.set(true)
            shouldBeMaintained.set(true)
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
        }
    }

    fun setNoDnd() {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            isTriggeredByPhone.set(true)
            shouldBeMaintained.set(false)
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    fun isDndEnabled(): DndStateEnum {
        if (!notificationManager.isNotificationPolicyAccessGranted) return DndStateEnum.PERMISSION_NOT_GRANTED
        return if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) DndStateEnum.DISABLED
        else DndStateEnum.ENABLED
    }

}

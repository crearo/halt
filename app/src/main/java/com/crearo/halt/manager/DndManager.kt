package com.crearo.halt.manager

import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Responsible for performing DND related operations with the Android System
 **/
class DndManager @Inject constructor(@ApplicationContext context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun setDnd() {
        if (hasPermissions()) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
        }
    }

    fun setNoDnd() {
        if (hasPermissions()) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    fun isDndEnabled(): DndStateEnum {
        if (!hasPermissions()) return DndStateEnum.PERMISSION_NOT_GRANTED
        return if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) DndStateEnum.DISABLED
        else DndStateEnum.ENABLED
    }

    fun hasPermissions(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

}

data class DndState(val dndStateEnum: DndStateEnum)

enum class DndStateEnum {
    PERMISSION_NOT_GRANTED,
    ENABLED,
    DISABLED
}

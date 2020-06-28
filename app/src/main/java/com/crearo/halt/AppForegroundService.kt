package com.crearo.halt

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.crearo.halt.pollers.DndPoller
import com.crearo.halt.pollers.DndSetter
import com.crearo.halt.pollers.PhoneLockStatePoller
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AppForegroundService : Service() {

    private val CHANNEL_ID = "AppForegroundService"

    @Inject
    lateinit var phoneLockStatePoller: PhoneLockStatePoller

    @Inject
    lateinit var dndSetter: DndSetter

    @Inject
    lateinit var dndStatePoller: DndPoller

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, AppForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, AppForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification: Notification =
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        phoneLockStatePoller.start()
        dndStatePoller.start()
        dndSetter.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        phoneLockStatePoller.stop()
        dndStatePoller.stop()
        dndSetter.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID, "Foreground Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager!!.createNotificationChannel(serviceChannel)
    }
}
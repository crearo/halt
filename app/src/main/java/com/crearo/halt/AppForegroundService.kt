package com.crearo.halt

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.crearo.halt.pollers.AppLaunchPoller
import com.crearo.halt.pollers.DndPoller
import com.crearo.halt.pollers.PhoneLockStatePoller
import com.crearo.halt.ui.MainActivity
import com.crearo.halt.usecase.AppLaunchBlocker
import com.crearo.halt.usecase.FocusModeSetter
import com.crearo.halt.usecase.IntentShower
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class AppForegroundService : Service() {

    @Inject
    lateinit var phoneLockStatePoller: PhoneLockStatePoller

    @Inject
    lateinit var focusModeSetter: FocusModeSetter

    @Inject
    lateinit var dndStatePoller: DndPoller

    @Inject
    lateinit var appLaunchPoller: AppLaunchPoller

    @Inject
    lateinit var intentShower: IntentShower

    @Inject
    lateinit var appLaunchBlocker: AppLaunchBlocker

    @Inject
    lateinit var analytics: Analytics

    companion object {
        private const val CHANNEL_ID = "AppForegroundService"

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
        focusModeSetter.start()
        appLaunchPoller.start()
        intentShower.start()
        appLaunchBlocker.start()
        analytics.sendStartForegroundService(Instant.now())
    }

    override fun onDestroy() {
        super.onDestroy()
        phoneLockStatePoller.stop()
        dndStatePoller.stop()
        focusModeSetter.stop()
        appLaunchPoller.stop()
        intentShower.stop()
        appLaunchBlocker.stop()
        analytics.sendStopForegroundService(Instant.now())
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
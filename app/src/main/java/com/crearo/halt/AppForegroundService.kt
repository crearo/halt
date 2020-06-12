package com.crearo.halt

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class AppForegroundService : Service() {

    private val CHANNEL_ID = "AppForegroundService"

    @Inject
    lateinit var phoneLockReceiver: PhoneLockBroadcastReceiver

    private val compositeDisposable = CompositeDisposable()

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
        registerReceivers()
        compositeDisposable.add(
            Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { isPhoneUnlocked() }
                .distinctUntilChanged()
                .doOnNext { value -> Timber.d("Interactive: $value") }
                .subscribe()
        )
    }

    /**
     * todo: check if km.isDeviceSecure for whether a screen lock is enabled.
     * also, move this out of here come on
     * */
    private fun isPhoneUnlocked(): Boolean {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return pm.isInteractive && !km.isDeviceLocked
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
        compositeDisposable.clear()
    }

    private fun registerReceivers() {
        val phoneLockIntentFilter = IntentFilter()
        phoneLockIntentFilter.addAction(ACTION_USER_PRESENT)
        phoneLockIntentFilter.addAction(ACTION_SCREEN_ON)
        phoneLockIntentFilter.addAction(ACTION_SCREEN_OFF)
        registerReceiver(phoneLockReceiver, phoneLockIntentFilter)
    }

    private fun unregisterReceivers() {
        unregisterReceiver(phoneLockReceiver)
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
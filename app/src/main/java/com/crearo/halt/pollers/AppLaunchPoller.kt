package com.crearo.halt.pollers

import android.content.Context
import com.crearo.halt.manager.AppTasksManager
import com.crearo.halt.rx.AppLaunchBus
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLaunchPoller @Inject constructor(@ApplicationContext context: Context) :
    Poller(context) {

    @Inject
    lateinit var appTasksManager: AppTasksManager

    @Inject
    lateinit var appLaunchBus: AppLaunchBus

    override fun start() {
        compositeDisposable.add(tickerObservable
            .map { appTasksManager.getCurrentlyRunningAppName() }
            .distinctUntilChanged()
            .doOnNext { onAppLaunched(it) }
            .subscribe()
        )
    }

    private fun onAppLaunched(packageName: String) {
        Timber.d("App Launched: $packageName")
        appLaunchBus.setAppLaunched(packageName)
    }

}
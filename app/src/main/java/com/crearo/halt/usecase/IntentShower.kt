package com.crearo.halt.usecase

import android.content.Context
import android.content.Intent
import com.crearo.halt.pollers.Poller
import com.crearo.halt.rx.PhoneLockStateBus
import com.crearo.halt.ui.ChooseIntentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentShower @Inject constructor(@ApplicationContext context: Context) :
    Poller(context) {

    @Inject
    lateinit var phoneLockStateBus: PhoneLockStateBus

    override fun start() {
        compositeDisposable.add(
            phoneLockStateBus.getState()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .skip(1) // skip the first because the app is already running
                .filter { !it }
                .subscribe {
                    val intent = Intent(context, ChooseIntentActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
        )
    }

}
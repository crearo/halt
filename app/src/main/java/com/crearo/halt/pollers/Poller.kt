package com.crearo.halt.pollers

import android.content.Context
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import javax.inject.Named

abstract class Poller(val context: Context) {

    @Inject
    @Named("ticker")
    lateinit var tickerObservable: Observable<Long>

    protected val compositeDisposable = CompositeDisposable()
    abstract fun start()

    fun stop() {
        compositeDisposable.clear()
    }

}
package com.crearo.halt.pollers

import android.content.Context
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Named

abstract class Poller(val context: Context) {

    @Inject
    @Named("ticker_300ms")
    lateinit var ticker300msObservable: Observable<Long>

    @Inject
    @Named("ticker")
    lateinit var tickerObservable: Observable<Long>

    @Inject
    @Named("time_ticker")
    lateinit var timeObservable: Observable<LocalTime>

    protected val compositeDisposable = CompositeDisposable()
    abstract fun start()

    fun stop() {
        compositeDisposable.clear()
    }

}
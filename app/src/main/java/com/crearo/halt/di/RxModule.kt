package com.crearo.halt.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object RxModule {

    @Provides
    @Singleton
    @Named("ticker")
    fun provideTickerObservable(): Observable<Long> {
        return Observable.interval(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .share()
    }

    /**
     * Definitely supremely inefficient
     **/
    @Provides
    @Singleton
    @Named("time_ticker")
    fun provideTimeTickerObservable(): Observable<LocalTime> {
        return Observable.interval(1, TimeUnit.SECONDS)
            .map { LocalTime.now() }
            .distinctUntilChanged { t1, t2 -> t1.hour == t2.hour && t1.minute == t2.minute }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .share()
    }

}
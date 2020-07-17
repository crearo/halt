package com.crearo.halt.rx

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLaunchBus @Inject constructor() {

    private val subject = PublishSubject.create<String>()

    fun setAppLaunched(packageName: String) {
        subject.onNext(packageName)
    }

    fun getTopApp(): Observable<String> {
        return subject
    }

}

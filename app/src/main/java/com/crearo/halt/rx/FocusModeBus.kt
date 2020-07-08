package com.crearo.halt.rx

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusModeBus @Inject constructor() {

    private val subject = PublishSubject.create<Boolean>()

    fun setState(enabled: Boolean) {
        subject.onNext(enabled)
    }

    fun getState(): Observable<Boolean> {
        return subject
    }

}

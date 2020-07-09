package com.crearo.halt.rx

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneLockStateBus @Inject constructor() {

    private val subject = PublishSubject.create<Boolean>()

    fun setState(unlocked: Boolean) {
        subject.onNext(unlocked)
    }

    /**
     * @return unlocked state
     **/
    fun getState(): Observable<Boolean> {
        return subject
    }

}

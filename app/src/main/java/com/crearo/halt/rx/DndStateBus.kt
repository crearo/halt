package com.crearo.halt.rx

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DndStateBus @Inject constructor() {

    private val subject = PublishSubject.create<DndState>()

    fun setState(dndState: DndState) {
        subject.onNext(dndState)
    }

    fun getState(): Observable<DndState> {
        return subject
    }

}

data class DndState(val dndStateEnum: DndStateEnum, val phoneTriggered: Boolean)

enum class DndStateEnum {
    PERMISSION_NOT_GRANTED,
    ENABLED,
    DISABLED
}

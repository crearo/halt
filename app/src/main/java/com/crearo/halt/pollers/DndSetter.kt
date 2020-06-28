package com.crearo.halt.pollers

import android.content.Context
import com.crearo.halt.data.DndRepository
import com.crearo.halt.rx.DndStateBus
import com.crearo.halt.rx.DndStateEnum
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIXME: Hate the name of this class
 **/
@Singleton
class DndSetter @Inject constructor(@ApplicationContext context: Context) :
    Poller(context) {

    @Inject
    lateinit var dndRepository: DndRepository

    @Inject
    lateinit var dndStateBus: DndStateBus

    override fun start() {
        compositeDisposable.add(
            timeObservable.subscribe {
                if (it.hour == 5 && it.minute == 0) {
                    dndRepository.setDnd()
                }
            }
        )

        // this sets DND back to true if it changes when it should be maintained. For example when
        // the user tries to change it from UI
        compositeDisposable.add(dndStateBus
            .getState()
            .subscribe {
                if (dndRepository.isDndEnabled() == DndStateEnum.DISABLED && dndRepository.shouldBeMaintained.get()) {
                    dndRepository.setDnd()
                }
            }
        )
    }

}
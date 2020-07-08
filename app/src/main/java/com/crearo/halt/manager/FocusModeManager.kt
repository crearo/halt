package com.crearo.halt.manager

import com.crearo.halt.data.FocusModeRepository
import com.crearo.halt.rx.FocusModeBus
import javax.inject.Inject

/**
 * This represents our intention to be in Focus Mode.
 * Focus Mode atm is either on or off.
 * When on, DND is on, and the user cannot open distracting apps.
 * Performs all actions to be done when FocusMode is set. All classes should use this when setting
 * Focus Mode.
 **/
class FocusModeManager @Inject constructor() {

    @Inject
    lateinit var focusModeRepository: FocusModeRepository

    @Inject
    lateinit var dndManager: DndManager

    @Inject
    lateinit var focusModeBus: FocusModeBus

    fun setFocusMode(enabled: Boolean) {
        focusModeRepository.setFocusMode(enabled)
        focusModeBus.setState(enabled)
        if (enabled) dndManager.setDnd() else dndManager.setNoDnd()
    }

    fun isFocusMode(): Boolean {
        return focusModeRepository.isFocusMode()
    }

}
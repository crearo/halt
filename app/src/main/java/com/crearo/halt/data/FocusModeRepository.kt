package com.crearo.halt.data

import android.content.Context
import com.crearo.halt.rx.FocusModeBus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * This represents our intention to be in Focus Mode.
 * Focus Mode atm is either on or off.
 * When on, DND is on, and the user cannot open distracting apps.
 **/
class FocusModeRepository @Inject constructor(@ApplicationContext context: Context) {

    @Inject
    lateinit var focusModeBus: FocusModeBus

    companion object {
        private const val KEY_FOCUS_MODE = "focus_mode"
    }

    val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    fun setFocusMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FOCUS_MODE, enabled).apply()
        focusModeBus.setState(enabled)
    }

    fun getFocusMode(): Boolean {
        return prefs.getBoolean(KEY_FOCUS_MODE, false)
    }

}
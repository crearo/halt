package com.crearo.halt.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * This should only be called by {@link FocusModeRepository}
 **/
class FocusModeRepository @Inject constructor(@ApplicationContext context: Context) {

    companion object {
        private const val KEY_FOCUS_MODE = "focus_mode"
    }

    private val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    fun setFocusMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FOCUS_MODE, enabled).apply()
    }

    fun isFocusMode(): Boolean {
        return prefs.getBoolean(KEY_FOCUS_MODE, false)
    }

}
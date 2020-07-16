package com.crearo.halt.ui

import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.crearo.halt.databinding.ActivityChooseIntentBinding
import com.crearo.halt.manager.FocusModeManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class ChooseIntentActivity : AppCompatActivity() {

    @Inject
    lateinit var focusModeManager: FocusModeManager

    private lateinit var binding: ActivityChooseIntentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseIntentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = ""
        binding.social.setOnClickListener {
            focusModeManager.setFocusMode(false)
            finish()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val view: View = window.decorView
        val lp = view.layoutParams as WindowManager.LayoutParams
        lp.gravity = Gravity.BOTTOM
        windowManager.updateViewLayout(view, lp)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN && event.y < 0) {
            Timber.d("Touched outside")
            focusModeManager.setFocusMode(true)
        }
        return super.onTouchEvent(event)
    }

}
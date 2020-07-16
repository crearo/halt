package com.crearo.halt.ui

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.crearo.halt.databinding.ActivityChooseIntentBinding
import com.crearo.halt.manager.FocusModeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ChooseIntentActivity : AppCompatActivity() {

    @Inject
    lateinit var focusModeManager: FocusModeManager

    private lateinit var binding: ActivityChooseIntentBinding
    private var stayInSocialMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseIntentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = ""
        binding.social.setOnClickListener {
            stayInSocialMode = true
            focusModeManager.setFocusMode(false)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!stayInSocialMode) {
            focusModeManager.setFocusMode(true)
        }
        stayInSocialMode = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val view: View = window.decorView
        val lp = view.layoutParams as WindowManager.LayoutParams
        lp.gravity = Gravity.BOTTOM
        windowManager.updateViewLayout(view, lp)
    }

}
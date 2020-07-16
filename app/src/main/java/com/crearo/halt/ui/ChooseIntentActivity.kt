package com.crearo.halt.ui

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.crearo.halt.databinding.ActivityChooseIntentBinding
import com.crearo.halt.manager.FocusModeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random


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

        val randomButton = if (Random(10).nextBoolean()) binding.btnLeft else binding.btnRight
        setupFocusButton(if (randomButton == binding.btnLeft) binding.btnLeft else binding.btnRight)
        setupSocialButton(if (randomButton == binding.btnLeft) binding.btnRight else binding.btnLeft)
    }

    private fun setupSocialButton(button: Button) {
        button.text = "Social"
        button.setOnClickListener { focusModeManager.setFocusMode(false) }
    }

    private fun setupFocusButton(button: Button) {
        button.text = "Focus"
        button.setOnClickListener { focusModeManager.setFocusMode(true) }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val view: View = window.decorView
        val lp = view.layoutParams as WindowManager.LayoutParams
        lp.gravity = Gravity.BOTTOM
        windowManager.updateViewLayout(view, lp)
    }

}
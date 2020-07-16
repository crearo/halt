package com.crearo.halt.ui

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.crearo.halt.databinding.ActivityChooseIntentBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChooseIntentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseIntentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseIntentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = ""
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val view: View = window.decorView
        val lp = view.layoutParams as WindowManager.LayoutParams
        lp.gravity = Gravity.BOTTOM
        windowManager.updateViewLayout(view, lp)
    }

}
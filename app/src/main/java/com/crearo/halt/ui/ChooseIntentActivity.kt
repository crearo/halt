package com.crearo.halt.ui

import android.os.Bundle
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
    }

}
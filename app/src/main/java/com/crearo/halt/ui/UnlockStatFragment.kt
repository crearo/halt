package com.crearo.halt.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.crearo.halt.databinding.FragmentUnlockStatBinding

class UnlockStatFragment : Fragment() {

    private lateinit var binding: FragmentUnlockStatBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUnlockStatBinding.inflate(layoutInflater)
        return binding.root
    }

}
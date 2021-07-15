package com.kabos.spotifydj.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kabos.spotifydj.databinding.FragmentRecommendBinding

class RecommendFragment: Fragment() {

    private lateinit var binding: FragmentRecommendBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecommendBinding.inflate(inflater, container, false)
        return binding.root
    }
}

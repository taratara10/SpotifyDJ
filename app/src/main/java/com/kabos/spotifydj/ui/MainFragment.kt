package com.kabos.spotifydj.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.kabos.spotifydj.databinding.FragmentMainBinding
import com.kabos.spotifydj.viewModel.UserViewModel

class MainFragment: Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var accessToken: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accessToken = requireActivity().getSharedPreferences("SPOTIFY", 0)
            .getString("token", "No token").toString()

        binding.apply {
            button2.setOnClickListener {
                textView3.text = viewModel.getUser(accessToken).toString()
            }
        }
    }
}

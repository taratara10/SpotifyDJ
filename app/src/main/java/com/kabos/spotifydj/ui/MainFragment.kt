package com.kabos.spotifydj.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.kabos.spotifydj.databinding.FragmentMainBinding
import com.kabos.spotifydj.ui.adapter.ViewPagerAdapter
import com.kabos.spotifydj.viewModel.UserViewModel

class MainFragment: Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setup viewPager
        val viewPager = binding.pager
        viewPager.adapter = ViewPagerAdapter(this)

        //setup TabLayout
        val tabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            tab.text = "OBJECT $position"
        }.attach()






//        accessToken = requireActivity().getSharedPreferences("SPOTIFY", 0)
//            .getString("token", "No token").toString()

//        binding.apply {
//            button2.setOnClickListener {
////                textView3.text = viewModel.getUser(accessToken).toString()
//                viewModel.getPlaylist(accessToken)
//                viewModel.playback(accessToken)
//            }
//
//            playBtn.setOnClickListener {
//                    viewModel.getCurrentPlayback(accessToken)
//            }
//        }
    }
}



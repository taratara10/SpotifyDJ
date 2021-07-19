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
    private val viewModel: UserViewModel by activityViewModels()

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

        //currentTrackの変更を監視して、自動的にrecommendFragmentへ遷移
        viewModel.currentTrack.observe(viewLifecycleOwner,{
            viewPager.setCurrentItem(1,true)
        })

        //playlistを監視して、playlistFragmentへ遷移
        viewModel.currentPlaylist.observe(viewLifecycleOwner,{
            viewPager.setCurrentItem(2,true)
        })

    }
}



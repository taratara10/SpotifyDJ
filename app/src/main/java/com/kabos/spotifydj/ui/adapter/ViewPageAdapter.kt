package com.kabos.spotifydj.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kabos.spotifydj.ui.PlaylistFragment
import com.kabos.spotifydj.ui.RecommendFragment
import com.kabos.spotifydj.ui.SearchFragment

class ViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment =
        when(position) {
            0 -> SearchFragment()
            1 -> RecommendFragment()
            else -> PlaylistFragment()
        }
}

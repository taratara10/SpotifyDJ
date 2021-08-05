package com.kabos.spotifydj.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.FragmentMainBinding
import com.kabos.spotifydj.ui.adapter.ViewPagerAdapter
import com.kabos.spotifydj.viewModel.UserViewModel

class MainFragment: Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
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
            //todo titleを変える
            tab.text = "OBJECT $position"
        }.attach()

        //currentTrackの変更を監視して、自動的にrecommendFragmentへ遷移
        viewModel.apply {
            isNavigateRecommendFragment.observe(viewLifecycleOwner,{ isNavigate ->
                if (isNavigate && !viewModel.isNavigatePlaylistFragment.value!!){
                    viewPager.setCurrentItem(1,true)
                    viewModel.isNavigateRecommendFragment.postValue(false)
                }
            })

            //playlistを監視して、playlistFragmentへ遷移
            isNavigatePlaylistFragment.observe(viewLifecycleOwner,{ isNavigate ->
                if (isNavigate) {
                    viewPager.setCurrentItem(2,true)
                    viewModel.isNavigatePlaylistFragment.postValue(false)
                }
            })
        }

        val accessToken = requireActivity().getSharedPreferences("SPOTIFY", 0)
            .getString("token", "No token").toString()
        viewModel.initializeAccessToken(accessToken)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_new_playlist -> {
                Log.d("isLoadingDowner","${viewModel.isLoadingDownerTrack.value}")

                true
            }
            R.id.menu_fetch_playlist -> {
                viewModel.getUsersAllPlaylists()
                findNavController().navigate(R.id.action_nav_main_to_nav_user_playlist)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}



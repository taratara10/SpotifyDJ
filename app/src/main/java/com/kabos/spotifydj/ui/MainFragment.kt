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
import com.google.android.material.tabs.TabLayoutMediator
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.FragmentMainBinding
import com.kabos.spotifydj.ui.adapter.ViewPagerAdapter
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment: Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setup viewPager
        val viewPager = binding.pager
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        //setup TabLayout
        val tabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            when(position){
                0 -> tab.text = "Search"
                1 -> tab.text = "Recommend"
                2 -> tab.text = "Playlist"
            }
        }.attach()


        //currentTrackの変更を監視して、自動的にrecommendFragmentへ遷移
        viewModel.apply {
            isNavigateSearchFragment.observe(viewLifecycleOwner,{ isNavigate ->
                if (isNavigate){
                    viewPager.setCurrentItem(0,true)
                    viewModel.isNavigateSearchFragment.postValue(false)
                }
            })

            isNavigateRecommendFragment.observe(viewLifecycleOwner,{ isNavigate ->
                //viewModel#AddTrack() でisNavigatePlaylist(true)
                //viewModel#updateCurrentTrack()でisNavigateRecommend(true)
                //viewModel#AddTrack()の内部でupdateCurrentTrack()が呼ばれてrecommendが更新されてnavigateが競合する
                //AddTrack()から呼ばれたupdateTrack()の場合は遷移しない。
                if (isNavigate && !viewModel.isNavigatePlaylistFragment.value!!){
                    viewPager.setCurrentItem(1,true)
                    viewModel.isNavigateRecommendFragment.postValue(false)
                }
            })

            isNavigatePlaylistFragment.observe(viewLifecycleOwner,{ isNavigate ->
                if (isNavigate) {
                    viewPager.setCurrentItem(2,true)
                    viewModel.isNavigatePlaylistFragment.postValue(false)
                }
            })
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_new_playlist -> {
                Log.d("mUserId","${viewModel.mUserName} /${viewModel.mUserId}")
                viewPagerAdapter.addFragment(1)
                viewPagerAdapter.notifyDataSet()

                true
            }
            R.id.menu_fetch_playlist -> {
                viewModel.getAllPlaylists()
                findNavController().navigate(R.id.action_nav_main_to_nav_user_playlist)
                true
            }
            R.id.menu_restart_playlist -> {
                viewPagerAdapter.addFragment(2)
                viewPagerAdapter.notifyDataSet()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}


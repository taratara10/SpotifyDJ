package com.kabos.spotifydj.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.FragmentMainBinding
import com.kabos.spotifydj.ui.adapter.ViewPagerAdapter
import com.kabos.spotifydj.util.FragmentList
import com.kabos.spotifydj.util.ReplaceFragment
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment: Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
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
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        //setup TabLayout
        val tabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            when(position){
                FragmentList.Search.position -> tab.text = FragmentList.Search.name
                FragmentList.Recommend.position -> tab.text = FragmentList.Recommend.name
                FragmentList.Playlist.position -> tab.text = FragmentList.Recommend.name
            }
        }.attach()


        //currentTrackの変更を監視して、自動的にrecommendFragmentへ遷移
        viewModel.apply {
            isNavigateSearchFragment.observe(viewLifecycleOwner,{ isNavigate ->
                if (isNavigate){
                    viewPager.setCurrentItem(FragmentList.Search.position,true)
                    viewModel.isNavigateSearchFragment.postValue(false)
                }
            })

            isNavigateRecommendFragment.observe(viewLifecycleOwner,{ isNavigate ->
                //viewModel#AddTrack() でisNavigatePlaylist(true)
                //viewModel#updateCurrentTrack()でisNavigateRecommend(true)
                //viewModel#AddTrack()の内部でupdateCurrentTrack()が呼ばれてrecommendが更新されてnavigateが競合する
                //AddTrack()から呼ばれたupdateTrack()の場合は遷移しない。
                if (isNavigate && !viewModel.isNavigatePlaylistFragment.value!!){
                    viewPager.setCurrentItem(FragmentList.Recommend.position,true)
                    viewModel.isNavigateRecommendFragment.postValue(false)
                }
            })

            isNavigatePlaylistFragment.observe(viewLifecycleOwner,{ isNavigate ->
                if (isNavigate) {
                    viewPager.setCurrentItem(FragmentList.Playlist.position,true)
                    viewModel.isNavigatePlaylistFragment.postValue(false)
                }
            })

            isNavigateNewPlaylistFragment.observe(viewLifecycleOwner,{ isNavigate ->
                if (isNavigate){
                    viewPagerAdapter.replaceFragment(ReplaceFragment.NewPlaylist)
                    //fragmentをreplaceした後なので、一旦別のfragment行って更新してから戻ってくる
                    viewPager.setCurrentItem(FragmentList.Search.position, true)
                    viewPager.setCurrentItem(FragmentList.Playlist.position,true)
                }
            })

            isNavigateExistingPlaylistFragment.observe(viewLifecycleOwner,{ isNavigate ->
                if (isNavigate) {
                    viewPagerAdapter.replaceFragment(ReplaceFragment.ExistingPlaylist)
                    viewPager.setCurrentItem(FragmentList.Search.position, true)
                    viewPager.setCurrentItem(FragmentList.Playlist.position, true)
                }
            })

            replaceFragmentFlag.observe(viewLifecycleOwner,{ replace ->
                viewPagerAdapter.replaceFragment(replace)
                viewPager.setCurrentItem(FragmentList.Search.position, true)
                viewPager.setCurrentItem(FragmentList.Playlist.position, true)
            })
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_new_playlist -> {
                binding.pager.setCurrentItem(0, true)
                true
            }
            R.id.menu_fetch_playlist -> {
                viewPagerAdapter.replaceFragment(ReplaceFragment.ResetPlaylist)
                //todo resetPlaylist でisNavigate New/Existing postValue(false)
                true
            }
            R.id.menu_restart_playlist -> {
                viewPagerAdapter.replaceFragment(ReplaceFragment.ExistingPlaylist)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}


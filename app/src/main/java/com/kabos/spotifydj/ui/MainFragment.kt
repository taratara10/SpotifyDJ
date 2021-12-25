package com.kabos.spotifydj.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
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
    private val viewPagerAdapter: ViewPagerAdapter by lazy { ViewPagerAdapter(this) }
    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()

        binding.apply {
            pager.adapter = viewPagerAdapter
            initTabLayoutLabel(tabLayout, pager)
        }
    }

    override fun onStart() {
        super.onStart()
        //復帰した時にaccessTokenをrefresh
        viewModel.needRefreshAccessToken.postValue(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_new_playlist -> {
                startActivity(
                    Intent().setComponent(
                        ComponentName("com.spotify.music",
                            "com.spotify.music.MainActivity")))

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

    private fun initTabLayoutLabel(tabLayout: TabLayout, pager: ViewPager2) {
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            when (position) {
                FragmentList.Search.position -> tab.text = FragmentList.Search.name
                FragmentList.Recommend.position -> tab.text = FragmentList.Recommend.name
                FragmentList.Playlist.position -> tab.text = FragmentList.Recommend.name
            }
        }.attach()
    }

    private fun initViewModels() {
        viewModel.apply {
            val viewPager = binding.pager
            isNavigateSearchFragment.observe(viewLifecycleOwner) { isNavigate ->
                if (isNavigate){
                    viewPager.setCurrentItem(FragmentList.Search.position,true)
                    // todo oneShotにする
                    viewModel.isNavigateSearchFragment.postValue(false)
                }
            }

            isNavigateRecommendFragment.observe(viewLifecycleOwner) { isNavigate ->
                //viewModel#AddTrack() でisNavigatePlaylist(true)
                //viewModel#updateCurrentTrack()でisNavigateRecommend(true)
                //viewModel#AddTrack()の内部でupdateCurrentTrack()が呼ばれてrecommendが更新されてnavigateが競合する
                //AddTrack()から呼ばれたupdateTrack()の場合は遷移しない。
                if (isNavigate && !viewModel.isNavigatePlaylistFragment.value!!){
                    viewPager.setCurrentItem(FragmentList.Recommend.position,true)
                    viewModel.isNavigateRecommendFragment.postValue(false)
                }
            }

            isNavigatePlaylistFragment.observe(viewLifecycleOwner) { isNavigate ->
                if (isNavigate) {
                    viewPager.setCurrentItem(FragmentList.Playlist.position,true)
                    viewModel.isNavigatePlaylistFragment.postValue(false)
                }
            }

            isNavigateNewPlaylistFragment.observe(viewLifecycleOwner) { isNavigate ->
                if (isNavigate){
                    viewPagerAdapter.replaceFragment(ReplaceFragment.NewPlaylist)
                    //fragmentをreplaceした後なので、一旦別のfragment行って更新してから戻ってくる
                    viewPager.setCurrentItem(FragmentList.Search.position, true)
                    viewPager.setCurrentItem(FragmentList.Playlist.position,true)
                }
            }

            isNavigateExistingPlaylistFragment.observe(viewLifecycleOwner) { isNavigate ->
                if (isNavigate) {
                    viewPagerAdapter.replaceFragment(ReplaceFragment.ExistingPlaylist)
                    viewPager.setCurrentItem(FragmentList.Search.position, true)
                    viewPager.setCurrentItem(FragmentList.Playlist.position, true)
                }
            }

        }
    }
}


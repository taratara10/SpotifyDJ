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
import com.kabos.spotifydj.util.Pager
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
                Pager.Search.position -> tab.text = Pager.Search.name
                Pager.Recommend.position -> tab.text = Pager.Recommend.name
                Pager.Playlist.position -> tab.text = Pager.Recommend.name
            }
        }.attach()
    }

    private fun initViewModels() {
        viewModel.apply {
            val viewPager = binding.pager

            setRootFragmentPagerPosition.observe(viewLifecycleOwner) { event ->
                event.getContentIfNotHandled()?.let { pager ->
                    viewPager.setCurrentItem(pager.position, true)
                }
            }

            isNavigateNewPlaylistFragment.observe(viewLifecycleOwner) { isNavigate ->
                if (isNavigate){
                    viewPagerAdapter.replaceFragment(ReplaceFragment.NewPlaylist)
                    //fragmentをreplaceした後なので、一旦別のfragment行って更新してから戻ってくる
                    viewPager.setCurrentItem(Pager.Search.position, true)
                    viewPager.setCurrentItem(Pager.Playlist.position,true)
                }
            }

            isNavigateExistingPlaylistFragment.observe(viewLifecycleOwner) { isNavigate ->
                if (isNavigate) {
                    viewPagerAdapter.replaceFragment(ReplaceFragment.ExistingPlaylist)
                    viewPager.setCurrentItem(Pager.Search.position, true)
                    viewPager.setCurrentItem(Pager.Playlist.position, true)
                }
            }

        }
    }
}


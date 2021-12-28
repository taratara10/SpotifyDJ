package com.kabos.spotifydj.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kabos.spotifydj.ui.*
import com.kabos.spotifydj.util.Pager
import com.kabos.spotifydj.util.ReplaceFragment

class ViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    private var fragmentList = mutableListOf<Fragment>()
    private var idsList = mutableListOf<Long>()

    init {
        fragmentList.apply {
            add(SearchFragment())
            add(RecommendFragment())
            add(PlaylistMainFragment())
            forEach {
                idsList.add(it.hashCode().toLong())
            }
        }
    }

    override fun getItemCount(): Int = idsList.size

    override fun getItemId(position: Int): Long = idsList[position]

    override fun containsItem(itemId: Long): Boolean = idsList.contains(itemId)

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    fun replaceFragment(pattern: ReplaceFragment){
        when (pattern) {
            ReplaceFragment.NewPlaylist -> {
                fragmentList.removeAt(Pager.Playlist.position)
                fragmentList.add(Pager.Playlist.position, EditNewPlaylistFragment())
            }
            ReplaceFragment.ExistingPlaylist -> {
                fragmentList.removeAt(Pager.Playlist.position)
                fragmentList.add(Pager.Playlist.position, EditExistingPlaylistFragment())
            }
            ReplaceFragment.ResetPlaylist -> {
                fragmentList.removeAt(Pager.Playlist.position)
                fragmentList.add(Pager.Playlist.position, PlaylistMainFragment())
            }
        }

        //Assign unique id to each fragment
        idsList.clear()
        fragmentList.forEach {
            idsList.add(it.hashCode().toLong())
        }

        notifyDataSet(pattern)
    }

    private fun notifyDataSet(pattern: ReplaceFragment) {
        notifyItemRemoved(Pager.Playlist.position)
        notifyItemInserted(Pager.Playlist.position)
    }
}


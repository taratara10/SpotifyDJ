package com.kabos.spotifydj.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kabos.spotifydj.ui.*
import com.kabos.spotifydj.util.FragmentList
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

        if (pattern == ReplaceFragment.NewPlaylist) {
            fragmentList.removeAt(FragmentList.Playlist.position)
            fragmentList.add(FragmentList.Playlist.position, EditNewPlaylistFragment())
        }
        if (pattern == ReplaceFragment.ExistingPlaylist){
            fragmentList.removeAt(FragmentList.Playlist.position)
            fragmentList.add(FragmentList.Playlist.position, EditExistingPlaylistFragment())
        }
        if (pattern == ReplaceFragment.ResetPlaylist){
            fragmentList.removeAt(FragmentList.Playlist.position)
            fragmentList.add(FragmentList.Playlist.position, PlaylistMainFragment())
        }

        //Assign unique id to each fragment
        idsList.clear()
        fragmentList.forEach {
            idsList.add(it.hashCode().toLong())
        }

        notifyDataSet(pattern)
    }

    private fun notifyDataSet(pattern: ReplaceFragment){
        if (   pattern == ReplaceFragment.NewPlaylist
            || pattern == ReplaceFragment.ExistingPlaylist
            || pattern ==ReplaceFragment.ResetPlaylist){
            notifyItemRemoved(FragmentList.Playlist.position)
            notifyItemInserted(FragmentList.Playlist.position)
        }

    }
}


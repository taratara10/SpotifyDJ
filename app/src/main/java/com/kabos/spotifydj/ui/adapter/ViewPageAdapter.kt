package com.kabos.spotifydj.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kabos.spotifydj.ui.*
import com.kabos.spotifydj.util.Pager

class ViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    private val fragmentList = mutableListOf<Fragment>()
    private val idsList = mutableListOf<Long>()

    init {
        fragmentList.apply {
            add(SearchFragment())
            add(RecommendFragment())
            add(PlaylistFragment())
            forEach {
                idsList.add(it.hashCode().toLong())
            }
        }
    }

    override fun getItemCount(): Int = idsList.size

    override fun getItemId(position: Int): Long = idsList[position]

    override fun containsItem(itemId: Long): Boolean = idsList.contains(itemId)

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    fun replaceFragment(pager: Pager){
        when (pager) {
            Pager.EditPlaylist -> {
                fragmentList.removeAt(Pager.Playlist.position)
                fragmentList.add(Pager.Playlist.position, EditPlaylistFragment())
            }

            Pager.Playlist -> {
                fragmentList.removeAt(Pager.Playlist.position)
                fragmentList.add(Pager.Playlist.position, PlaylistFragment())
            }
        }

        //Assign unique id to each fragment
        idsList.clear()
        fragmentList.forEach {
            idsList.add(it.hashCode().toLong())
        }

        notifyDataSet()
    }

    private fun notifyDataSet() {
        notifyItemRemoved(Pager.Playlist.position)
        notifyItemInserted(Pager.Playlist.position)
    }
}


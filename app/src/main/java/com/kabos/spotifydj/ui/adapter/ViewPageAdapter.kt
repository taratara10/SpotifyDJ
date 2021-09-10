package com.kabos.spotifydj.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.kabos.spotifydj.ui.*
import com.kabos.spotifydj.util.ReplaceFragment

class ViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    private var fragmentList = mutableListOf<Fragment>()
    private var idsList = mutableListOf<Long>()


    init {
        fragmentList.apply {
            add(PlaylistFragment())
            add(RecommendFragment())
            add(SearchFragment())
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
            fragmentList.removeFirst()
            fragmentList.add(0, EditNewPlaylistFragment())
        }
        if (pattern == ReplaceFragment.ExistingPlaylist){
            fragmentList.removeFirst()
            fragmentList.add(0, EditExistingPlaylistFragment())
        }

        //Assign unique id to each fragment
        idsList.clear()
        fragmentList.forEach {
            idsList.add(it.hashCode().toLong())
        }

        notifyDataSet(pattern)
    }

    fun notifyDataSet(pattern: ReplaceFragment){
        if(pattern == ReplaceFragment.NewPlaylist){
            notifyItemRemoved(0)
            notifyItemInserted(0)
        }

        if (pattern == ReplaceFragment.ExistingPlaylist){
            notifyItemRemoved(0)
            notifyItemInserted(0)
        }
    }
}


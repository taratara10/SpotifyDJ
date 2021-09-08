package com.kabos.spotifydj.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.kabos.spotifydj.ui.*

class ViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    private var itemCount = 1
    private var fragmentMutableList = mutableListOf<Fragment>()
    private var idsMutableList = mutableListOf<Long>()


    init {
        fragmentMutableList.apply {
            add(PlaylistFragment())
            add(RecommendFragment())
            add(SearchFragment())
            forEach {
                idsMutableList.add(it.hashCode().toLong())
            }
        }
    }

    override fun getItemCount(): Int = idsMutableList.size

    override fun getItemId(position: Int): Long = idsMutableList[position]

    override fun containsItem(itemId: Long): Boolean = idsMutableList.contains(itemId)

    override fun createFragment(position: Int): Fragment = fragmentMutableList[position]

    //Add/Remove based on triggered logic
    fun addFragment(itemCount: Int){
        this.itemCount = itemCount
        //末尾のFragmentを削除
        fragmentMutableList.removeFirst()
        idsMutableList.removeFirst()

        if (itemCount == 1) {
            fragmentMutableList.add(0, EditNewPlaylistFragment())
        } else {
            fragmentMutableList.add(0, EditExistingPlaylistFragment())
        }

        //Assign unique id to each fragment
        fragmentMutableList.first() {
            idsMutableList.add(it.hashCode().toLong())
        }
    }
    fun notifyDataSet(){
        if(itemCount == 1){
            notifyItemRemoved(0)
            notifyItemInserted(0)
        }else{
            notifyItemRemoved(0)
            notifyItemInserted(0)
        }
    }
}


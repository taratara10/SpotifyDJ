package com.kabos.spotifydj.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.kabos.spotifydj.ui.PlaylistFragment
import com.kabos.spotifydj.ui.RecommendFragment
import com.kabos.spotifydj.ui.SearchFragment

class ViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment =
        when(position) {
            0 -> SearchFragment()
            1 -> RecommendFragment()
            else -> PlaylistFragment()
        }
}
class SearchAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    private var itemCount = 1
    private var fragmentMutableList = mutableListOf<Fragment>()
    private var idsMutableList = mutableListOf<Long>()


    init {
        fragmentMutableList.apply {
            add(SearchFragment())
            add(RecommendFragment())
            forEach {
                idsMutableList.add(it.hashCode().toLong())
            }
        }
    }

    override fun getItemCount(): Int {
        return idsMutableList.size
    }
    override fun getItemId(position: Int): Long {
        return idsMutableList[position]
    }
    override fun containsItem(itemId: Long): Boolean {
        return idsMutableList.contains(itemId)
    }


    override fun createFragment(position: Int): Fragment {
        return fragmentMutableList[position]
    }
    //Add/Remove based on triggered logic
    fun addFragment(itemCount: Int){
        this.itemCount = itemCount
        //末尾のFragmentを削除
        fragmentMutableList.removeLast()
        idsMutableList.removeLast()
        if (itemCount == 1) {
            fragmentMutableList.add(PlaylistFragment())
        } else {
            fragmentMutableList.add(SearchFragment())
        }
    //Assign unique id to each fragment
        fragmentMutableList.last() {
            idsMutableList.add(it.hashCode().toLong())
        }
    }
    fun notifyDataSet(){
        if(itemCount == 1){
            notifyItemRemoved(1)
            notifyItemInserted(1)
        }else{
            notifyItemRemoved(1)
            notifyItemInserted(1)
        }
    }
}


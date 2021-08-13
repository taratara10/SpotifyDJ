package com.kabos.spotifydj.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kabos.spotifydj.databinding.FragmentRecommendBinding
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.ui.adapter.TrackAdapter
import com.kabos.spotifydj.viewModel.UserViewModel
class RecommendFragment: Fragment() {

    private lateinit var binding: FragmentRecommendBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val currentTrackAdapter by lazy { TrackAdapter(viewModel.callback) }
    private val upperTrackAdapter by lazy { TrackAdapter(viewModel.callback) }
    private val downerTrackAdapter  by lazy { TrackAdapter(viewModel.callback) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecommendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       binding.apply {
           rvUpperTracksResult.apply {
               layoutManager = LinearLayoutManager(activity)
               adapter = upperTrackAdapter
           }

           rvDownerTracksResult.apply {
               layoutManager = LinearLayoutManager(activity)
               adapter = downerTrackAdapter
           }
           rvCurrentTracks.apply{
               layoutManager = LinearLayoutManager(activity)
               adapter = currentTrackAdapter
           }

           button.setOnClickListener {
           }

           viewModel.apply {
               upperTrackList.observe(viewLifecycleOwner,{upperTrackList ->
                   upperTrackAdapter.submitList(upperTrackList)
                   updateEmptyTextView(upperTrackList,tvUpperTracksNothing)
               })

               downerTrackList.observe(viewLifecycleOwner,{downerTrackList ->
                   downerTrackAdapter.submitList(downerTrackList)
                   updateEmptyTextView(downerTrackList,tvDownerTracksNothing)
               })

               currentTrack.observe(viewLifecycleOwner,{currentTrack ->
                   //adapterがList<TrackInfo>で受け取るので、Listでラップする
                   val list = listOf(currentTrack)
                   currentTrackAdapter.submitList(list)
                   updateEmptyTextView(list, binding.tvCurrentTracksEmpty)
                   updateEmptyTextView(list,tvUpperTracksEmpty)
                   updateEmptyTextView(list,tvDownerTracksEmpty)
               })
               isLoadingUpperTrack.observe(viewLifecycleOwner,{isLoading ->
                   updateProgressBar(isLoading,pbUpperProgress)
               })
               isLoadingDownerTrack.observe(viewLifecycleOwner,{isLoading ->
                   updateProgressBar(isLoading,pbDownerProgress)
               })
           }
        }
    }

    private fun updateEmptyTextView(item: List<TrackInfo?>?, textView: TextView){
        if (item.isNullOrEmpty())textView.visibility = View.VISIBLE
        else textView.visibility = View.GONE
    }

    private fun updateProgressBar(isLoading:Boolean, progressBar: ProgressBar){
        if (isLoading)progressBar.visibility = View.VISIBLE
        else progressBar.visibility = View.GONE
    }
}

package com.kabos.spotifydj.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kabos.spotifydj.databinding.FragmentRecommendBinding
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
               viewModel.updateRecommendTrack()
           }
       }

        viewModel.apply {
            upperTrackList.observe(viewLifecycleOwner,{upperTrack ->
                upperTrackAdapter.submitList(upperTrack)
            })

            downerTrackList.observe(viewLifecycleOwner,{downerTrack ->
                downerTrackAdapter.submitList(downerTrack)
            })

            currentTrack.observe(viewLifecycleOwner,{currentTrack ->
                //adapterがList<TrackInfo>で受け取るので、Listでラップする
                val list = listOf(currentTrack)
                currentTrackAdapter.submitList(list)
            })

            isLoadingUpperTrack.observe(viewLifecycleOwner,{isLoading ->
                if (isLoading) binding.pbUpperProgress.visibility = View.VISIBLE
                else binding.pbUpperProgress.visibility = View.GONE
            })

            isLoadingDownerTrack.observe(viewLifecycleOwner,{isLoading ->
                if (isLoading) binding.pbDownerProgress.visibility = View.VISIBLE
                else binding.pbDownerProgress.visibility = View.GONE
            })

        }
    }
}

package com.kabos.spotifydj.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kabos.spotifydj.databinding.FragmentRecommendBinding
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.ui.adapter.TrackAdapter
import com.kabos.spotifydj.util.callback.TrackCallback
import com.kabos.spotifydj.viewModel.RecommendViewModel
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecommendFragment: Fragment() {
    private lateinit var binding: FragmentRecommendBinding
    private val recommendViewModel: RecommendViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val currentTrackAdapter by lazy { TrackAdapter(callback) }
    private val upperTrackAdapter by lazy { TrackAdapter(callback) }
    private val downerTrackAdapter  by lazy { TrackAdapter(callback) }
    private val callback = object : TrackCallback {
        override fun addTrack(trackInfo: TrackInfo) {
            userViewModel.addTrackToEditingPlaylist(trackInfo)
        }

        override fun playback(trackInfo: TrackInfo) {
            userViewModel.playbackTrack(trackInfo)
        }

        override fun onClick(trackInfo: TrackInfo) {
            recommendViewModel.updateCurrentTrack(trackInfo)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecommendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        binding.apply {
            rvUpperTracksResult.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = upperTrackAdapter
                addItemDecoration(
                    DividerItemDecoration(
                        activity,
                        LinearLayoutManager(activity).orientation
                    )
                )
            }

            rvDownerTracksResult.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = downerTrackAdapter
                addItemDecoration(
                    DividerItemDecoration(
                        activity,
                        LinearLayoutManager(activity).orientation
                    )
                )
            }
            rvCurrentTracks.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = currentTrackAdapter
            }
        }
    }

    private fun initViewModels() {
        recommendViewModel.apply {
            upperTracks.observe(viewLifecycleOwner) { tracks ->
                upperTrackAdapter.submitList(tracks)
                updateEmptyTextView(tracks, binding.tvUpperTracksNothing)
            }

            downerTracks.observe(viewLifecycleOwner){ tracks ->
                downerTrackAdapter.submitList(tracks)
                updateEmptyTextView(tracks, binding.tvDownerTracksNothing)
            }

            currentTrack.observe(viewLifecycleOwner){currentTrack ->
                //adapterがList<TrackInfo>で受け取るので、Listでラップする
                val list = listOf(currentTrack)
                currentTrackAdapter.submitList(list)
                updateEmptyTextView(list, binding.tvCurrentTracksEmpty)
                updateEmptyTextView(list,binding.tvUpperTracksEmpty)
                updateEmptyTextView(list,binding.tvDownerTracksEmpty)
            }
            isLoadingUpperTrack.observe(viewLifecycleOwner){isLoading ->
                updateProgressBar(isLoading,binding.pbUpperProgress)
            }
            isLoadingDownerTrack.observe(viewLifecycleOwner){isLoading ->
                updateProgressBar(isLoading,binding.pbDownerProgress)
            }
        }
    }

    private fun updateEmptyTextView(item: List<TrackInfo>, textView: TextView){
        textView.isVisible = item.isEmpty()
    }

    private fun updateProgressBar(isLoading:Boolean, progressBar: ProgressBar){
        progressBar.isVisible = isLoading
    }
}

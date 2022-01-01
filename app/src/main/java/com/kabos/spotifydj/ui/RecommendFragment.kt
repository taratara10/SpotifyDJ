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
import com.kabos.spotifydj.util.Pager
import com.kabos.spotifydj.util.callback.TrackCallback
import com.kabos.spotifydj.viewModel.PlaylistViewModel
import com.kabos.spotifydj.viewModel.RecommendViewModel
import com.kabos.spotifydj.viewModel.RootViewModel
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class RecommendFragment: Fragment() {
    private lateinit var binding: FragmentRecommendBinding
    private val recommendViewModel: RecommendViewModel by activityViewModels()
    private val rootViewModel: RootViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by activityViewModels()
    private val currentTrackAdapter by lazy { TrackAdapter(callback) }
    private val upperTrackAdapter by lazy { TrackAdapter(callback) }
    private val downerTrackAdapter  by lazy { TrackAdapter(callback) }
    private val callback = object : TrackCallback {
        override fun addTrack(trackInfo: TrackInfo) {
            playlistViewModel.addTrackToEditingPlaylist(trackInfo)
            rootViewModel.setPagerPosition(Pager.Playlist)
        }

        override fun playback(trackInfo: TrackInfo) {
            userViewModel.playbackTrack(trackInfo)
        }

        override fun onClick(trackInfo: TrackInfo) {
            recommendViewModel.updateCurrentTrack(trackInfo)
        }

    }
    private var alreadySetEditPlaylistFragment = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecommendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initRecyclerView()

    }

    private fun initViewModels() {
        recommendViewModel.apply {
            upperTracks.observe(viewLifecycleOwner) { tracks ->
                upperTrackAdapter.submitList(tracks)
                toggleNotApplicableResult(tracks, binding.notApplicableUpperTrackResult)
            }

            downerTracks.observe(viewLifecycleOwner){ tracks ->
                downerTrackAdapter.submitList(tracks)
                toggleNotApplicableResult(tracks, binding.notApplicableDownerTrackResult)
            }

            currentTrack.observe(viewLifecycleOwner){ track ->
                currentTrackAdapter.submitList(track)
                toggleEmptyTextView(track)
            }

            isLoadingUpperTrack.observe(viewLifecycleOwner){isLoading ->
                toggleProgressBar(isLoading, binding.upperProgress)
            }
            isLoadingDownerTrack.observe(viewLifecycleOwner){isLoading ->
                toggleProgressBar(isLoading, binding.downerProgress)
            }
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            val dividerItemDecorator = DividerItemDecoration(activity, LinearLayoutManager(activity).orientation)
            upperTrackList.apply {
                adapter = upperTrackAdapter
                addItemDecoration(dividerItemDecorator)
            }

            downerTrackList.apply {
                adapter = downerTrackAdapter
                addItemDecoration(dividerItemDecorator)
            }
            currentTrackList.adapter = currentTrackAdapter
        }
    }

    // toggle View
    private fun toggleEmptyTextView(currentTrack: List<TrackInfo>) {
        binding.apply {
            currentTrackEmpty.isVisible = currentTrack.isEmpty()
            upperTrackEmpty.isVisible = currentTrack.isEmpty()
            downerTrackEmpty.isVisible = currentTrack.isEmpty()
        }
    }
    private fun toggleNotApplicableResult(tracks: List<TrackInfo>, textView: TextView) {
        val isSelectedCurrentTrack: Boolean = !binding.currentTrackEmpty.isVisible
        textView.isVisible = tracks.isEmpty() && isSelectedCurrentTrack
    }

    private fun toggleProgressBar(isLoading:Boolean, progressBar: ProgressBar){
        progressBar.isVisible = isLoading
    }
}

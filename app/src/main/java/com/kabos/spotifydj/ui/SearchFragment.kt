package com.kabos.spotifydj.ui

import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.FragmentSearchBinding
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.ui.adapter.TrackAdapter
import com.kabos.spotifydj.util.Pager
import com.kabos.spotifydj.util.callback.TrackCallback
import com.kabos.spotifydj.viewModel.*
import dagger.hilt.android.AndroidEntryPoint

class SearchFragment: Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val rootViewModel: RootViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by activityViewModels()
    private val recommendViewModel: RecommendViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by activityViewModels()
    private val trackAdapter by lazy { TrackAdapter(callback) }
    private val callback: TrackCallback = object : TrackCallback {
        override fun addTrack(trackInfo: TrackInfo) {
            playlistViewModel.addTrackToEditingPlaylist(trackInfo)
            if (playlistViewModel.shouldReplaceEditPlaylistFragment()) {
                rootViewModel.setEditPlaylistFragment()
            }
        }

        override fun playback(trackInfo: TrackInfo) {
            userViewModel.playbackTrack(trackInfo)
        }

        override fun onClick(trackInfo: TrackInfo) {
            recommendViewModel.updateCurrentTrack(trackInfo)
            rootViewModel.setPagerPosition(Pager.Recommend)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        binding.apply {
            searchEditText.doAfterTextChanged { text ->
                searchViewModel.searchTracks(text.toString())
            }

            searchResultList.apply {
                adapter = trackAdapter
                addItemDecoration(DividerItemDecoration(activity,LinearLayoutManager(activity).orientation))
            }

            loadPlaylistButton.setOnClickListener {
                playlistViewModel.getUsersPlaylists()
                findNavController().navigate(R.id.action_nav_main_to_nav_select_playlist)
            }
        }
    }

    private fun initViewModels() {
        searchViewModel.apply {
            searchTracks.observe(viewLifecycleOwner) { result ->
                binding.notApplicableResult.isVisible = result.isEmpty()
                trackAdapter.submitList(result)
            }

            isLoadingSearchTrack.observe(viewLifecycleOwner) { isLoading ->
                binding.searchProgress.isVisible = isLoading
            }
        }
    }

}

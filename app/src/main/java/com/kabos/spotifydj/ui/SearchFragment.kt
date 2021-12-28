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
import com.kabos.spotifydj.viewModel.RecommendViewModel
import com.kabos.spotifydj.viewModel.RootViewModel
import com.kabos.spotifydj.viewModel.SearchViewModel
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment: Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val rootViewModel: RootViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by activityViewModels()
    private val recommendViewModel: RecommendViewModel by activityViewModels()
    private val trackAdapter by lazy { TrackAdapter(callback) }
    private val callback: TrackCallback = object : TrackCallback {
        override fun addTrack(trackInfo: TrackInfo) {
            userViewModel.addTrackToEditingPlaylist(trackInfo)
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
            etSearchTracks.doAfterTextChanged { text ->
                searchTrack(text.toString())
            }

            rvSearchTracksResult.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = trackAdapter
                addItemDecoration(DividerItemDecoration(activity,LinearLayoutManager(activity).orientation))
            }

            btnLoadPlaylist.setOnClickListener {
                userViewModel.getAllPlaylists()
                val action = MainFragmentDirections.actionNavMainToNavUserPlaylist(fromSearch = true)
                findNavController().navigate(action)
            }
        }
    }

    private fun initViewModels() {
        searchViewModel.apply {
            searchTracks.observe(viewLifecycleOwner) { searchResult ->
                trackAdapter.submitList(searchResult)

                //todo このクソキモイ処理を抹消する
                //「検索結果該当なし」の表示・非表示する処理
                // tvLetSearch(キーワードで検索しよう)が非表示の時のみ、「該当なし」を表示する
                binding.apply {
                    if (searchResult.isNullOrEmpty() && tvLetSearchTrack.visibility == View.GONE){
                        tvSearchItemNothing.visibility = View.VISIBLE
                    }else{
                        tvSearchItemNothing.visibility = View.GONE
                        tvLetSearchTrack.visibility = View.GONE
                    }
                }
            }

            isLoadingSearchTrack.observe(viewLifecycleOwner) { isLoading ->
                binding.pbSearchProgress.isVisible = isLoading
            }
        }
    }

    private fun searchTrack(keyword: String) {
        if (keyword.isEmpty()) searchViewModel.clearSearchTracks()
        else searchViewModel.searchTracks(keyword)
        binding.tvLetSearchTrack.isVisible = keyword.isEmpty()
    }

}

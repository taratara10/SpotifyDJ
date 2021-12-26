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
import com.kabos.spotifydj.ui.adapter.TrackAdapter
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment: Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val trackAdapter by lazy { TrackAdapter(viewModel.callback) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        binding.apply {
            etSearchTracks.doAfterTextChanged { text ->
                viewModel.updateSearchedTracksResult(text.toString())
                //「キーワードで検索しよう」を表示・非表示する処理
                if (text.isNullOrEmpty()) {
                    viewModel.clearSearchTracks()
                    tvLetSearchTrack.visibility = View.VISIBLE
                }else{
                    tvLetSearchTrack.visibility = View.GONE
                }
            }

            rvSearchTracksResult.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = trackAdapter
                addItemDecoration(DividerItemDecoration(activity,LinearLayoutManager(activity).orientation))
            }

            btnLoadPlaylist.setOnClickListener {
                viewModel.getAllPlaylists()
                val action = MainFragmentDirections.actionNavMainToNavUserPlaylist(fromSearch = true)
                findNavController().navigate(action)
            }
        }
    }

    private fun initViewModels() {
        viewModel.apply {
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


}

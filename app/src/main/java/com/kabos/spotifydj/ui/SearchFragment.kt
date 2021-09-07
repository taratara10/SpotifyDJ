package com.kabos.spotifydj.ui

import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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

        binding.apply {

            etSearchTracks.doAfterTextChanged { text ->
                viewModel.updateSearchedTracksResult(text.toString())
                //「キーワードで検索しよう」を表示・非表示する処理
                if (text.isNullOrEmpty()) {
                    viewModel.searchTrackList.postValue(listOf())
                    tvLetSearchTrack.visibility = View.VISIBLE
                }else{
                    tvLetSearchTrack.visibility = View.GONE
                }
            }

            rvSearchTracksResult.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = trackAdapter
            }

            btnLoadPlaylist.setOnClickListener {
                viewModel.getAllPlaylists()
                val action = MainFragmentDirections.actionNavMainToNavUserPlaylist(fromSearch = true)
                findNavController().navigate(action)
            }


            viewModel.searchTrackList.observe(viewLifecycleOwner, { searchResult ->
                trackAdapter.submitList(searchResult)

                //「検索結果該当なし」の表示・非表示する処理
                // tvLetSearch(キーワードで検索しよう)が非表示の時のみ、「該当なし」を表示する
                if (searchResult.isNullOrEmpty() && tvLetSearchTrack.visibility == View.GONE){
                    tvSearchItemNothing.visibility = View.VISIBLE
                }else{
                    tvSearchItemNothing.visibility = View.GONE
                }
            })

            viewModel.isLoadingSearchTrack.observe(viewLifecycleOwner,{isLoading ->
                if (isLoading) pbSearchProgress.visibility = View.VISIBLE
                else pbSearchProgress.visibility = View.GONE
            })


        }



    }


}

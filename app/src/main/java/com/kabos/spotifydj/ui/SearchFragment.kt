package com.kabos.spotifydj.ui

import android.os.Bundle
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

                //empty viewを表示・非表示する処理
                if (text.isNullOrEmpty()) {
                    tvEditTextIsEmpty.visibility = View.VISIBLE
                    rvSearchTracksResult.visibility = View.GONE
                } else {
                    tvEditTextIsEmpty.visibility = View.GONE
                    rvSearchTracksResult.visibility = View.VISIBLE
                }
            }

            rvSearchTracksResult.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = trackAdapter
            }
        }

        viewModel.searchTrackList.observe(viewLifecycleOwner, { searchResult ->
            trackAdapter.submitList(searchResult)
            if (searchResult.isNullOrEmpty()){
                binding.tvSearchItemNothing.visibility = View.VISIBLE
            }else{
                binding.tvSearchItemNothing.visibility = View.GONE
            }
        })

        viewModel.isLoadingSearchTrack.observe(viewLifecycleOwner,{isLoading ->
            if (isLoading)binding.pbSearchProgress.visibility = View.VISIBLE
                else binding.pbSearchProgress.visibility = View.GONE
        })



    }
}

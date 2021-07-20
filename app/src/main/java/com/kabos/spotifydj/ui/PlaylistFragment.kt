package com.kabos.spotifydj.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kabos.spotifydj.databinding.FragmentPlaylistBinding
import com.kabos.spotifydj.ui.adapter.TrackAdapter
import com.kabos.spotifydj.viewModel.UserViewModel

class PlaylistFragment: Fragment() {

    private lateinit var binding: FragmentPlaylistBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val trackAdapter by lazy { TrackAdapter(viewModel.callback) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            rvPlaylist.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = trackAdapter
            }


            //todo 消す
            btnSavePlaylist.setOnClickListener {
                //todo
//                val title = etPlaylistTitle.text.toString()
                viewModel.createPlaylist("test")
            }

            btnAddPlaylist.setOnClickListener {
                viewModel.postItemToPlaylist()
            }

            viewModel.currentPlaylist.observe(viewLifecycleOwner,{playlist ->
                trackAdapter.submitList(playlist)
            })
        }


    }

}

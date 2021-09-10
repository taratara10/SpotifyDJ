package com.kabos.spotifydj.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.kabos.spotifydj.databinding.FragmentPlaylistBinding
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.ui.adapter.DragTrackAdapter
import com.kabos.spotifydj.ui.adapter.PlaylistAdapter
import com.kabos.spotifydj.ui.adapter.PlaylistCallback
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class PlaylistFragment: Fragment() {

    private lateinit var binding: FragmentPlaylistBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val playlistAdapter by lazy { PlaylistAdapter(playlistCallback) }
    private val playlistCallback = object : PlaylistCallback {
        override fun onClick(playlistItem: PlaylistItem) {
        //todo isNavigete
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)

        viewModel.getAllPlaylists()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            rvPlaylist.apply {
                layoutManager = GridLayoutManager(activity, 2, RecyclerView.VERTICAL,false)
                adapter = playlistAdapter
                }
        }

        viewModel.allPlaylists.observe(viewLifecycleOwner,{ playlist ->
            playlistAdapter.submitList(playlist)
            Log.d("allPlaylist","observed $playlist")
        })

    }
}

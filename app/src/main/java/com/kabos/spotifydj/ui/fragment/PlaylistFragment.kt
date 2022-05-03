package com.kabos.spotifydj.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.kabos.spotifydj.databinding.FragmentPlaylistBinding
import com.kabos.spotifydj.data.model.playlist.*
import com.kabos.spotifydj.ui.adapter.PlaylistAdapter
import com.kabos.spotifydj.ui.viewmodel.EditingPlaylistViewModel
import com.kabos.spotifydj.util.Pager
import com.kabos.spotifydj.util.callback.PlaylistCallback
import com.kabos.spotifydj.util.constant.PlaylistConstant.Companion.CREATE_NEW_PLAYLIST_ID
import com.kabos.spotifydj.ui.viewmodel.PlaylistViewModel
import com.kabos.spotifydj.ui.viewmodel.RootViewModel
import com.kabos.spotifydj.ui.viewmodel.UserViewModel
import com.kabos.spotifydj.util.InfiniteScrollListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment: Fragment() {
    private lateinit var binding: FragmentPlaylistBinding
    private val rootViewModel: RootViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by activityViewModels()
    private val editingPlaylistViewModel: EditingPlaylistViewModel by activityViewModels()
    private val playlistAdapter by lazy { PlaylistAdapter(callback) }
    private val callback = object : PlaylistCallback {
        override fun onClick(playlistItem: PlaylistItem) {
            if (playlistItem.id == CREATE_NEW_PLAYLIST_ID) {
                editingPlaylistViewModel.clearEditingPlaylist()
            } else {
                editingPlaylistViewModel.loadPlaylistIntoEditPlaylistFragment(playlistItem)
            }
            rootViewModel.setPagerPosition(Pager.EditPlaylist)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        playlistViewModel.getUsersPlaylists(userViewModel.userName)
        binding.apply {
            playlistList.adapter = playlistAdapter
            playlistList.addOnScrollListener(InfiniteScrollListener(playlistAdapter){
                playlistViewModel.getNextPlaylist(userViewModel.userName)
            })
        }
    }

    private fun addCreateNewPlaylistItemToFirst(playlist: List<PlaylistItem>): List<PlaylistItem>{
        val mutablePlaylist = playlist.toMutableList()
        val firstItem = PlaylistItem.createNewPlaylistItem()
        mutablePlaylist.add(0, firstItem)
        return mutablePlaylist.toList()
    }

    private fun initViewModels() {
        playlistViewModel.apply {
            userCreatedPlaylist.observe(viewLifecycleOwner) { playlist ->
                playlistAdapter.submitList(addCreateNewPlaylistItemToFirst(playlist))
            }
            getUsersPlaylists(userViewModel.userName)
        }
    }
}

package com.kabos.spotifydj.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kabos.spotifydj.databinding.DialogUsersPlaylistsBinding
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.ui.adapter.PlaylistAdapter
import com.kabos.spotifydj.util.callback.PlaylistCallback
import com.kabos.spotifydj.util.Pager
import com.kabos.spotifydj.viewModel.PlaylistViewModel
import com.kabos.spotifydj.viewModel.RootViewModel
import com.kabos.spotifydj.viewModel.SearchViewModel
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogUsersPlaylists: DialogFragment() {
    private lateinit var binding: DialogUsersPlaylistsBinding
    private val rootViewModel: RootViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by activityViewModels()
    private val mainFragmentArgs: RootFragmentArgs by navArgs()
    private val playlistAdapter by lazy { PlaylistAdapter(playlistCallback) }
    private val playlistCallback = object : PlaylistCallback {
        override fun onClick(playlistItem: PlaylistItem) {
            if (mainFragmentArgs.fromSearch){
                searchViewModel.loadPlaylistIntoSearchFragment(playlistItem.id)
                rootViewModel.setPagerPosition(Pager.Search)
            }
            if (mainFragmentArgs.fromPlaylist){
                playlistViewModel.loadPlaylistIntoPlaylistFragment(playlistItem)
                rootViewModel.setPagerPosition(Pager.Playlist)
            }
            findNavController().popBackStack()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogUsersPlaylistsBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onStart() {
        super.onStart()
        initViewModels()
        binding.apply {
            rvUsersPlaylist.adapter = playlistAdapter
        }

    }

    private fun initViewModels() {
        playlistViewModel.apply {
            usersPlaylist.observe(this@DialogUsersPlaylists) { playlist ->
                //Searchで読み込む場合は全件表示
                if (mainFragmentArgs.fromSearch){
                    playlistAdapter.submitList(playlist)
                }
            }

            userCreatedPlaylist.observe(this@DialogUsersPlaylists) { playlist ->
                //Playlistで読み込む場合は、編集可能な自身のプレイリストのみを表示
                if (mainFragmentArgs.fromPlaylist){
                    playlistAdapter.submitList(playlist)
                }
            }
        }
    }

}

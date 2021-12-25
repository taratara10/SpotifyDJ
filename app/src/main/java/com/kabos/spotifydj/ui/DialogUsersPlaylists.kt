package com.kabos.spotifydj.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kabos.spotifydj.databinding.DialogUsersPlaylistsBinding
import com.kabos.spotifydj.model.playlist.Playlist
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.ui.adapter.PlaylistAdapter
import com.kabos.spotifydj.ui.adapter.PlaylistCallback
import com.kabos.spotifydj.ui.adapter.TrackAdapter
import com.kabos.spotifydj.util.Pager
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogUsersPlaylists: DialogFragment() {

    private lateinit var binding: DialogUsersPlaylistsBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val mainFragmentArgs: MainFragmentArgs by navArgs()

    private val playlistAdapter by lazy { PlaylistAdapter(playlistCallback) }
    private val playlistCallback = object : PlaylistCallback {
        override fun onClick(playlistItem: PlaylistItem) {
            //check where fragment
            if (mainFragmentArgs.fromSearch){
                viewModel.loadPlaylistIntoSearchFragment(playlistItem)
                viewModel.navigateRootFragmentPagerPosition(Pager.Search)
            }
            if (mainFragmentArgs.fromPlaylist){
                viewModel.loadPlaylistIntoPlaylistFragment(playlistItem)
                viewModel.navigateRootFragmentPagerPosition(Pager.Playlist)
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
        binding.apply {
            rvUsersPlaylist.apply {
                layoutManager = GridLayoutManager(activity, 2, RecyclerView.VERTICAL,false)
                adapter = playlistAdapter
            }
        }

        viewModel.allPlaylists.observe(this,{ usersPlaylist ->
            //Searchで読み込む場合は全件表示
            if (mainFragmentArgs.fromSearch){
                playlistAdapter.submitList(usersPlaylist)
            }
        })

        viewModel.filterOwnPlaylist.observe(this,{ ownPlaylist ->
            //Playlistで読み込む場合は、編集可能な自身のプレイリストのみを表示
            if (mainFragmentArgs.fromPlaylist){
                playlistAdapter.submitList(ownPlaylist)
            }

        })
    }

}

package com.kabos.spotifydj.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kabos.spotifydj.databinding.DialogFragmentSelectPlaylistBinding
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.ui.adapter.PlaylistAdapter
import com.kabos.spotifydj.util.callback.PlaylistCallback
import com.kabos.spotifydj.util.Pager
import com.kabos.spotifydj.ui.viewModel.PlaylistViewModel
import com.kabos.spotifydj.ui.viewModel.RootViewModel
import com.kabos.spotifydj.ui.viewModel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectPlaylistDialogFragment: DialogFragment() {
    private lateinit var binding: DialogFragmentSelectPlaylistBinding
    private val rootViewModel: RootViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by activityViewModels()
    private val playlistAdapter by lazy { PlaylistAdapter(playlistCallback) }
    private val playlistCallback = object : PlaylistCallback {
        override fun onClick(playlistItem: PlaylistItem) {
            searchViewModel.loadPlaylistIntoSearchFragment(playlistItem.id)
            rootViewModel.setPagerPosition(Pager.Search)
            findNavController().popBackStack()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentSelectPlaylistBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onStart() {
        super.onStart()
        initViewModels()
        binding.apply {
            usersPlaylistList.adapter = playlistAdapter
        }

    }

    private fun initViewModels() {
        playlistViewModel.allPlaylist.observe(this) { playlist ->
            playlistAdapter.submitList(playlist)
        }
    }
}

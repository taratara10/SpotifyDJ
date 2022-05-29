package com.kabos.spotifydj.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kabos.spotifydj.databinding.DialogFragmentSelectPlaylistBinding
import com.kabos.spotifydj.data.model.playlist.PlaylistItem
import com.kabos.spotifydj.ui.adapter.PlaylistAdapter
import com.kabos.spotifydj.util.callback.PlaylistCallback
import com.kabos.spotifydj.util.Pager
import com.kabos.spotifydj.ui.viewmodel.PlaylistViewModel
import com.kabos.spotifydj.ui.viewmodel.RootViewModel
import com.kabos.spotifydj.ui.viewmodel.SearchViewModel
import com.kabos.spotifydj.ui.viewmodel.UserViewModel
import com.kabos.spotifydj.util.InfiniteScrollListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SelectPlaylistDialogFragment: DialogFragment() {
    private lateinit var binding: DialogFragmentSelectPlaylistBinding
    private val rootViewModel: RootViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
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
            usersPlaylistList.addOnScrollListener(InfiniteScrollListener(playlistAdapter){
                playlistViewModel.getNextPlaylist(userViewModel.userName)
            })
        }
    }

    private fun initViewModels() {
        with(playlistViewModel) {
            allPlaylist.observe(this@SelectPlaylistDialogFragment) { playlist ->
                playlistAdapter.submitList(playlist)
            }

            isLoadingPlaylist.observe(this@SelectPlaylistDialogFragment) {isLoading ->
                binding.progress.isVisible = isLoading
            }
        }
    }

}

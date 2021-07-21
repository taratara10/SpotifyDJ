package com.kabos.spotifydj.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kabos.spotifydj.databinding.ActivityMainBinding.inflate
import com.kabos.spotifydj.databinding.DialogUsersPlaylistsBinding
import com.kabos.spotifydj.ui.adapter.TrackAdapter
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogUsersPlaylists: DialogFragment() {

    private lateinit var binding: DialogUsersPlaylistsBinding
    private val viewModel: UserViewModel by activityViewModels()

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
                layoutManager = LinearLayoutManager(activity)
//                adapter =
            }

            viewModel.usersPlaylistsList.observe(viewLifecycleOwner, {playlist ->

            })
        }
    }

}

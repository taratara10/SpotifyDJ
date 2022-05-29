package com.kabos.spotifydj.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.DialogFragmentConfirmCreatePlaylistBinding
import com.kabos.spotifydj.ui.viewmodel.EditingPlaylistViewModel
import com.kabos.spotifydj.ui.viewmodel.PlaylistViewModel
import com.kabos.spotifydj.ui.viewmodel.UserViewModel
import com.kabos.spotifydj.util.setErrorMessageByBoolean
import com.kabos.spotifydj.util.setInvalidAppearance
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmCreatePlaylistDialogFragment : DialogFragment() {
    private lateinit var binding: DialogFragmentConfirmCreatePlaylistBinding
    private val userViewModel: UserViewModel by activityViewModels()
    private val editingPlaylistViewModel: EditingPlaylistViewModel by activityViewModels()
    private var playlistTrackUris: List<String> = listOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentConfirmCreatePlaylistBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onStart() {
        super.onStart()
        initViewModel()
        binding.apply {
            titleEdit.doAfterTextChanged { text ->
                verifyPlaylistTitle(text.toString())
            }

            cancelButton.setOnClickListener {
                findNavController().popBackStack()
            }

            saveButton.setOnClickListener {
                editingPlaylistViewModel.createPlaylist(
                    userViewModel.userId,
                    titleEdit.text.toString(),
                    playlistTrackUris
                )
                findNavController().popBackStack()
            }
        }
    }

    private fun initViewModel() {
        editingPlaylistViewModel.apply {
            editingPlaylistTitle.observe(this@ConfirmCreatePlaylistDialogFragment) { title ->
                binding.titleEdit.setText(title)
                verifyPlaylistTitle(title)
            }

            editingPlaylist.observe(this@ConfirmCreatePlaylistDialogFragment) { tracks ->
                playlistTrackUris = tracks.map { it.contextUri }
            }
        }
    }

    private fun verifyPlaylistTitle(title: String) {
        binding.apply {
            titleLayout.setErrorMessageByBoolean(
                title.isNotEmpty(),
                getString(R.string.playlist_title_error_message)
            )
            saveButton.setInvalidAppearance(title.isEmpty())
        }
    }
}


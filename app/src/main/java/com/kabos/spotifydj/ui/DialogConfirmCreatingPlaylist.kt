package com.kabos.spotifydj.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.DialogConfirmCreatingPlaylistBinding
import com.kabos.spotifydj.util.ReplaceFragment
import com.kabos.spotifydj.viewModel.PlaylistViewModel
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogConfirmCreatingPlaylist: DialogFragment() {
    private lateinit var binding: DialogConfirmCreatingPlaylistBinding
    private val userViewModel: UserViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by activityViewModels()
    private var playlistTitle = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogConfirmCreatingPlaylistBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setTitle("プレイリスト名")
            .create()
    }

    override fun onStart() {
        super.onStart()
        binding.apply {

            // todo playlistTitleに置換したけど問題ない？
//            etDialogCreatePlaylistTitle.setText(viewModel.localPlaylistTitle)
            etDialogCreatePlaylistTitle.doAfterTextChanged { text ->
                //emptyならErrorを表示する & save buttonをenableにする
                if (text.isNullOrEmpty()) {
                    tilDialogCreatePlaylistTitle.error = "タイトルを入力してください"
                    btnDialogSave.isEnabled = false
                } else {
                    tilDialogCreatePlaylistTitle.error = null
                    btnDialogSave.isEnabled = true
                    playlistTitle = text.toString()
                }
            }

            btnDialogCancel.setOnClickListener { dialog?.cancel() }
            btnDialogSave  .setOnClickListener {
                if (playlistTitle.isNotEmpty()) {
                    playlistViewModel.createPlaylist(playlistTitle)
                    // todo 抹消する
                    userViewModel.isNavigateExistingPlaylistFragment.value = true
                    dialog?.cancel()
                    Toast.makeText(context,"プレイリストを作成しました", Toast.LENGTH_LONG).show()
                }
            }

        }
    }
}


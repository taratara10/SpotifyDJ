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
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.DialogConfirmCreatingPlaylistBinding
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogConfirmCreatingPlaylist: DialogFragment() {
    private lateinit var binding: DialogConfirmCreatingPlaylistBinding
    private val viewModel: UserViewModel by activityViewModels()


    private val negativeButtonListener = DialogInterface.OnClickListener{_,_ -> dialog?.cancel() }
    private val positiveButtonListener =DialogInterface.OnClickListener { _, _ ->
        //todo implement navigate and replace fragment
        //todo varidateはviewmodelの責務にしたい　handlingどーするか
        if (viewModel.localPlaylistTitle.isNotEmpty()) {
            viewModel.createPlaylist()
            //replace existingPlaylist
            Toast.makeText(context,"プレイリストを作成しました", Toast.LENGTH_LONG).show()
        }
    }

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

            etDialogCreatePlaylistTitle.apply {
                setText(viewModel.localPlaylistTitle)
                doAfterTextChanged { text ->
                    viewModel.localPlaylistTitle = text.toString()
                    //emptyならErrorを表示する
                    if (text.isNullOrEmpty()) {
                        tilDialogCreatePlaylistTitle.error = "タイトルを入力してください"
                        //todo save button make enable
                    } else {
                        tilDialogCreatePlaylistTitle.error = null
                    }
                }
            }



        }
    }
}


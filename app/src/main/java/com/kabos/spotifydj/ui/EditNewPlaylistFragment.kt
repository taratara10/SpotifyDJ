package com.kabos.spotifydj.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.FragmentEditNewPlaylistBinding
import com.kabos.spotifydj.databinding.FragmentPlaylistBinding
import com.kabos.spotifydj.ui.adapter.DragTrackAdapter
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

class EditNewPlaylistFragment: Fragment() {
    private lateinit var binding: FragmentEditNewPlaylistBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val dragTackAdapter by lazy { DragTrackAdapter(viewModel.dragTrackCallback,emptyList()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEditNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        binding.apply {
            rvNewPlaylist.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = dragTackAdapter
                dragListener = dragTackAdapter.onItemDragListener
                swipeListener = dragTackAdapter.onItemSwipeListener
                disableSwipeDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)
            }

            val date = Calendar.getInstance().time
            val dataFormat = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault())
            etNewPlaylistTitle.setText("NewPlaylist_${dataFormat.format(date)}")

            btnSavePlaylist.setOnClickListener {
                viewModel.updatePlaylistTitle(etNewPlaylistTitle.text.toString())
                findNavController().navigate(R.id.action_nav_main_to_nav_confirm_playlist)
            }

            //todo localPlaylistをrefreshする処理
            etNewPlaylistTitle.setOnFocusChangeListener { editText, hasFocus ->
                //focusが外れたらplaylist titleを更新
                if (!hasFocus && viewModel.editingPlaylistId.isNotEmpty()){
                    editText as EditText
                    viewModel.updatePlaylistTitle(editText.text.toString())
                }

            }



        }
    }

    private fun initViewModels() {
        viewModel.apply {
            editingPlaylist.observe(viewLifecycleOwner) { playlist ->
                dragTackAdapter.submitList(playlist)
                binding.tvPlaylistEmpty.isVisible = playlist.isNullOrEmpty()
                binding.btnSavePlaylist.isEnabled = playlist.isNotEmpty()
            }

            editingPlaylistTitle.observe(viewLifecycleOwner) { title ->
                binding.etNewPlaylistTitle.setText(title)
            }

        }
    }
}

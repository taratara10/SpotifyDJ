package com.kabos.spotifydj.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.kabos.spotifydj.databinding.FragmentEditExistingPlaylistBinding
import com.kabos.spotifydj.databinding.FragmentPlaylistBinding
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.ui.adapter.DragTrackAdapter
import com.kabos.spotifydj.util.callback.DragTrackCallback
import com.kabos.spotifydj.viewModel.PlaylistViewModel
import com.kabos.spotifydj.viewModel.RecommendViewModel
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditExistingPlaylistFragment: Fragment() {
    private lateinit var binding: FragmentEditExistingPlaylistBinding
    private val recommendViewModel: RecommendViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by activityViewModels()
    private val dragTackAdapter by lazy { DragTrackAdapter(callback ,emptyList()) }
    private val callback = object : DragTrackCallback {
        override fun onClick(trackInfo: TrackInfo) {
            recommendViewModel.updateCurrentTrack(trackInfo)
        }

        override fun playback(trackInfo: TrackInfo) {
//            playbackTrack(trackInfo)
        }

        override fun onSwiped(position: Int) {
            playlistViewModel.removeTrackFromLocalPlaylist(position)
        }

        override fun onDropped(initial: Int, final: Int) {
            playlistViewModel.reorderPlaylistsTracks(initial,final)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        binding.apply {
            rvExistingPlaylist.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = dragTackAdapter
                dragListener = dragTackAdapter.onItemDragListener
                swipeListener = dragTackAdapter.onItemSwipeListener
                disableSwipeDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)
            }

            etExistingPlaylistTitle.apply {
                doAfterTextChanged { text ->
                    //emptyならErrorを表示する & save buttonをenableにする
                    if (text.isNullOrEmpty()) {
                        tilExistingPlaylistTitle.error = "タイトルを入力してください"
                    } else {
                        tilExistingPlaylistTitle.error = null
                    }
                }

                setOnFocusChangeListener { editText, hasFocus ->
                    //focusが外れたらplaylist titleを更新
                    if (!hasFocus){
                        editText as EditText
                        playlistViewModel.updatePlaylistTitle(editText.text.toString())
                    }
                }
            }

        }
    }

    private fun initViewModels() {
        playlistViewModel.apply {
            editingPlaylist.observe(viewLifecycleOwner) { playlist ->
                dragTackAdapter.submitList(playlist)
                binding.tvPlaylistEmpty.isVisible = playlist.isNullOrEmpty()
            }

            editingPlaylistTitle.observe(viewLifecycleOwner) { title ->
                binding.etExistingPlaylistTitle.setText(title)
            }

            isLoadingPlaylistTrack.observe(viewLifecycleOwner) { isLoading ->
                binding.pbPlaylistProgress.isVisible = isLoading
            }
        }
    }
}

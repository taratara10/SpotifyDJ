package com.kabos.spotifydj.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.FragmentEditPlaylistBinding
import com.kabos.spotifydj.data.model.TrackInfo
import com.kabos.spotifydj.ui.adapter.DragTrackAdapter
import com.kabos.spotifydj.ui.viewmodel.*
import com.kabos.spotifydj.util.Pager
import com.kabos.spotifydj.util.callback.DragTrackCallback

class EditPlaylistFragment: Fragment() {
    private lateinit var binding: FragmentEditPlaylistBinding
    private val rootViewModel: RootViewModel by activityViewModels()
    private val recommendViewModel: RecommendViewModel by activityViewModels()
    private val editingPlaylistViewModel: EditingPlaylistViewModel by activityViewModels()
    private val dragTackAdapter by lazy { DragTrackAdapter(callback ,emptyList()) }
    private val callback = object : DragTrackCallback {
        override fun onClick(trackInfo: TrackInfo) {
            recommendViewModel.updateCurrentTrack(trackInfo)
            rootViewModel.setPagerPosition(Pager.Recommend)
        }

        override fun playback(trackInfo: TrackInfo) {
//            playbackTrack(trackInfo)
        }

        override fun onSwiped(position: Int) {
            editingPlaylistViewModel.removeTrackFromLocalPlaylist(position)
        }

        override fun onDropped(initial: Int, final: Int) {
            editingPlaylistViewModel.reorderPlaylistsTracks(initial,final)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEditPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        binding.apply {
            playlistList.apply {
                adapter = dragTackAdapter
                dragListener = dragTackAdapter.onItemDragListener
                swipeListener = dragTackAdapter.onItemSwipeListener
                disableSwipeDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)
            }

            saveButton.setOnClickListener {
                editingPlaylistViewModel.updateEditingPlaylistTitle(titleEdit.text.toString())
                findNavController().navigate(R.id.action_nav_main_to_nav_confirm_create_playlist)
            }

            back.setOnClickListener {
                editingPlaylistViewModel.clearEditingPlaylist()
                rootViewModel.setPagerPosition(Pager.Playlist)
            }

            // todo editTextの編集ではなく、dialogで表示する

        }
    }

    private fun initViewModels() {
        editingPlaylistViewModel.apply {
            editingPlaylist.observe(viewLifecycleOwner) { playlist ->
                dragTackAdapter.submitList(playlist)
                binding.emptyText.isVisible = playlist.isNullOrEmpty()
                binding.saveButton.isEnabled = playlist.isNotEmpty()
            }

            editingPlaylistTitle.observe(viewLifecycleOwner) { title ->
                binding.titleEdit.setText(title)
            }

            isPlaylistUnSaved.observe(viewLifecycleOwner) { unSaved ->
                toggleNewPlaylistDescription(unSaved)
            }
        }
    }

    private fun toggleNewPlaylistDescription(isPlaylistUnSaved: Boolean) {
        binding.apply {
            unsavedDescription.isVisible = isPlaylistUnSaved
            saveButton.isVisible = isPlaylistUnSaved
        }
    }
}

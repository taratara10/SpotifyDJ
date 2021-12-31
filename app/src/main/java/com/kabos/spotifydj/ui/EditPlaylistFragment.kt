package com.kabos.spotifydj.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.FragmentEditPlaylistBinding
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.ui.adapter.DragTrackAdapter
import com.kabos.spotifydj.util.callback.DragTrackCallback
import com.kabos.spotifydj.viewModel.PlaylistViewModel
import com.kabos.spotifydj.viewModel.RecommendViewModel
import java.text.SimpleDateFormat
import java.util.*

class EditPlaylistFragment: Fragment() {
    private lateinit var binding: FragmentEditPlaylistBinding
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

            val date = Calendar.getInstance().time
            val dataFormat = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault())
            titleEdit.setText("NewPlaylist_${dataFormat.format(date)}")

            saveButton.setOnClickListener {
                hideNewPlaylistLayout()
            }

            // todo editTextの編集ではなく、dialogで表示する

        }
    }

    private fun initViewModels() {
        playlistViewModel.apply {
            editingPlaylist.observe(viewLifecycleOwner) { playlist ->
                dragTackAdapter.submitList(playlist)
                binding.emptyText.isVisible = playlist.isNullOrEmpty()
                binding.saveButton.isEnabled = playlist.isNotEmpty()
            }

            editingPlaylistTitle.observe(viewLifecycleOwner) { title ->
                binding.titleEdit.setText(title)
            }
        }
    }

    private fun hideNewPlaylistLayout() {
        binding.apply {
            unsavedDescription.isVisible = false
            saveButton.isVisible = false
        }
    }
}
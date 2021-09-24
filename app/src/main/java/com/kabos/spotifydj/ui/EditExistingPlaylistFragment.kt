package com.kabos.spotifydj.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.kabos.spotifydj.databinding.FragmentEditExistingPlaylistBinding
import com.kabos.spotifydj.databinding.FragmentPlaylistBinding
import com.kabos.spotifydj.ui.adapter.DragTrackAdapter
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditExistingPlaylistFragment: Fragment() {

    private lateinit var binding: FragmentEditExistingPlaylistBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val dragTackAdapter by lazy { DragTrackAdapter(viewModel.dragTrackCallback,emptyList()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEditExistingPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            rvExistingPlaylist.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = dragTackAdapter
                dragListener = dragTackAdapter.onItemDragListener
                swipeListener = dragTackAdapter.onItemSwipeListener
                disableSwipeDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)
            }

            etExistingPlaylistTitle.setOnFocusChangeListener { editText, hasFocus ->
                //focusが外れたらplaylist titleを更新
                if (!hasFocus){
                    editText as EditText
                    viewModel.updatePlaylistTitle(editText.text.toString())
                }
            }

            viewModel.localPlaylist.observe(viewLifecycleOwner,{playlist ->
                playlist?.let { dragTackAdapter.submitList(it) }
                if(playlist.isNullOrEmpty()) {
                    tvPlaylistEmpty.visibility = View.VISIBLE
                }else {
                    tvPlaylistEmpty.visibility = View.GONE
                }
            })

            viewModel.loadedPlaylistTitle.observe(viewLifecycleOwner,{title ->
                etExistingPlaylistTitle.setText(title)
            })

            viewModel.isLoadingPlaylistTrack.observe(viewLifecycleOwner,{isLoading ->
                if (isLoading) pbPlaylistProgress.visibility = View.VISIBLE
                else pbPlaylistProgress.visibility = View.GONE
            })

        }
    }
}

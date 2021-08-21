package com.kabos.spotifydj.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.kabos.spotifydj.databinding.FragmentPlaylistBinding
import com.kabos.spotifydj.ui.adapter.DragTrackAdapter
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment: Fragment() {

    private lateinit var binding: FragmentPlaylistBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val dragTackAdapter by lazy { DragTrackAdapter(viewModel.dragTrackCallback,emptyList()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            rvPlaylist.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = dragTackAdapter
                dragListener = dragTackAdapter.onItemDragListener
                swipeListener = dragTackAdapter.onItemSwipeListener
                disableSwipeDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)
            }


            //todo 消す
            btnSavePlaylist.setOnClickListener {
                //todo
//                val title = etPlaylistTitle.text.toString()
//                viewModel.createPlaylist("test")
            }

            btnAddPlaylist.setOnClickListener {
                viewModel.postItemToPlaylist()
            }

            viewModel.currentPlaylist.observe(viewLifecycleOwner,{playlist ->
                dragTackAdapter.submitList(playlist)
                if(playlist.isNullOrEmpty()) tvPlaylistEmpty.visibility = View.VISIBLE
                else tvPlaylistEmpty.visibility = View.GONE
            })
        }


    }

}

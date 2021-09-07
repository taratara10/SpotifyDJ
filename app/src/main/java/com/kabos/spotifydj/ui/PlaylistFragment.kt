package com.kabos.spotifydj.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.kabos.spotifydj.databinding.FragmentPlaylistBinding
import com.kabos.spotifydj.ui.adapter.DragTrackAdapter
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

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

            val date = Calendar.getInstance().time
            val dataFormat = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault())
            etPlaylistTitle.setText("NewPlaylist_${dataFormat.format(date)}")

            btnCreatePlaylist.setOnClickListener {
                viewModel.createPlaylist(etPlaylistTitle.text.toString())
                Toast.makeText(context,"プレイリストを作成しました",Toast.LENGTH_LONG).show()

                //todo btnEnableどうやって管理しよか
                it.isEnabled = false
            }


            viewModel.localPlaylist.observe(viewLifecycleOwner,{playlist ->
                playlist?.let { dragTackAdapter.submitList(it) }
                if(playlist.isNullOrEmpty()) {
                    tvPlaylistEmpty.visibility = View.VISIBLE
                    btnEditPlaylist.visibility = View.VISIBLE
                }else {
                    tvPlaylistEmpty.visibility = View.GONE
                    btnEditPlaylist.visibility = View.GONE
                }
            })

            viewModel.loadedPlaylistTitle.observe(viewLifecycleOwner,{title ->
                etPlaylistTitle.setText(title)
            })

            btnEditPlaylist.setOnClickListener{
                viewModel.getAllPlaylists()
                val action = MainFragmentDirections.actionNavMainToNavUserPlaylist(fromPlaylist = true)
                findNavController().navigate(action)
            }









        }


    }

}

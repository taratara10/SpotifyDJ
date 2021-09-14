package com.kabos.spotifydj.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kabos.spotifydj.databinding.FragmentPlaylistBinding
import com.kabos.spotifydj.model.playlist.*
import com.kabos.spotifydj.ui.adapter.PlaylistAdapter
import com.kabos.spotifydj.ui.adapter.PlaylistCallback
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class PlaylistFragment: Fragment() {

    private lateinit var binding: FragmentPlaylistBinding
    private val viewModel: UserViewModel by activityViewModels()
    private val playlistAdapter by lazy { PlaylistAdapter(playlistCallback) }
    private val playlistCallback = object : PlaylistCallback {
        override fun onClick(playlistItem: PlaylistItem) {
            if (playlistItem.id == "createNewPlaylist"){
                viewModel.isNavigateNewPlaylistFragment.postValue(true)
            }else{
                viewModel.loadPlaylistIntoPlaylistFragment(playlistItem)
                viewModel.isNavigateExistingPlaylistFragment.postValue(true)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)

        viewModel.getAllPlaylists()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            rvPlaylist.apply {
                layoutManager = GridLayoutManager(activity, 2, RecyclerView.VERTICAL,false)
                adapter = playlistAdapter
                }
        }

        viewModel.allPlaylists.observe(viewLifecycleOwner,{ playlist ->
            if (playlist == null) return@observe
            playlistAdapter.submitList(fixFirstItemByCreateNewPlaylist(playlist))

        })

    }

    private fun fixFirstItemByCreateNewPlaylist(list: List<PlaylistItem>): List<PlaylistItem>{
        val mList = list.toMutableList()
        //1つ目に表示する"新規作成"のアイテム
        val firstPlaylistItem = PlaylistItem(
            collaborative = false,
            description = "",
            external_urls = ExternalUrls(""),
            href = "",
            id = "createNewPlaylist",
            images = listOf(Image(url = "firstItem")),
            name = "新規作成",
            owner = Owner("", ExternalUrlsX(""),"","","",""),
            public = false,
            snapshot_id = "",
            type = "",
            uri = "")

        mList.add(0, firstPlaylistItem)
        return mList.toList()
    }
}

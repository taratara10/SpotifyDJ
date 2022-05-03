package com.kabos.spotifydj.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kabos.spotifydj.databinding.DialogFragmentSelectPlaylistBinding
import com.kabos.spotifydj.data.model.playlist.PlaylistItem
import com.kabos.spotifydj.ui.adapter.PlaylistAdapter
import com.kabos.spotifydj.util.callback.PlaylistCallback
import com.kabos.spotifydj.util.Pager
import com.kabos.spotifydj.ui.viewmodel.PlaylistViewModel
import com.kabos.spotifydj.ui.viewmodel.RootViewModel
import com.kabos.spotifydj.ui.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectPlaylistDialogFragment: DialogFragment() {
    private lateinit var binding: DialogFragmentSelectPlaylistBinding
    private val rootViewModel: RootViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by activityViewModels()
    private val playlistAdapter by lazy { PlaylistAdapter(playlistCallback) }
    private val playlistCallback = object : PlaylistCallback {
        override fun onClick(playlistItem: PlaylistItem) {
            searchViewModel.loadPlaylistIntoSearchFragment(playlistItem.id)
            rootViewModel.setPagerPosition(Pager.Search)
            findNavController().popBackStack()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentSelectPlaylistBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onStart() {
        super.onStart()
        initViewModels()
        binding.apply {
            usersPlaylistList.adapter = playlistAdapter
            usersPlaylistList.addOnScrollListener(InfiniteScrollListener())
        }

    }

    private fun initViewModels() {
        with(playlistViewModel) {
            allPlaylist.observe(this@SelectPlaylistDialogFragment) { playlist ->
                playlistAdapter.submitList(playlist)
            }

            isLoadingPlaylist.observe(this@SelectPlaylistDialogFragment) {isLoading ->
                binding.progress.isVisible = isLoading
            }
        }
    }

    inner class InfiniteScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            // アダプターが保持しているアイテムの合計
            val itemCount = playlistAdapter.itemCount
            // 画面に表示されているアイテム数
            val childCount = recyclerView.childCount
            val manager = recyclerView.layoutManager as LinearLayoutManager
            // 画面に表示されている一番上のアイテムの位置
            val firstPosition = manager.findFirstVisibleItemPosition()
            // 以下の条件に当てはまれば一番下までスクロールされたと判断できる。
            if (itemCount == childCount + firstPosition) {
                playlistViewModel.getNextPlaylist()
            }
        }
    }
}

package com.kabos.spotifydj.ui.viewmodel

import androidx.lifecycle.*
import com.kabos.spotifydj.R
import com.kabos.spotifydj.data.model.TrackInfo
import com.kabos.spotifydj.data.model.User
import com.kabos.spotifydj.data.model.exception.SpotifyApiException
import com.kabos.spotifydj.data.model.exception.TokenExpiredException
import com.kabos.spotifydj.data.model.playlist.Playlist
import com.kabos.spotifydj.data.model.playlist.PlaylistItem
import com.kabos.spotifydj.data.repository.PlaylistRepository
import com.kabos.spotifydj.data.repository.TrackRepository
import com.kabos.spotifydj.data.repository.UserRepository
import com.kabos.spotifydj.util.OneShotEvent
import com.kabos.spotifydj.util.addItem
import com.kabos.spotifydj.util.constant.PlaylistConstant.Companion.CREATE_NEW_PLAYLIST_ID
import com.kabos.spotifydj.util.removeAt
import com.kabos.spotifydj.util.replacePosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : BaseViewModel() {

    private var currentPlaylist: Playlist? = null
    private val _allPlaylist = MediatorLiveData<List<PlaylistItem>>()
    private val _userCreatedPlaylist = MutableLiveData<List<PlaylistItem>>()
    private val _isLoadingPlaylist = MutableLiveData(false)

    val allPlaylist: LiveData<List<PlaylistItem>> = _allPlaylist
    val userCreatedPlaylist: LiveData<List<PlaylistItem>> = _userCreatedPlaylist
    val isLoadingPlaylist: LiveData<Boolean> = _isLoadingPlaylist

    fun getUsersPlaylists(userName: String) = viewModelScope.launch {
        _isLoadingPlaylist.postValue(true)
        runCatching {
            currentPlaylist = playlistRepository.getUsersPlaylist()
            val items = currentPlaylist!!.items
            _allPlaylist.postValue(items)
            filterUserCreatedPlaylist(userName, items)
        }.onFailure { errorHandle(it) }
        _isLoadingPlaylist.postValue(false)
    }

    /**
     * 無限スクロールで次のオフセットがあれば取得する
     * _isLoadingPlaylistで連続で発火しないよう早期return
     * */
    fun getNextPlaylist(userName: String) = viewModelScope.launch {
        if (_isLoadingPlaylist.value == true) return@launch

        _isLoadingPlaylist.postValue(true)

        runCatching {
            val nextOffset = getNextPlaylistOffset(currentPlaylist)
            if (nextOffset == currentPlaylist?.total) return@runCatching

            val nextOffsetPlaylist = playlistRepository.getUsersPlaylist(nextOffset)
            currentPlaylist = nextOffsetPlaylist

            nextOffsetPlaylist.items.forEach {
                _allPlaylist.addItem(it)
            }

            _allPlaylist.value?.let {
                filterUserCreatedPlaylist(userName, it)
            }
        }.onFailure { errorHandle(it) }

        _isLoadingPlaylist.postValue(false)
    }

    private fun getNextPlaylistOffset(playlist: Playlist?): Int {
        if (playlist == null) return 0

        val itemLimit = 50
        val total = playlist.total
        val nextOffset = playlist.offset + itemLimit
        return if (nextOffset <= total) nextOffset else total
    }

    private fun filterUserCreatedPlaylist(userName: String, playlist: List<PlaylistItem>) {
        _userCreatedPlaylist.postValue(playlist.filter { it.owner.display_name == userName })
    }

}

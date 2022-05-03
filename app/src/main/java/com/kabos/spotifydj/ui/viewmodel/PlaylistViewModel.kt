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
    private val userRepository: UserRepository,
    private val playlistRepository: PlaylistRepository,
    private val trackRepository: TrackRepository
) : BaseViewModel() {
    private var mUserId = ""
    private var mUserName = ""
    private var _editingPlaylistId = ""

    private val _editingPlaylist = MediatorLiveData<List<TrackInfo>>()
    private val _editingPlaylistTitle = MutableLiveData<String>()

    private var currentPlaylist: Playlist? = null
    private val _allPlaylist = MediatorLiveData<List<PlaylistItem>>()
    private val _userCreatedPlaylist = MutableLiveData<List<PlaylistItem>>()
    private val _isLoadingPlaylist = MutableLiveData(false)
    private val _isPlaylistUnSaved = MutableLiveData(false)

    val allPlaylist: LiveData<List<PlaylistItem>>
        get() = _allPlaylist
    val userCreatedPlaylist: LiveData<List<PlaylistItem>>
        get() = _userCreatedPlaylist
    val editingPlaylist: LiveData<List<TrackInfo>>
        get() = _editingPlaylist
    val editingPlaylistTitle: LiveData<String>
        get() = _editingPlaylistTitle
    val isLoadingPlaylist: LiveData<Boolean>
        get() = _isLoadingPlaylist
    val isPlaylistUnSaved: LiveData<Boolean>
        get() = _isPlaylistUnSaved

    fun initUserAccount(id: String, name: String) {

    }

    fun getUserAccount() = viewModelScope.launch {

        runCatching {
            val user = userRepository.getUsersProfile()
            mUserId = user.id
            mUserName = user.display_name
        }.onFailure { exception ->
            if (exception is SpotifyApiException && exception is SpotifyApiException.UnAuthorized) {
                _needRefreshAccessToken.postValue(OneShotEvent(Unit))
            }
            if (exception is TokenExpiredException) _needRefreshAccessToken.postValue(OneShotEvent(Unit))
            Timber.d("errorHandle $exception")
        }
    }
    fun getUsersPlaylists() = viewModelScope.launch {
        _isLoadingPlaylist.postValue(true)
        runCatching {
            currentPlaylist = playlistRepository.getUsersPlaylist()
            val items = currentPlaylist!!.items
            _allPlaylist.postValue(items)
            filterUserCreatedPlaylist(items)
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

    /**
     * 無限スクロールで次のオフセットがあれば取得する
     * _isLoadingPlaylistで連続で発火しないよう早期return
     * */
    fun getNextPlaylist() = viewModelScope.launch {
        if (_isLoadingPlaylist.value == true) return@launch

        _isLoadingPlaylist.postValue(true)

        runCatching {
            val nextOffset = getNextPlaylistOffset(currentPlaylist)
            if (nextOffset == currentPlaylist?.total) return@runCatching

            val nextOffsetPlaylist = playlistRepository.getUsersPlaylist(nextOffset)
            currentPlaylist = nextOffsetPlaylist

            Timber.d("--ss playlist ${nextOffsetPlaylist.offset}/${nextOffsetPlaylist.total}")
            nextOffsetPlaylist.items.forEach {
                _allPlaylist.addItem(it)
            }
            _allPlaylist.value?.let {
                filterUserCreatedPlaylist(it)
            }
        }.onFailure { errorHandle(it) }

        _isLoadingPlaylist.postValue(false)
    }

    private fun filterUserCreatedPlaylist(playlist: List<PlaylistItem>) {
        _userCreatedPlaylist.postValue(playlist.filter { it.owner.display_name == mUserName })
    }

    fun createPlaylist(title: String, trackUris: List<String>) = viewModelScope.launch {
        runCatching {
            val playlistId = playlistRepository.createPlaylist(mUserId, title)
            updatePlaylistId(playlistId)
            addTracksToPlaylist(trackUris)
            _toastMessageId.postValue(R.string.result_crete_playlist_success)
        }.onFailure { errorHandle(it) }
    }

    fun addTrackToEditingPlaylist(track: TrackInfo) {
        _editingPlaylist.addItem(track)
        addTracksToPlaylist(listOf(track.contextUri))
        verifyPlaylistIsSaved()
    }

    private fun addTracksToPlaylist(trackUris: List<String>) = viewModelScope.launch {
        if (_editingPlaylistId.isEmpty() || _editingPlaylistId == CREATE_NEW_PLAYLIST_ID) return@launch
        runCatching {
            playlistRepository.addTracksToPlaylist(_editingPlaylistId, trackUris)
        }.onFailure { errorHandle(it) }
    }

    fun updateEditingPlaylistTitle(title: String) {
        _editingPlaylistTitle.postValue(title)
    }

    fun updatePlaylistTitle(title: String) = viewModelScope.launch {
        if (title.isEmpty()) return@launch
        runCatching {
            playlistRepository.updatePlaylistTitle(_editingPlaylistId, title)
            _toastMessageId.postValue(R.string.result_update_title_success)
        }.onFailure { errorHandle(it) }
    }

    fun removeTrackFromLocalPlaylist(position: Int) {
        val removeTrack = _editingPlaylist.removeAt(position)
        if (removeTrack != null) deleteTracksFromPlaylist(removeTrack.contextUri)
    }

    private fun deleteTracksFromPlaylist(trackUri: String) = viewModelScope.launch {
        if (_editingPlaylistId.isEmpty()) return@launch
        runCatching {
            playlistRepository.deleteTracksFromPlaylist(_editingPlaylistId, trackUri)
        }.onFailure { errorHandle(it) }
    }

    fun reorderPlaylistsTracks(initialPosition: Int, finalPosition: Int) = viewModelScope.launch {
        if (_editingPlaylistId.isEmpty()) return@launch
        _editingPlaylist.replacePosition(initialPosition, finalPosition)
        runCatching {
            playlistRepository.reorderPlaylistsTracks(
                _editingPlaylistId,
                initialPosition,
                finalPosition
            )
        }.onFailure { errorHandle(it) }
    }

    /**
     * Dialog Playlist
     * */

    fun loadPlaylistIntoEditPlaylistFragment(playlist: PlaylistItem) = viewModelScope.launch {
        _editingPlaylistTitle.postValue(playlist.name)
        updatePlaylistId(playlist.id)
        getTracksByPlaylistId(playlist.id)
    }

    private fun updatePlaylistId(playlistId: String) {
        _editingPlaylistId = playlistId
        verifyPlaylistIsSaved()
    }

    private fun verifyPlaylistIsSaved() {
        val isNotSaved =
            _editingPlaylistId.isEmpty() || _editingPlaylistId == CREATE_NEW_PLAYLIST_ID
        _isPlaylistUnSaved.postValue(isNotSaved)
    }

    fun clearEditingPlaylist() {
        updatePlaylistId("")
        _editingPlaylistTitle.postValue("")
        _editingPlaylist.postValue(listOf())
    }

    private fun getTracksByPlaylistId(playlistId: String) = viewModelScope.launch {
        runCatching {
            _editingPlaylist.postValue(trackRepository.getTrackInfosByPlaylistId(playlistId))
        }.onFailure { errorHandle(it) }
    }

}

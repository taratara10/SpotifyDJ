package com.kabos.spotifydj.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.R
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.model.apiResult.SpotifyApiErrorReason
import com.kabos.spotifydj.model.apiResult.SpotifyApiResource
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.repository.Repository
import com.kabos.spotifydj.util.OneShotEvent
import com.kabos.spotifydj.util.addItem
import com.kabos.spotifydj.util.constant.PlaylistConstant.Companion.CREATE_NEW_PLAYLIST_ID
import com.kabos.spotifydj.util.removeAt
import com.kabos.spotifydj.util.replacePosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(private val repository: Repository): ViewModel() {
    private var mAccessToken = ""
    private var mUserId = ""
    private var mUserName = ""
    private var _editingPlaylistId = ""

    private val _editingPlaylist = MutableLiveData<List<TrackInfo>>()
    private val _editingPlaylistTitle = MutableLiveData<String>()
    private val _allPlaylist = MutableLiveData<List<PlaylistItem>>()
    private val _userCreatedPlaylist = MutableLiveData<List<PlaylistItem>>()
    private val _isLoadingPlaylistTrack = MutableLiveData(false)
    private val _isPlaylistUnSaved = MutableLiveData(false)
    private val _needRefreshAccessToken = MutableLiveData<OneShotEvent<Boolean>>()
    private val _toastMessageId = MutableLiveData<Int>()

    val allPlaylist: LiveData<List<PlaylistItem>>
        get() = _allPlaylist
    val userCreatedPlaylist: LiveData<List<PlaylistItem>>
        get() = _userCreatedPlaylist
    val editingPlaylist: LiveData<List<TrackInfo>>
        get() = _editingPlaylist
    val editingPlaylistTitle: LiveData<String>
        get() = _editingPlaylistTitle
    val isLoadingPlaylistTrack: LiveData<Boolean>
        get() = _isLoadingPlaylistTrack
    val isPlaylistUnSaved: LiveData<Boolean>
        get() = _isPlaylistUnSaved
    val needRefreshAccessToken: LiveData<OneShotEvent<Boolean>>
        get() = _needRefreshAccessToken
    val toastMessageId: LiveData<Int>
        get() = _toastMessageId

    fun initAccessToken(token: String) {
        mAccessToken = token
    }

    fun initUserAccount(id:String, name: String) {
        mUserId = id
        mUserName = name
    }

    fun getUsersPlaylists() = viewModelScope.launch {
        when (val result = repository.getUsersPlaylist(mAccessToken)) {
            is SpotifyApiResource.Success -> {
                val playlist = result.data ?: listOf()
                _allPlaylist.postValue(playlist)
                filterUserCreatedPlaylist(playlist)
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        _toastMessageId.postValue(R.string.result_failed)
                    }
                }
            }
        }
    }
    private fun filterUserCreatedPlaylist(playlist:List<PlaylistItem>) {
        // todo idでなくていいの？
        _userCreatedPlaylist.postValue(playlist.filter { it.owner.display_name == mUserName })
    }


    fun createPlaylist(title: String, trackUris: List<String>) = viewModelScope.launch {
        when (val result = repository.createPlaylist(mAccessToken, mUserId, title)) {
            is SpotifyApiResource.Success -> {
                updatePlaylistId(result.data.toString())
                addTracksToPlaylist(trackUris)
                _toastMessageId.postValue(R.string.result_crete_playlist_success)
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        _toastMessageId.postValue(R.string.result_failed)
                    }
                }
            }
        }
    }

    fun addTrackToEditingPlaylist(track: TrackInfo){
        _editingPlaylist.addItem(track)
        addTracksToPlaylist(listOf(track.contextUri))
        verifyPlaylistIsSaved()
    }

    private fun addTracksToPlaylist(trackUris: List<String>) = viewModelScope.launch {
        if (_editingPlaylistId.isEmpty() || _editingPlaylistId == CREATE_NEW_PLAYLIST_ID) return@launch
        when (val result = repository.addTracksToPlaylist(mAccessToken, _editingPlaylistId, trackUris)) {
            is SpotifyApiResource.Success -> { }
            is SpotifyApiResource.Error -> {
                when (result.reason) {
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        _toastMessageId.postValue(R.string.result_failed)
                    }
                }
            }
        }
    }

    fun updateEditingPlaylistTitle(title: String) {
        _editingPlaylistTitle.postValue(title)
    }

    fun updatePlaylistTitle(title: String) = viewModelScope.launch {
        if (title.isEmpty()) return@launch

        when (val result = repository.updatePlaylistTitle(mAccessToken, _editingPlaylistId, title)) {
            is SpotifyApiResource.Success -> {
                _toastMessageId.postValue(R.string.result_update_title_success)
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        _toastMessageId.postValue(R.string.result_failed)
                    }
                }
            }
        }
    }

    fun removeTrackFromLocalPlaylist(position:Int){
        val removeTrack = _editingPlaylist.removeAt(position)
        if (removeTrack != null) deleteTracksFromPlaylist(removeTrack.contextUri)
    }

    private fun deleteTracksFromPlaylist(trackUri: String) = viewModelScope.launch {
        if (_editingPlaylistId.isEmpty()) return@launch
        when (val result = repository.deleteTracksFromPlaylist(mAccessToken, _editingPlaylistId, trackUri)) {
            is SpotifyApiResource.Success -> {

            }
            is SpotifyApiResource.Error -> {
                when (result.reason) {
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        _toastMessageId.postValue(R.string.result_failed)
                    }
                }
            }
        }
    }

    fun reorderPlaylistsTracks(initialPosition: Int, finalPosition: Int) = viewModelScope.launch {
        if (_editingPlaylistId.isEmpty()) return@launch
        _editingPlaylist.replacePosition(initialPosition, finalPosition)

        when (val result = repository.reorderPlaylistsTracks(
            mAccessToken,
            _editingPlaylistId,
            initialPosition,
            finalPosition
        )) {
            is SpotifyApiResource.Success -> {

            }
            is SpotifyApiResource.Error -> {
                when (result.reason) {
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        _toastMessageId.postValue(R.string.result_failed)
                    }
                }
            }
        }
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
        val isNotSaved = _editingPlaylistId.isEmpty() || _editingPlaylistId == CREATE_NEW_PLAYLIST_ID
        _isPlaylistUnSaved.postValue(isNotSaved)
    }

    fun clearEditingPlaylist() {
        updatePlaylistId("")
        _editingPlaylistTitle.postValue("")
        _editingPlaylist.postValue(listOf())
    }

    private fun getTracksByPlaylistId(playlistId: String) = viewModelScope.launch {
        _isLoadingPlaylistTrack.value = true
        when (val result = repository.getTrackInfosByPlaylistId(mAccessToken, playlistId)) {
            is SpotifyApiResource.Success -> {
                _editingPlaylist.postValue(result.data ?: listOf())
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        _toastMessageId.postValue(R.string.result_failed)
                    }
                }
            }
        }
        _isLoadingPlaylistTrack.value = false
    }

    private fun refreshAccessToken() {
        _needRefreshAccessToken.postValue(OneShotEvent(true))
    }

}

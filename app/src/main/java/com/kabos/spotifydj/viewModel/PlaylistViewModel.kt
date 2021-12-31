package com.kabos.spotifydj.viewModel

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


    fun createPlaylist(title: String) = viewModelScope.launch {
        when (val result = repository.createPlaylist(mAccessToken, mUserId, title)) {
            is SpotifyApiResource.Success -> {
                _editingPlaylistId = result.data.toString()
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
        //todo createPlaylist時に、editingPalylistの内容をpostする処理
        //localPlaylistのTrackを新規作成したplaylistに追加 空なら何もしない
//        if (localPlaylist.value == null) return@launch
//        val requestBody = AddTracksBody(localPlaylist.value?.map { it.contextUri }!!)
//        repository.addTracksToPlaylist(mAccessToken, _editingPlaylistId, requestBody)
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

    //onAdd callback
    // todo playlist replaceの処理必要？
    fun addTrackToEditingPlaylist(track: TrackInfo){
        _editingPlaylist.addItem(track)
        addTrackToPlaylist(track.contextUri)
    }


    private fun addTrackToPlaylist(trackUri: String) = viewModelScope.launch {
        if (_editingPlaylistId.isEmpty()) return@launch
        when (val result = repository.addTracksToPlaylist(mAccessToken, _editingPlaylistId, trackUri)) {
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


    //onSwipe callback
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

    fun loadPlaylistIntoPlaylistFragment(playlist: PlaylistItem) = viewModelScope.launch {
        _editingPlaylistId = playlist.id
        _editingPlaylistTitle.postValue(playlist.name)
        getTracksByPlaylistId(playlist.id)
    }

    fun clearEditingPlaylist() {
        _editingPlaylistId = ""
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

    fun updateEditingPlaylistTitle(title: String) {
        _editingPlaylistTitle.postValue(title)
    }

    private fun refreshAccessToken() {
        _needRefreshAccessToken.postValue(OneShotEvent(true))
    }

}

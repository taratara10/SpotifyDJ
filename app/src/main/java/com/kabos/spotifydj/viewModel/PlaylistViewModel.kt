package com.kabos.spotifydj.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private var editingPlaylistId = ""

    private val _editingPlaylist = MutableLiveData<List<TrackInfo>>()
    private val _editingPlaylistTitle = MutableLiveData<String>()
    private val _usersPlaylist = MutableLiveData<List<PlaylistItem>>()
    private val _userCreatedPlaylist = MutableLiveData<List<PlaylistItem>>()
    private val _isLoadingPlaylistTrack = MutableLiveData(false)
    private val _needRefreshAccessToken = MutableLiveData<OneShotEvent<Boolean>>()

    val usersPlaylist: LiveData<List<PlaylistItem>>
        get() = _usersPlaylist
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
                _usersPlaylist.postValue(playlist)
                filterUserCreatedPlaylist(playlist)
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    is SpotifyApiErrorReason.NotFound,
                    is SpotifyApiErrorReason.ResponseError,
                    is SpotifyApiErrorReason.UnKnown -> {
                        //error handle
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
                editingPlaylistId = result.data.toString()
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    is SpotifyApiErrorReason.NotFound,
                    is SpotifyApiErrorReason.ResponseError,
                    is SpotifyApiErrorReason.UnKnown -> {
//                            Toast.makeText(this@UserViewModel,"fail",Toast.LENGTH_SHORT).
                    }
                }
            }
        }
        //todo createPlaylist時に、editingPalylistの内容をpostする処理
        //localPlaylistのTrackを新規作成したplaylistに追加 空なら何もしない
//        if (localPlaylist.value == null) return@launch
//        val requestBody = AddTracksBody(localPlaylist.value?.map { it.contextUri }!!)
//        repository.addTracksToPlaylist(mAccessToken, editingPlaylistId, requestBody)
    }

    //ExistingPlaylistのtitleを変更する
    fun updatePlaylistTitle(title: String) = viewModelScope.launch {
        if (title.isEmpty()) return@launch

        when (val result = repository.updatePlaylistTitle(mAccessToken, editingPlaylistId, title)) {
            is SpotifyApiResource.Success -> {
                //todo Toast(タイトルを更新しました)
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        //todo Toast出したい
                    }
                }
            }
        }
    }

    //onAdd callback
    // todo playlist replaceの処理必要？
    fun addTrackToEditingPlaylist(track: TrackInfo){
        _editingPlaylist.addItem(track)
        postTracksToPlaylist(track.contextUri)
    }


    //addItemToCurrentPlaylistと名前が似てるので、add -> postに変更した
    private fun postTracksToPlaylist(trackUri: String) = viewModelScope.launch {
        if (editingPlaylistId.isEmpty()) return@launch
        when (val result = repository.addTracksToPlaylist(mAccessToken, editingPlaylistId, trackUri)) {
            is SpotifyApiResource.Success -> {

            }
            is SpotifyApiResource.Error -> {
                when (result.reason) {
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        //todo Toast出したい
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
        if (editingPlaylistId.isEmpty()) return@launch
        when (val result = repository.deleteTracksFromPlaylist(mAccessToken, editingPlaylistId, trackUri)) {
            is SpotifyApiResource.Success -> {

            }
            is SpotifyApiResource.Error -> {
                when (result.reason) {
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        //todo Toast出したい
                    }
                }
            }
        }
    }

    fun reorderPlaylistsTracks(initialPosition: Int, finalPosition: Int) = viewModelScope.launch {
        if (editingPlaylistId.isEmpty()) return@launch
        _editingPlaylist.replacePosition(initialPosition, finalPosition)

        when (val result = repository.reorderPlaylistsTracks(
            mAccessToken,
            editingPlaylistId,
            initialPosition,
            finalPosition
        )) {
            is SpotifyApiResource.Success -> {

            }
            is SpotifyApiResource.Error -> {
                when (result.reason) {
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        //todo Toast出したい
                    }
                }
            }
        }
    }

    /**
     * Dialog Playlist
     * */

    fun loadPlaylistIntoPlaylistFragment(playlist: PlaylistItem) = viewModelScope.launch {
        updatePlaylistTitleAndId(playlist)
        getTracksByPlaylistId(playlist.id)
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
                    else  -> {
                        //error handle
                    }
                }
            }
        }
        _isLoadingPlaylistTrack.value = false
    }

    private fun updatePlaylistTitleAndId(playlist: PlaylistItem){
        editingPlaylistId = playlist.id
        _editingPlaylistTitle.postValue(playlist.name)
    }


    fun updateEditingPlaylistTitle(title: String) {
        _editingPlaylistTitle.postValue(title)
    }

    private fun refreshAccessToken() {
        _needRefreshAccessToken.postValue(OneShotEvent(true))
    }

}

package com.kabos.spotifydj.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.R
import com.kabos.spotifydj.data.model.TrackInfo
import com.kabos.spotifydj.data.model.playlist.PlaylistItem
import com.kabos.spotifydj.data.repository.PlaylistRepository
import com.kabos.spotifydj.data.repository.TrackRepository
import com.kabos.spotifydj.util.addItem
import com.kabos.spotifydj.util.constant.PlaylistConstant
import com.kabos.spotifydj.util.removeAt
import com.kabos.spotifydj.util.replacePosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditingPlaylistViewModel  @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val trackRepository: TrackRepository
) : BaseViewModel() {
    private var _editingPlaylistId = ""
    private val _editingPlaylist = MediatorLiveData<List<TrackInfo>>()
    private val _editingPlaylistTitle = MutableLiveData<String>()
    private val _isPlaylistUnSaved = MutableLiveData(false)

    val editingPlaylist: LiveData<List<TrackInfo>>
        get() = _editingPlaylist
    val editingPlaylistTitle: LiveData<String>
        get() = _editingPlaylistTitle
    val isPlaylistUnSaved: LiveData<Boolean>
        get() = _isPlaylistUnSaved

    fun createPlaylist(userId: String, title: String, trackUris: List<String>) = viewModelScope.launch {
        runCatching {
            val playlistId = playlistRepository.createPlaylist(userId, title)
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

    // todo 不正なID代入してそう
    private fun addTracksToPlaylist(trackUris: List<String>) = viewModelScope.launch {
        if (_editingPlaylistId.isEmpty() || _editingPlaylistId == PlaylistConstant.CREATE_NEW_PLAYLIST_ID) return@launch
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
        runCatching {
            val removeTrack = _editingPlaylist.removeAt(position)
            if (removeTrack != null) deleteTracksFromPlaylist(removeTrack.contextUri)
        }
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

    fun loadPlaylistIntoEditPlaylistFragment(playlist: PlaylistItem) = viewModelScope.launch {
        _editingPlaylistTitle.postValue(playlist.name)
        updatePlaylistId(playlist.id)
        getTracksByPlaylistId(playlist.id)
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

    private fun updatePlaylistId(playlistId: String) {
        _editingPlaylistId = playlistId
        verifyPlaylistIsSaved()
    }

    private fun verifyPlaylistIsSaved() {
        val isNotSaved =
            _editingPlaylistId.isEmpty() || _editingPlaylistId == PlaylistConstant.CREATE_NEW_PLAYLIST_ID
        _isPlaylistUnSaved.postValue(isNotSaved)
    }
}

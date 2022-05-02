package com.kabos.spotifydj.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.R
import com.kabos.spotifydj.data.model.TrackInfo
import com.kabos.spotifydj.data.model.exception.SpotifyApiException
import com.kabos.spotifydj.data.repository.TrackRepository
import com.kabos.spotifydj.util.OneShotEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val trackRepository: TrackRepository) :
    BaseViewModel() {
    private var searchTrackJob: Job? = null

    private val _searchTracks = MutableLiveData<List<TrackInfo>>()
    val searchTracks: LiveData<List<TrackInfo>> = _searchTracks

    private val _isLoadingSearchTrack = MutableLiveData(false)
    val isLoadingSearchTrack: LiveData<Boolean> = _isLoadingSearchTrack

    fun searchTracks(keyword: String) {
        searchTrackJob?.cancel()
        if (keyword.isEmpty()) {
            clearSearchTracks()
            _isLoadingSearchTrack.postValue(false)
            return
        }
        searchTrackJob = viewModelScope.launch {
            _isLoadingSearchTrack.value = true
            runCatching {
                val trackInfo = trackRepository.searchTrackInfo(keyword)
                _searchTracks.postValue(trackInfo)
            }.onFailure { errorHandle(it) }
            _isLoadingSearchTrack.postValue(false)
        }
    }

    private fun clearSearchTracks() {
        _searchTracks.postValue(listOf())
    }

    // todo 名前が良くない
    fun loadPlaylistIntoSearchFragment(playlistId: String) = viewModelScope.launch {
        _isLoadingSearchTrack.value = true
        runCatching {
            val trackInfo = trackRepository.getTrackInfosByPlaylistId(playlistId)
            _searchTracks.postValue(trackInfo)
        }.onFailure { errorHandle(it) }
        _isLoadingSearchTrack.postValue(false)
    }

}

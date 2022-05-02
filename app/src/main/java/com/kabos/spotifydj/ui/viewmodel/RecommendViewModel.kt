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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val trackRepository: TrackRepository
) : BaseViewModel() {
    private val _upperTracks = MutableLiveData<List<TrackInfo>>()
    private val _downerTracks = MutableLiveData<List<TrackInfo>>()
    private val _currentTrack = MutableLiveData<List<TrackInfo>>()
    private val _isLoadingUpperTrack = MutableLiveData(false)
    private val _isLoadingDownerTrack = MutableLiveData(false)

    val upperTracks: LiveData<List<TrackInfo>>
        get() = _upperTracks
    val downerTracks: LiveData<List<TrackInfo>>
        get() = _downerTracks
    val currentTrack: LiveData<List<TrackInfo>>
        get() = _currentTrack
    val isLoadingUpperTrack: LiveData<Boolean>
        get() = _isLoadingUpperTrack
    val isLoadingDownerTrack: LiveData<Boolean>
        get() = _isLoadingDownerTrack

    fun updateCurrentTrack(track: TrackInfo) {
        _currentTrack.postValue(listOf(track))
        updateUpperRecommendTrack(track)
        updateDownerRecommendTrack(track)
    }

    private fun updateUpperRecommendTrack(trackInfo: TrackInfo) = viewModelScope.launch {
        _isLoadingUpperTrack.value = true
        runCatching {
            _upperTracks.postValue(trackRepository.getRecommendTrackInfos(trackInfo, true))
        }.onFailure { errorHandle(it) }
        _isLoadingUpperTrack.value = false
    }

    private fun updateDownerRecommendTrack(trackInfo: TrackInfo) = viewModelScope.launch {
        _isLoadingDownerTrack.value = true
        runCatching {
            _downerTracks.postValue(trackRepository.getRecommendTrackInfos(trackInfo, false))
        }.onFailure { errorHandle(it) }
        _isLoadingDownerTrack.value = false
    }

}

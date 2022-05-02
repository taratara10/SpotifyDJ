package com.kabos.spotifydj.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.R
import com.kabos.spotifydj.data.model.TrackInfo
import com.kabos.spotifydj.data.model.apiResult.SpotifyApiErrorReason
import com.kabos.spotifydj.data.model.apiResult.SpotifyApiResource
import com.kabos.spotifydj.repository.Repository
import com.kabos.spotifydj.util.OneShotEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendViewModel @Inject constructor(private val repository: Repository): ViewModel() {
    private var mAccessToken = ""
    private val _upperTracks = MutableLiveData<List<TrackInfo>>()
    private val _downerTracks = MutableLiveData<List<TrackInfo>>()
    private val _currentTrack = MutableLiveData<List<TrackInfo>>()
    private val _isLoadingUpperTrack = MutableLiveData(false)
    private val _isLoadingDownerTrack = MutableLiveData(false)
    private val _needRefreshAccessToken = MutableLiveData<OneShotEvent<Boolean>>()
    private val _toastMessageId = MutableLiveData<Int>()

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
    val needRefreshAccessToken: LiveData<OneShotEvent<Boolean>>
        get() = _needRefreshAccessToken
    val toastMessageId: LiveData<Int>
        get() = _toastMessageId

    fun initAccessToken(token: String) {
        mAccessToken = token
    }

    fun updateCurrentTrack(track: TrackInfo){
        _currentTrack.postValue(listOf(track))
        updateUpperRecommendTrack(track)
        updateDownerRecommendTrack(track)
    }

    private fun updateUpperRecommendTrack(trackInfo: TrackInfo) = viewModelScope.launch {
        _isLoadingUpperTrack.value = true
        when (val result = repository.getRecommendTrackInfos(mAccessToken, trackInfo, true)) {
            is SpotifyApiResource.Success -> {
                _upperTracks.postValue(result.data ?: listOf())
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
        _isLoadingUpperTrack.value = false
    }

    private fun updateDownerRecommendTrack(trackInfo: TrackInfo) = viewModelScope.launch {
        _isLoadingDownerTrack.value = true
        when (val result = repository.getRecommendTrackInfos(mAccessToken, trackInfo, false)) {
            is SpotifyApiResource.Success -> {
                _downerTracks.postValue(result.data ?: listOf())
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
        _isLoadingDownerTrack.value = false
    }

    private fun refreshAccessToken() {
        _needRefreshAccessToken.postValue(OneShotEvent(true))
    }
}

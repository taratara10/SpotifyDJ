package com.kabos.spotifydj.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.model.apiResult.SpotifyApiErrorReason
import com.kabos.spotifydj.model.apiResult.SpotifyApiResource
import com.kabos.spotifydj.repository.Repository
import com.kabos.spotifydj.util.OneShotEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel@Inject constructor(private val repository: Repository): ViewModel() {
    private var mAccessToken = ""
    private var searchTrackJob: Job? = null

    private val _searchTracks = MutableLiveData<List<TrackInfo>>()
    private val _isLoadingSearchTrack = MutableLiveData(false)
    private val _needRefreshAccessToken = MutableLiveData<OneShotEvent<Boolean>>()
    val searchTracks: LiveData<List<TrackInfo>>
        get() = _searchTracks
    val isLoadingSearchTrack: LiveData<Boolean>
        get() = _isLoadingSearchTrack
    val needRefreshAccessToken: LiveData<OneShotEvent<Boolean>>
        get() = _needRefreshAccessToken


    fun initAccessToken(token: String) {
        mAccessToken = token
    }

    fun searchTracks(keyword: String) {
        searchTrackJob?.cancel()
        if (keyword.isEmpty()) {
            clearSearchTracks()
            _isLoadingSearchTrack.postValue(false)
            return
        }
        searchTrackJob = viewModelScope.launch {
            _isLoadingSearchTrack.value = true
            when (val result = repository.searchTrackInfo(mAccessToken, keyword)) {
                is SpotifyApiResource.Success -> {
                    _searchTracks.postValue(result.data ?: listOf())
                }
                is SpotifyApiResource.Error -> {
                    when (result.reason) {
                        is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                        is SpotifyApiErrorReason.NotFound,
                        is SpotifyApiErrorReason.ResponseError,
                        is SpotifyApiErrorReason.UnKnown -> {
                            //todo display onFetchFailed textView or Toast
                        }
                    }
                }
            }
            _isLoadingSearchTrack.postValue(false)
        }
    }

    private fun clearSearchTracks() {
        _searchTracks.postValue(listOf())
    }

    fun loadPlaylistIntoSearchFragment(playlistId: String) = viewModelScope.launch{
        _isLoadingSearchTrack.value = true
        when (val result = repository.getTrackInfosByPlaylistId(mAccessToken, playlistId)) {
            is SpotifyApiResource.Success -> {
                 _searchTracks.postValue(result.data ?: listOf())
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
        _isLoadingSearchTrack.postValue(false)
    }

    private fun refreshAccessToken() {
        _needRefreshAccessToken.postValue(OneShotEvent(true))
    }
}

package com.kabos.spotifydj.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.R
import com.kabos.spotifydj.data.model.TrackInfo
import com.kabos.spotifydj.data.model.User
import com.kabos.spotifydj.data.model.apiResult.SpotifyApiErrorReason
import com.kabos.spotifydj.data.model.apiResult.SpotifyApiResource
import com.kabos.spotifydj.data.model.playback.Device
import com.kabos.spotifydj.data.repository.Repository
import com.kabos.spotifydj.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repository: Repository): ViewModel() {
    private var mDeviceId = ""
    private val _userAccount = MutableLiveData<User>()
    private val _needRefreshAccessToken = MutableLiveData<OneShotEvent<Boolean>>()
    private val _toastMessageId = MutableLiveData<Int>()

    val startExternalSpotifyApp = MutableLiveData(false)


    val needRefreshAccessToken: LiveData<OneShotEvent<Boolean>>
        get() = _needRefreshAccessToken
    val userAccount: LiveData<User>
        get() = _userAccount
    val toastMessageId: LiveData<Int>
        get() = _toastMessageId

    fun getUserAccount() = viewModelScope.launch {
        when (val result = repository.getUsersProfile()) {
            is SpotifyApiResource.Success -> {
                val user = result.data ?: return@launch
                _userAccount.postValue(user)
            }
            is SpotifyApiResource.Error -> {
                when (result.reason) {
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else -> {
                        Timber.d("${result.reason}")
                    }
                }
            }
        }
    }

    /**
     * Playback
     * */

    //todo deviceIdをSharePrefから取り出して初期化する
    fun initializeDeviceId(deviceId: String){
        mDeviceId = deviceId
    }

    private fun getUsersDevices() = viewModelScope.launch {
        when (val result = repository.getUsersDevices()) {
            is SpotifyApiResource.Success -> {
                //sharedPrefに詰めて運用したかったけど、activeじゃないとdeviceId指定しても404
                //なので、毎回Spotifyアプリを開いて、deviceIdを取得
                val userDevice: Device? =result.data?.find { it.type == "Smartphone" }
                if (userDevice != null) {
                    mDeviceId = userDevice.id
                }else {
                    startExternalSpotifyApp.postValue(true)
                }
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    is SpotifyApiErrorReason.NotFound -> startExternalSpotifyApp.postValue(true)
                    else -> {
                        _toastMessageId.postValue(R.string.result_failed)
                    }
                }
            }
        }


    }

    fun playbackTrack(trackInfo: TrackInfo) = viewModelScope.launch {
        if (mDeviceId.isEmpty()) getUsersDevices()

        when (val result = repository.playbackTrack(mDeviceId, trackInfo.contextUri)) {
            is SpotifyApiResource.Success -> {
               //icon変えたりする？
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    is SpotifyApiErrorReason.NotFound -> getUsersDevices()
                    else -> {
                        _toastMessageId.postValue(R.string.result_failed)
                    }
                }
            }
        }
    }
//        //isPlaybackによって、再生、停止を行う
//        if (trackInfo.isPlayback){
//            repository.pausePlayback(mAccessToken,mDeviceId)
//        }else {
//            repository.playbackTrack(mDeviceId, trackInfo.contextUri)
//        }
//        togglePlaybackIcon(trackInfo)


//    //▶の再生アイコンを切り替える
//    private fun togglePlaybackIcon(trackInfo: TrackInfo){
//        replaceTrackToPlaybackTrack(trackInfo,searchTrackList)
//        replaceTrackToPlaybackTrack(trackInfo,upperTrackList)
//        replaceTrackToPlaybackTrack(trackInfo,_downerTracs)
//        replaceTrackToPlaybackTrack(trackInfo,localPlaylist as MutableLiveData<List<TrackInfo>?>)
//
//        //currentTrackはListじゃないので別処理
//        if(currentTrack.value == trackInfo){
//            trackInfo.isPlayback = !trackInfo.isPlayback
//            currentTrack.postValue(trackInfo)
//        }
//    }
//
//
//    private fun replaceTrackToPlaybackTrack(trackInfo: TrackInfo,trackList:MutableLiveData<List<TrackInfo>?>){
//        if(trackList.value == null) return
//
//        val list = trackList.value!!.toMutableList()
//
//        for (item in list){
//            //他の再生中アイコンをリセット
//            if (item.isPlayback) item.isPlayback = false
//            //再生したいTrackがあれば変更
//            if (item == trackInfo) item.isPlayback = true
//        }
//        trackList.value = list
//    }


    fun refreshAccessToken() {
        _needRefreshAccessToken.postValue(OneShotEvent(true))
    }
}

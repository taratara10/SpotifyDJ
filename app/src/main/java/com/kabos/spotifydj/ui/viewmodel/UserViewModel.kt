package com.kabos.spotifydj.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.R
import com.kabos.spotifydj.data.model.TrackInfo
import com.kabos.spotifydj.data.model.exception.SpotifyApiException
import com.kabos.spotifydj.data.model.exception.TokenExpiredException
import com.kabos.spotifydj.data.model.playback.Device
import com.kabos.spotifydj.data.repository.UserRepository
import com.kabos.spotifydj.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UserRepository) :
    BaseViewModel() {
    private var mUserId = ""
    private var mUserName = ""
    private var mDeviceId = ""

    val startExternalSpotifyApp = MutableLiveData(false)
    val userId
        get() = mUserId
    val userName
        get() = mUserName

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

    //todo deviceIdをSharePrefから取り出して初期化する
    fun initializeDeviceId(deviceId: String) {
        mDeviceId = deviceId
    }

    private fun getUsersDevices() = viewModelScope.launch {
        runCatching {
            val devices = userRepository.getUsersDevices()
            // TODO ここでデバイス選択ダイアログだしたらよさげ

            //sharedPrefに詰めて運用したかったけど、activeじゃないとdeviceId指定しても404
            //なので、毎回Spotifyアプリを開いて、deviceIdを取得
            val userDevice: Device? = devices.find { it.type == "Smartphone" }
            if (userDevice != null) {
                mDeviceId = userDevice.id
            } else {
                startExternalSpotifyApp.postValue(true)
            }
        }.onFailure { errorHandle(it) }

    }

    fun playbackTrack(trackInfo: TrackInfo) = viewModelScope.launch {
        if (mDeviceId.isEmpty()) getUsersDevices()

        runCatching {
            userRepository.playbackTrack(mDeviceId, trackInfo.contextUri)
        }.onFailure {
            if (it is SpotifyApiException) {
                when (it) {
                    is SpotifyApiException.UnAuthorized -> _needRefreshAccessToken.postValue(
                        OneShotEvent(Unit)
                    )
                    is SpotifyApiException.NotFound -> getUsersDevices()
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

}

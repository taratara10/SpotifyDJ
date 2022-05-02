package com.kabos.spotifydj.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kabos.spotifydj.R
import com.kabos.spotifydj.data.model.exception.SpotifyApiException
import com.kabos.spotifydj.data.model.exception.TokenExpiredException
import com.kabos.spotifydj.util.OneShotEvent
import timber.log.Timber

open class BaseViewModel : ViewModel(){
    protected val _apiError: MutableLiveData<Throwable> = MutableLiveData()
    val apiError: LiveData<Throwable> = _apiError

    protected val _toastMessageId = MutableLiveData<Int>()
    val toastMessageId: LiveData<Int>
        get() = _toastMessageId

    protected val _needRefreshAccessToken = MutableLiveData<OneShotEvent<Unit>>()
    val needRefreshAccessToken: LiveData<OneShotEvent<Unit>>
        get() = _needRefreshAccessToken


    fun errorHandle(exception: Throwable) {
        when (exception) {
            is SpotifyApiException -> {
                when (exception) {
                    is SpotifyApiException.UnAuthorized -> _needRefreshAccessToken.postValue(OneShotEvent(Unit))
                    else -> _toastMessageId.postValue(R.string.result_failed)
                }
            }
            is TokenExpiredException -> {
                _needRefreshAccessToken.postValue(OneShotEvent(Unit))
            }
        }

        Timber.d("--api error $exception")
    }

}

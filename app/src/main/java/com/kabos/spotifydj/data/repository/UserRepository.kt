package com.kabos.spotifydj.data.repository

import com.kabos.spotifydj.data.api.UserApi
import com.kabos.spotifydj.data.model.User
import com.kabos.spotifydj.data.model.playback.Device
import com.kabos.spotifydj.data.model.requestBody.PlaybackBody
import com.kabos.spotifydj.util.errorHandling
import javax.inject.Inject

class UserRepository @Inject constructor(private val userApi: UserApi) {

    suspend fun getUsersProfile(): User {
        return userApi.getUsersProfile().errorHandling()
    }

    suspend fun playbackTrack(
        deviceId: String,
        contextUri: String
    ) : Boolean{
        userApi.playback(
            deviceId = deviceId,
            body = PlaybackBody(uris = listOf(contextUri))
        ) .errorHandling()
        return true
    }

    suspend fun pausePlayback(deviceId: String): Boolean {

        userApi.pausePlayback(deviceId).errorHandling()

        // TODO これ、、、Unit返せばいいな
        return true
    }

    suspend fun getUsersDevices(): List<Device> {
        return userApi.getUsersDevices().errorHandling().devices
    }

}

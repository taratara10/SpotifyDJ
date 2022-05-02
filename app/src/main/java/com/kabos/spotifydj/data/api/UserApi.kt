package com.kabos.spotifydj.data.api

import com.kabos.spotifydj.data.model.User
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants
import com.kabos.spotifydj.data.model.playback.Devices
import com.kabos.spotifydj.data.model.requestBody.PlaybackBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserApi {

    @GET("me")
    suspend fun getUsersProfile(): Response<User>

    @GET("me/player/devices")
    suspend fun getUsersDevices(): Response<Devices>

    @PUT("me/player/play")
    suspend fun playback(
        @Query(ApiConstants.DEVICE_ID) deviceId: String,
        @Body body: PlaybackBody
    ): Response<Unit>

    @PUT("me/player/pause")
    suspend fun pausePlayback(
        @Query(ApiConstants.DEVICE_ID) deviceId: String,
    ): Response<Unit>
}

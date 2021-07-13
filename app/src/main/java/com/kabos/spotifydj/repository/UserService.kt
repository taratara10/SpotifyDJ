package com.kabos.spotifydj.repository

import android.content.SharedPreferences
import com.kabos.spotifydj.model.*
import retrofit2.Response
import retrofit2.http.*

interface UserService {
    @GET("me")
    suspend fun getUser(@Header("Authorization") accessToken: String):Response<User>

    @GET("me/playlists")
    suspend fun getCurrentPlaylist(@Header("Authorization")accessToken: String): Response<Playlist>

    @GET("me/player/recently-played")
    suspend fun getRecentlyPlayed(@Header("Authorization")accessToken: String): Response<RecentlyPlaylist>

    @GET("me/player/devices")
    suspend fun getCurrentPlayback(@Header("Authorization")accessToken: String): Response<Devices>

    @PUT("me/player/play")
    suspend fun playback(@Header("Authorization")accessToken: String,
                         @Body body:Playback,
                         @Query("device_id")id: String
    )

}

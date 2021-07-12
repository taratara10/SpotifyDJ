package com.kabos.spotifydj.repository

import android.content.SharedPreferences
import com.kabos.spotifydj.model.CurrentPlayback
import com.kabos.spotifydj.model.Playlist
import com.kabos.spotifydj.model.RecentlyPlaylist
import com.kabos.spotifydj.model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserService {
    @GET("me")
    suspend fun getUser(@Header("Authorization") accessToken: String):Response<User>

    @GET("me/playlists")
    suspend fun getCurrentPlaylist(@Header("Authorization")accessToken: String): Response<Playlist>

    @GET("me/player/recently-played")
    suspend fun getRecentlyPlayed(@Header("Authorization")accessToken: String): Response<RecentlyPlaylist>

    @GET("me/player")
    suspend fun getCurrentPlayback(@Header("Authorization")accessToken: String): Response<CurrentPlayback>

    @PUT("me/player/play")
    suspend fun playback(@Header("Authorization")accessToken: String)
}

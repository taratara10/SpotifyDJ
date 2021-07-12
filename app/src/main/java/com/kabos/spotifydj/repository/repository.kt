package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.CurrentPlayback
import com.kabos.spotifydj.model.Playlist
import com.kabos.spotifydj.model.RecentlyPlaylist
import com.kabos.spotifydj.model.User
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class Repository @Inject constructor( private val userService: UserService) {


    suspend fun getUser(accessToken: String): Response<User> =
        userService.getUser("Bearer $accessToken")

    suspend fun getPlaylist(accessToken: String): Response<Playlist> =
        userService.getCurrentPlaylist("Bearer $accessToken")

    suspend fun getRecentlyPlayed(accessToken: String): Response<RecentlyPlaylist> =
        userService.getRecentlyPlayed("Bearer $accessToken")

    suspend fun playback(accessToken: String) {
        userService.playback("Bearer $accessToken")
    }

    suspend fun getCurrentPlayback(accessToken: String): Response<CurrentPlayback> =
        userService.getCurrentPlayback("Bearer $accessToken")
}

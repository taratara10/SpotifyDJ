package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.*
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.feature.AudioFeatures
import com.kabos.spotifydj.model.track.SearchTracks
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class Repository @Inject constructor( private val userService: UserService) {

    val body = Playback("spotify:album:5ht7ItJgpBH7W6vJ5BqpPr", Offset(5),0)
    val id = "88568a2c2385a4eb6d662a06a4b3c799ea973cc2"

    suspend fun getUser(accessToken: String): Response<User> =
        userService.getUser("Bearer $accessToken")

    suspend fun getPlaylist(accessToken: String): Response<Playlist> =
        userService.getCurrentPlaylist("Bearer $accessToken")

    suspend fun getRecentlyPlayed(accessToken: String): Response<RecentlyPlaylist> =
        userService.getRecentlyPlayed("Bearer $accessToken")

//    suspend fun playback(accessToken: String) {
//        userService.playback("Bearer $accessToken",id)
//    }

    suspend fun getCurrentPlayback(accessToken: String): Response<Devices> =
        userService.getCurrentPlayback("Bearer $accessToken")


    suspend fun searchTracks(accessToken: String, keyword: String) : Response<SearchTracks> =
         userService.searchTracks("Bearer $accessToken",keyword, "track")

    suspend fun getAudioFeaturesById(accessToken: String, id: String) : Response<AudioFeature> =
        userService.getAudioFeaturesById("Bearer $accessToken", id)
}

package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.*
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.track.SearchTracks
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor( private val userService: UserService) {

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


    suspend fun getTracksByKeyword(accessToken: String, keyword: String) : Response<SearchTracks> =
         userService.getTracksByKeyword("Bearer $accessToken",keyword, "track")

    suspend fun getAudioFeaturesById(accessToken: String, id: String) : Response<AudioFeature> =
        userService.getAudioFeaturesById("Bearer $accessToken", id)

    suspend fun getRecommendTracks(accessToken: String, trackInfo: TrackInfo, isUpper: Boolean): Response<RecommendTracks> {
        val minTempoRate = 0.9
        val maxTempoRate = 1.1
        val minDanceabilityRate = 0.8
        val maxDanceabilityRate = 1.2
        var minEnergyRate = 1.0
        var maxEnergyRate = 1.0

        //UpperTrackListを返したいならEnergyを1.0~1.2、Downerは0.8~1.0に調整
        if (isUpper) maxEnergyRate = 1.2
        if (!isUpper) minEnergyRate = 0.8
        return userService.getRecommendations(accessToken = "Bearer $accessToken",
            seedTrackId = trackInfo.id,
            minTempo = (trackInfo.tempo * minTempoRate).toInt(),
            maxTempo = (trackInfo.tempo * maxTempoRate).toInt(),
            minDancebility = (trackInfo.danceability * minDanceabilityRate).toInt(),
            maxDancebility = (trackInfo.danceability * maxDanceabilityRate).toInt(),
            minEnergy = (trackInfo.energy * minEnergyRate).toInt(),
            maxEnergy = (trackInfo.energy * maxEnergyRate).toInt(),
        )
    }



}

package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.*
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playlist.AddItemToPlaylistBody
import com.kabos.spotifydj.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.model.playlist.Playlist
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.track.SearchTracks
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor( private val userService: UserService) {

    //@HeaderのaccessTokenは必ず、この関数を通して入力する
    private fun generateBearer(accessToken: String) = "Bearer $accessToken"

    suspend fun getUsersProfile(accessToken: String): Response<User> =
        userService.getUsersProfile(generateBearer(accessToken))
//
//    suspend fun getPlaylist(accessToken: String): Response<Playlist> =
//        userService.getCurrentPlaylist("Bearer $accessToken")
//
//    suspend fun getRecentlyPlayed(accessToken: String): Response<RecentlyPlaylist> =
//        userService.getRecentlyPlayed("Bearer $accessToken")
//
////    suspend fun playback(accessToken: String) {
////        userService.playback("Bearer $accessToken",id)
////    }
//
//    suspend fun getCurrentPlayback(accessToken: String): Response<Devices> =
//        userService.getCurrentPlayback("Bearer $accessToken")


    suspend fun getTracksByKeyword(accessToken: String, keyword: String) : Response<SearchTracks> =
         userService.getTracksByKeyword(generateBearer(accessToken),keyword, "album,track,artist")

    suspend fun getAudioFeaturesById(accessToken: String, id: String) : Response<AudioFeature> =
        userService.getAudioFeaturesById(generateBearer(accessToken), id)

    suspend fun getRecommendTracks(accessToken: String, trackInfo: TrackInfo, fetchUpperTrack: Boolean): Response<RecommendTracks> {
        val minTempoRate = 0.9
        val maxTempoRate = 1.1
        val minDanceabilityRate = 0.8
        val maxDanceabilityRate = 1.2
        var minEnergyRate = 1.0
        var maxEnergyRate = 1.0

        //UpperTrackListを返したいならEnergyを1.0~1.2、Downerは0.8~1.0に調整
        if (fetchUpperTrack) maxEnergyRate = 1.2
        if (!fetchUpperTrack) minEnergyRate = 0.8
        return userService.getRecommendations(accessToken = generateBearer(accessToken),
            seedTrackId = trackInfo.id,
            minTempo = trackInfo.tempo * minTempoRate,
            maxTempo = trackInfo.tempo * maxTempoRate,
            minDancebility = trackInfo.danceability * minDanceabilityRate,
            maxDancebility = trackInfo.danceability * maxDanceabilityRate,
            minEnergy = trackInfo.energy * minEnergyRate,
            maxEnergy = trackInfo.energy * maxEnergyRate,
        )
    }

    suspend fun createPlaylist(accessToken: String, userId: String, title: String): Response<PlaylistItem> =
        userService.createPlaylist(
            accessToken = generateBearer(accessToken),
            userId = userId,
            body = CreatePlaylistBody(name = title))

    suspend fun getUsersPlaylist(accessToken: String): Response<Playlist> =
        userService.getUsersPlaylists(generateBearer(accessToken))

    suspend fun addItemToPlaylist(accessToken: String,playlistId: String, body: AddItemToPlaylistBody) {
        userService.addItemsToPlaylist(
            accessToken = generateBearer(accessToken),
            contentType = "application/json",
            playlistId = playlistId,
            body = body
        )
    }
}

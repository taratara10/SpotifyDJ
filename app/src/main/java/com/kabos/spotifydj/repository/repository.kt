package com.kabos.spotifydj.repository

import android.util.Log
import com.kabos.spotifydj.model.*
import com.kabos.spotifydj.model.PlaylistById.PlaylistById
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playlist.AddItemToPlaylistBody
import com.kabos.spotifydj.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.model.playlist.Playlist
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.track.SearchTracks
import com.kabos.spotifydj.model.track.TrackItems
import com.kabos.spotifydj.model.track.Tracks
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor( private val userService: UserService) {

    //@HeaderのaccessTokenは必ず、この関数を通して入力する
    private fun generateBearer(accessToken: String) = "Bearer $accessToken"

    suspend fun getUsersProfile(accessToken: String): User? {
        val request = userService.getUsersProfile(generateBearer(accessToken))
        return if (request.isSuccessful) request.body()
        else{
            Log.d("initializeUserId","failed")
            null
        }
    }
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


    suspend fun getTracksByKeyword(accessToken: String, keyword: String) : List<TrackItems>? {
        val request = userService.getTracksByKeyword(
            accessToken= generateBearer(accessToken),
            keyword = keyword,
            type = "album,track,artist"
        )
        return if (request.isSuccessful) request.body()?.tracks?.items as List<TrackItems>
        else {
            Log.d("getTracksByKeyword","failed")
            null
        }
    }

    suspend fun getAudioFeaturesById(accessToken: String, id: String) : AudioFeature? {
        val request = userService.getAudioFeaturesById(generateBearer(accessToken), id)
        return if (request.isSuccessful) request.body()
        else{
            Log.d("getAudioFeature","getAudioFeature failed")
            null
        }
    }

    suspend fun getRecommendTracks(accessToken: String, trackInfo: TrackInfo, fetchUpperTrack: Boolean)
        : List<TrackItems>? {
        val minTempoRate = 0.9
        val maxTempoRate = 1.1
        val minDanceabilityRate = 0.8
        val maxDanceabilityRate = 1.2
        var minEnergyRate = 1.0
        var maxEnergyRate = 1.0

        //UpperTrackListを返したいならEnergyを1.0~1.2、Downerは0.8~1.0に調整
        if (fetchUpperTrack) maxEnergyRate = 1.2
        if (!fetchUpperTrack) minEnergyRate = 0.8
        val request =  userService.getRecommendations(accessToken = generateBearer(accessToken),
            seedTrackId = trackInfo.id,
            minTempo = trackInfo.tempo * minTempoRate,
            maxTempo = trackInfo.tempo * maxTempoRate,
            minDancebility = trackInfo.danceability * minDanceabilityRate,
            maxDancebility = trackInfo.danceability * maxDanceabilityRate,
            minEnergy = trackInfo.energy * minEnergyRate,
            maxEnergy = trackInfo.energy * maxEnergyRate,
        )

        return if (request.isSuccessful) request.body()?.tracks
        else{
            Log.d("getRecommendTracks","getRecommendTracks failed")
            return null
        }
    }

    suspend fun createPlaylist(accessToken: String, userId: String, title: String): String {
       val request = userService.createPlaylist(
           accessToken = generateBearer(accessToken),
           userId = userId,
           body = CreatePlaylistBody(name = title)
       )
        return if(request.isSuccessful) request.body()?.id.toString()
        else {
            Log.d("createPlaylist","failed")
            ""
        }
    }


    suspend fun getUsersAllPlaylist(accessToken: String): Response<Playlist> =
        userService.getUsersAllPlaylists(generateBearer(accessToken))
//
//    suspend fun getPlaylistItemById(accessToken: String,playlistId: String):Response<PlaylistById> {
//        val request = userService.getPlaylistItemById(
//            accessToken = generateBearer(accessToken),
//            playlistId = playlistId
//        )
//        if (request.isSuccessful){
//            return
//
//        }
//    }


    suspend fun addItemToPlaylist(accessToken: String,playlistId: String, body: AddItemToPlaylistBody) {
        userService.addItemsToPlaylist(
            accessToken = generateBearer(accessToken),
            contentType = "application/json",
            playlistId = playlistId,
            body = body
        )
    }
}

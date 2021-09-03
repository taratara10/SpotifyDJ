package com.kabos.spotifydj.repository

import android.util.Log
import com.kabos.spotifydj.model.*
import com.kabos.spotifydj.model.PlaylistById.Item
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playback.Device
import com.kabos.spotifydj.model.requestBody.PlaybackBody
import com.kabos.spotifydj.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.requestBody.AddTracksBody
import com.kabos.spotifydj.model.requestBody.DeleteTracksBody
import com.kabos.spotifydj.model.track.TrackItems
import javax.inject.Inject

class Repository @Inject constructor( private val userService: UserService) {

    //@HeaderのaccessTokenは必ず、この関数を通して入力する
    private fun generateBearer(accessToken: String) = "Bearer $accessToken"



    suspend fun getUsersProfile(accessToken: String): User? {
        val request = userService.getUsersProfile(generateBearer(accessToken))
        return if (request.isSuccessful) request.body()
        else{
            Log.d("getUserProfile","${request.errorBody()?.string()}")
            null
        }
    }

    /**
     * Player
     * */
    suspend fun playbackTrack(
        accessToken: String,
        deviceId: String,
        contextUri: String) {
        try {
            userService.playback(
                accessToken = generateBearer(accessToken),
                deviceId= deviceId,
                body = PlaybackBody(uris = listOf(contextUri))
                )
        }catch (e:Exception){
            Log.d("playbackTrack","failed. $e")
        }

        //todo 必要に応じて、errorHandleのコード書く
    }

    suspend fun pausePlayback(accessToken: String,deviceId: String){
        try {
            userService.pausePlayback(
                accessToken = generateBearer(accessToken),
                deviceId= deviceId
            )
        }catch (e:Exception){
            Log.d("pausePlaybackTrack","failed. $e")
        }
    }

    suspend fun getUsersDevices(accessToken: String): List<Device>? {
        val request = userService.getUsersDevices(generateBearer(accessToken))
        return if (request.isSuccessful){
            request.body()?.devices
        }else{
            Log.d("getCurrentPlayback","${request.errorBody()?.string()}")
            null
        }
    }

    /**
     * Search
     * */

    suspend fun getTracksByKeyword(
        accessToken: String,
        keyword: String,
        onFetchFailed: () -> Unit) : List<TrackItems>? {
        val request = userService.getTracksByKeyword(
            accessToken = generateBearer(accessToken),
            keyword = keyword,
            type = "album,track,artist"
        )

        return if (request.isSuccessful){
            request.body()?.tracks?.items as List<TrackItems>
        }
        else {
            Log.d("getTracksByKeyword","${request.errorBody()?.string()}")
            onFetchFailed()
            null
        }
    }

    suspend fun getAudioFeaturesById(
        accessToken: String,
        id: String,
        onFetchFailed: () -> Unit) : AudioFeature? {
        val request = userService.getAudioFeaturesById(generateBearer(accessToken), id)
        return if (request.isSuccessful) request.body()
        else{
            Log.d("getAudioFeature","${request.errorBody()?.string()}")
            onFetchFailed()
            null
        }
    }

    suspend fun getRecommendTracks(
        accessToken: String,
        trackInfo: TrackInfo,
        fetchUpperTrack: Boolean,
        onFetchFailed: () -> Unit) : List<TrackItems>? {
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
            Log.d("getRecommendTracks","${request.errorBody()?.string()}")
            onFetchFailed()
            return null
        }
    }


    /**
     * Playlist
     * */

    suspend fun getUsersAllPlaylist(accessToken: String): List<PlaylistItem>? {
        val request = userService.getUsersAllPlaylists(generateBearer(accessToken))
        return if (request.isSuccessful) request.body()?.items
        else {
            Log.d("getUserPlaylist","${request.errorBody()?.string()}")
            listOf()
        }

    }

    suspend fun getTracksByPlaylistId(accessToken: String,playlistId: String):List<TrackItems>? {
        val request = userService.getTracksByPlaylistId(
            accessToken = generateBearer(accessToken),
            playlistId = playlistId
            )
        if (request.isSuccessful){
             //List<TrackItem>で扱いたいので、item.tackをmapで取り出す
             val items:List<Item>? = request.body()?.items
             return items?.map { it.track }
        }else{
             Log.d("getPlaylistItemById","${request.errorBody()?.string()}")
             return null
         }
    }

    suspend fun createPlaylist(
        accessToken: String,
        userId: String,
        title: String): String {
        val request = userService.createPlaylist(
            accessToken = generateBearer(accessToken),
            userId = userId,
            body = CreatePlaylistBody(name = title)
        )
        return if(request.isSuccessful) request.body()?.id.toString()
        else {
            Log.d("createPlaylist","${request.errorBody()?.string()}")
            ""
        }
    }

    //todo error handle
    suspend fun addTracksToPlaylist(accessToken: String,playlistId: String, body: AddTracksBody) {
        userService.addTracksToPlaylist(
            accessToken = generateBearer(accessToken),
            contentType = "application/json",
            playlistId = playlistId,
            body = body
        )
    }

    suspend fun deleteTracksFromPlaylist(accessToken: String, playlistId: String, body: DeleteTracksBody) {
        userService.deleteTracksFromPlaylist(
            accessToken = generateBearer(accessToken),
            contentType = "application/json",
            playlistId = playlistId,
            body = body
        )
    }
}

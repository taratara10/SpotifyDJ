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
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor( private val userService: UserService) {

    //@HeaderのaccessTokenは必ず、この関数を通して入力する
    private fun generateBearer(accessToken: String) = "Bearer $accessToken"

    private val apiErrorAdapter = Moshi.Builder().build().adapter(ApiError::class.java)

    private fun <T> errorReasonHandler(body: Response<T>): Reason {
        val apiError = apiErrorAdapter.fromJson(body.errorBody()?.string())!!
        return when (apiError.error.status){
            401  -> Reason.UnAuthorized
            404  -> Reason.NotFound
            else -> Reason.ResponseError(apiError.error.message)
        }
    }

    suspend fun getUsersProfile(accessToken: String): UserResult {
        return try {
            val request = userService.getUsersProfile(generateBearer(accessToken))
            return if (request.isSuccessful) UserResult.Success(request.body()!!)
            else UserResult.Failure(errorReasonHandler(request))
        } catch (e: Exception){
            UserResult.Failure(Reason.UnKnown(e))
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

    suspend fun getTracksByKeyword(accessToken: String, keyword: String): TrackItemsResult{
        return try {
            val request = userService.getTracksByKeyword(
                accessToken = generateBearer(accessToken),
                keyword = keyword,
                type = "album,track,artist")

            return if (request.isSuccessful) TrackItemsResult.Success(request.body()!!.tracks.items)
            else TrackItemsResult.Failure(errorReasonHandler(request))
        } catch (e: Exception){
            TrackItemsResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun getAudioFeaturesById(accessToken: String, id: String) : AudioFeatureResult {
        return try {
            val request = userService.getAudioFeaturesById(generateBearer(accessToken), id)

            return if (request.isSuccessful) AudioFeatureResult.Success(request.body()!!)
            else AudioFeatureResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            AudioFeatureResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun getRecommendTracks(
                accessToken: String,
                trackInfo: TrackInfo,
                fetchUpperTrack: Boolean) : TrackItemsResult {
        //UpperTrackListを返したいならEnergyを1.0~1.2、Downerは0.8~1.0に調整
        var minEnergyRate = RecommendParameter.MinEnergyRate.value //1.0
        var maxEnergyRate = RecommendParameter.MinEnergyRate.value //1.0
        if (fetchUpperTrack) maxEnergyRate *= 1.2
        if (!fetchUpperTrack) minEnergyRate *= 0.8

        return try {
            val request =  userService.getRecommendations(accessToken = generateBearer(accessToken),
                seedTrackId = trackInfo.id,
                minTempo = trackInfo.tempo * RecommendParameter.MinTempoRate.value,
                maxTempo = trackInfo.tempo * RecommendParameter.MaxTempoRate.value,
                minDancebility = trackInfo.danceability * RecommendParameter.MinDanceabilityRate.value,
                maxDancebility = trackInfo.danceability * RecommendParameter.MaxDanceabilityRate.value,
                minEnergy = trackInfo.energy * minEnergyRate,
                maxEnergy = trackInfo.energy * maxEnergyRate,
            )

            return if (request.isSuccessful) TrackItemsResult.Success(request.body()!!.tracks)
            else TrackItemsResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            TrackItemsResult.Failure(Reason.UnKnown(e))
        }
   }


    /**
     * Playlist
     * */

    suspend fun getUsersAllPlaylist(accessToken: String): PlaylistItemsResult {
        return try {
            val request = userService.getUsersAllPlaylists(generateBearer(accessToken))

            return if (request.isSuccessful) PlaylistItemsResult.Success(request.body()!!.items)
            else PlaylistItemsResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            PlaylistItemsResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun getTracksByPlaylistId(accessToken: String,playlistId: String):TrackItemsResult {
        return try {
            val request = userService.getTracksByPlaylistId(
                accessToken = generateBearer(accessToken),
                playlistId = playlistId)

            return if (request.isSuccessful) TrackItemsResult.Success(request.body()!!.items.map { it.track })
            else TrackItemsResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            TrackItemsResult.Failure(Reason.UnKnown(e))
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

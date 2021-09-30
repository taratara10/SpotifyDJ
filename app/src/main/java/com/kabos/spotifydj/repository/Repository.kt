package com.kabos.spotifydj.repository

import android.util.Log
import com.kabos.spotifydj.model.*
import com.kabos.spotifydj.model.networkUtil.*
import com.kabos.spotifydj.model.playback.Device
import com.kabos.spotifydj.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.model.requestBody.*
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor( private val userService: UserService) {

    //@HeaderのaccessTokenは必ず、この関数を通して入力する
    private fun generateBearer(accessToken: String) = "Bearer $accessToken"

    private val apiErrorAdapter = Moshi.Builder().build().adapter(ApiError::class.java)
    private fun <T> errorReasonHandler(body: Response<T>): Reason {
        val apiError = apiErrorAdapter.fromJson(body.errorBody()?.string())!!
        return when (apiError.error.status) {
            401 -> Reason.UnAuthorized
            404 -> Reason.NotFound
            else -> Reason.ResponseError(apiError.error.message)
        }
    }

    suspend fun getUsersProfile(accessToken: String): UserResult {
        if (accessToken.isEmpty()) return UserResult.Failure(Reason.EmptyAccessToken)

        val request = userService.getUsersProfile(generateBearer(accessToken))
        return try {
            if (request.isSuccessful) UserResult.Success(request.body()!!)
            else UserResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            UserResult.Failure(Reason.UnKnown(e))
        }
    }

    /**
     * Player
     * */
    suspend fun playbackTrack(
        accessToken: String,
        deviceId: String,
        contextUri: String): PlaybackResult {
        if (accessToken.isEmpty()) return PlaybackResult.Failure(Reason.EmptyAccessToken)

        val request = userService.playback(
            accessToken = generateBearer(accessToken),
            deviceId = deviceId,
            body = PlaybackBody(uris = listOf(contextUri)))
        return try {
            if (request.isSuccessful) PlaybackResult.Success
            else PlaybackResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            PlaybackResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun pausePlayback(accessToken: String, deviceId: String): PlaybackResult {
        val request = userService.pausePlayback(
                accessToken = generateBearer(accessToken),
                deviceId = deviceId)
        return try {
            if (request.isSuccessful) PlaybackResult.Success
            else PlaybackResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            PlaybackResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun getUsersDevices(accessToken: String): DevicesResult {
        if (accessToken.isEmpty()) return DevicesResult.Failure(Reason.EmptyAccessToken)

        val request = userService.getUsersDevices(generateBearer(accessToken))
        return try {
            if (request.isSuccessful) DevicesResult.Success(request.body()!!.devices)
            else DevicesResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            DevicesResult.Failure(Reason.UnKnown(e))
        }
    }

    /**
     * Search
     * */

    suspend fun getTracksByKeyword(accessToken: String, keyword: String): TrackItemsResult {
        if (accessToken.isEmpty()) return TrackItemsResult.Failure(Reason.EmptyAccessToken)

        val request = userService.getTracksByKeyword(
            accessToken = generateBearer(accessToken),
            keyword = keyword,
            type = "album,track,artist")
        return try {
            if (request.isSuccessful) TrackItemsResult.Success(request.body()!!.tracks.items)
            else TrackItemsResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            TrackItemsResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun getAudioFeaturesById(accessToken: String, id: String): AudioFeatureResult {
        if (accessToken.isEmpty()) return AudioFeatureResult.Failure(Reason.EmptyAccessToken)
        val request = userService.getAudioFeaturesById(generateBearer(accessToken), id)
        return try {
            if (request.isSuccessful) AudioFeatureResult.Success(request.body()!!)
            else AudioFeatureResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            AudioFeatureResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun getRecommendTracks(
        accessToken: String,
        trackInfo: TrackInfo,
        fetchUpperTrack: Boolean
    ): TrackItemsResult {
        if (accessToken.isEmpty()) return TrackItemsResult.Failure(Reason.EmptyAccessToken)

        //UpperTrackListを返したいならEnergyを1.0~1.2、Downerは0.8~1.0に調整
        var minEnergyRate = RecommendParameter.MinEnergyRate.value //1.0
        var maxEnergyRate = RecommendParameter.MinEnergyRate.value //1.0
        if (fetchUpperTrack) maxEnergyRate *= 1.2
        if (!fetchUpperTrack) minEnergyRate *= 0.8

        val request = userService.getRecommendations(
            accessToken = generateBearer(accessToken),
            seedTrackId = trackInfo.id,
            minTempo = trackInfo.tempo * RecommendParameter.MinTempoRate.value,
            maxTempo = trackInfo.tempo * RecommendParameter.MaxTempoRate.value,
            minDancebility = trackInfo.danceability * RecommendParameter.MinDanceabilityRate.value,
            maxDancebility = trackInfo.danceability * RecommendParameter.MaxDanceabilityRate.value,
            minEnergy = trackInfo.energy * minEnergyRate,
            maxEnergy = trackInfo.energy * maxEnergyRate,)
        return try {
            if (request.isSuccessful) TrackItemsResult.Success(request.body()!!.tracks)
            else TrackItemsResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            TrackItemsResult.Failure(Reason.UnKnown(e))
        }
    }


    /**
     * Playlist
     * */

    suspend fun getUsersAllPlaylist(accessToken: String): PlaylistItemsResult {
        if (accessToken.isEmpty()) return PlaylistItemsResult.Failure(Reason.EmptyAccessToken)
        val request = userService.getUsersAllPlaylists(generateBearer(accessToken))
        return try {
            if (request.isSuccessful) PlaylistItemsResult.Success(request.body()!!.items)
            else PlaylistItemsResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            PlaylistItemsResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun getTracksByPlaylistId(accessToken: String, playlistId: String): TrackItemsResult {
        if (accessToken.isEmpty()) return TrackItemsResult.Failure(Reason.EmptyAccessToken)
        val request = userService.getTracksByPlaylistId(
            accessToken = generateBearer(accessToken),
            playlistId = playlistId)
        return try {
            if (request.isSuccessful) TrackItemsResult.Success(request.body()!!.items.map { it.track })
            else TrackItemsResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            TrackItemsResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun createPlaylist(
        accessToken: String,
        userId: String,
        title: String): CreatePlaylistResult {
        val request = userService.createPlaylist(
            accessToken = generateBearer(accessToken),
            userId = userId,
            body = CreatePlaylistBody(name = title))

        return try {
            if (request.isSuccessful) CreatePlaylistResult.Success(request.body()?.id.toString())
            else CreatePlaylistResult.Failure(errorReasonHandler(request))
        }catch (e: Exception){
            CreatePlaylistResult.Failure(Reason.UnKnown(e))
        }
    }

    //todo bodyは内部で処理したい
    suspend fun addTracksToPlaylist(
        accessToken: String,
        playlistId: String,
        body: AddTracksBody): EditPlaylistResult {
        if (accessToken.isEmpty()) return EditPlaylistResult.Failure(Reason.EmptyAccessToken)
        val request = userService.addTracksToPlaylist(
            accessToken = generateBearer(accessToken),
            contentType = "application/json",
            playlistId = playlistId,
            body = body)
        return try {
            if (request.isSuccessful) EditPlaylistResult.Success
            else EditPlaylistResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            EditPlaylistResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun reorderPlaylistsTracks(
        accessToken: String,
        playlistId: String,
        initialPosition: Int,
        finalPosition: Int
    ): EditPlaylistResult {
        if (accessToken.isEmpty()) return EditPlaylistResult.Failure(Reason.EmptyAccessToken)

        val request = userService.reorderPlaylistsTracks(
            accessToken = generateBearer(accessToken),
            contentType = "application/json",
            playlistId = playlistId,
            body = ReorderBody(
                range_start = initialPosition,
                insert_before = finalPosition
            )
        )
        return try {
            if (request.isSuccessful) EditPlaylistResult.Success
            else EditPlaylistResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            EditPlaylistResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun updatePlaylistTitle(
        accessToken: String,
        playlistId: String,
        playlistTitle: String): EditPlaylistResult{
        //todo これfunでまとめよう
        if (accessToken.isEmpty()) return EditPlaylistResult.Failure(Reason.EmptyAccessToken)

        val request = userService.updatePlaylistTitle(
            accessToken = generateBearer(accessToken),
            contentType = "application/json",
            playlistId = playlistId,
            body = UpdatePlaylistTitleBody(name = playlistTitle))
        return try {
            if (request.isSuccessful) EditPlaylistResult.Success
            else EditPlaylistResult.Failure(errorReasonHandler(request))
        }catch (e: java.lang.Exception){
            EditPlaylistResult.Failure(Reason.UnKnown(e))
        }
    }

    suspend fun deleteTracksFromPlaylist(
        accessToken: String,
        playlistId: String,
        body: DeleteTracksBody
    ): EditPlaylistResult {
        if (accessToken.isEmpty()) return EditPlaylistResult.Failure(Reason.EmptyAccessToken)

        val request = userService.deleteTracksFromPlaylist(
            accessToken = generateBearer(accessToken),
            contentType = "application/json",
            playlistId = playlistId,
            body = body)
        return try {
            if (request.isSuccessful) EditPlaylistResult.Success
            else EditPlaylistResult.Failure(errorReasonHandler(request))
        } catch (e: Exception) {
            EditPlaylistResult.Failure(Reason.UnKnown(e))
        }
    }
}

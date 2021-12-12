package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.*
import com.kabos.spotifydj.model.apiResult.SpotifyApiErrorReason
import com.kabos.spotifydj.model.apiResult.ErrorResponse.Companion.toSpotifyApiErrorResponse
import com.kabos.spotifydj.model.apiResult.SpotifyApiResource
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playback.Device
import com.kabos.spotifydj.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.requestBody.*
import com.kabos.spotifydj.model.track.TrackItems
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor(private val userService: UserService) {

    //@HeaderのaccessTokenは必ず、この関数を通して入力する
    private fun generateBearer(accessToken: String) = "Bearer $accessToken"

    private fun <T> errorReasonHandler(body: Response<T>): SpotifyApiErrorReason {
        val apiError = body.toSpotifyApiErrorResponse() ?: return SpotifyApiErrorReason.UnKnown(null)
        return when (apiError.error.status) {
            401 -> SpotifyApiErrorReason.UnAuthorized
            404 -> SpotifyApiErrorReason.NotFound
            else -> SpotifyApiErrorReason.ResponseError(apiError.error.message)
        }
    }

    suspend fun getUsersProfile(accessToken: String):SpotifyApiResource<User> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        return  try {
            val request = userService.getUsersProfile(generateBearer(accessToken))
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    /**
     * Player
     * */
    suspend fun playbackTrack(
        accessToken: String,
        deviceId: String,
        contextUri: String): SpotifyApiResource<Boolean> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        return try {
            val request = userService.playback(
                accessToken = generateBearer(accessToken),
                deviceId = deviceId,
                body = PlaybackBody(uris = listOf(contextUri)))
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun pausePlayback(accessToken: String, deviceId: String): SpotifyApiResource<Boolean> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        return try {
            val request = userService.pausePlayback(
                accessToken = generateBearer(accessToken),
                deviceId = deviceId)
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun getUsersDevices(accessToken: String): SpotifyApiResource<List<Device>> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        return try {
            val request = userService.getUsersDevices(generateBearer(accessToken))
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!.devices)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    /**
     * Search
     * */
    suspend fun getTracksByKeyword(accessToken: String, keyword: String): SpotifyApiResource<List<TrackItems>> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        return try {
            val request = userService.getTracksByKeyword(
                accessToken = generateBearer(accessToken),
                keyword = keyword,
                type = "album,track,artist")
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!.tracks.items)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun getAudioFeaturesById(accessToken: String, id: String): SpotifyApiResource<AudioFeature> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        return try {
            val request = userService.getAudioFeaturesById(generateBearer(accessToken), id)
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun getRecommendTracks(
        accessToken: String,
        trackInfo: TrackInfo,
        fetchUpperTrack: Boolean
    ): SpotifyApiResource<List<TrackItems>> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

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
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!.tracks)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }


    /**
     * Playlist
     * */

    suspend fun getUsersAllPlaylist(accessToken: String): SpotifyApiResource<List<PlaylistItem>> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        val request = userService.getUsersAllPlaylists(generateBearer(accessToken))
        return try {
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!.items)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun getTracksByPlaylistId(accessToken: String, playlistId: String): SpotifyApiResource<List<TrackItems>> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        val request = userService.getTracksByPlaylistId(
            accessToken = generateBearer(accessToken),
            playlistId = playlistId)
        return try {
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!.items.map { it.track })
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun createPlaylist(
        accessToken: String,
        userId: String,
        title: String): SpotifyApiResource<String> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        return try {
            val request = userService.createPlaylist(
                accessToken = generateBearer(accessToken),
                userId = userId,
                body = CreatePlaylistBody(name = title))
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()?.id.toString())
            else SpotifyApiResource.Error(errorReasonHandler(request))
        }catch (e: Exception){
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    //todo bodyは内部で処理したい
    suspend fun addTracksToPlaylist(
        accessToken: String,
        playlistId: String,
        body: AddTracksBody): SpotifyApiResource<Boolean> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        return try {
            val request = userService.addTracksToPlaylist(
                accessToken = generateBearer(accessToken),
                contentType = "application/json",
                playlistId = playlistId,
                body = body)
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun reorderPlaylistsTracks(
        accessToken: String,
        playlistId: String,
        initialPosition: Int,
        finalPosition: Int
    ): SpotifyApiResource<Boolean> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

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
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun updatePlaylistTitle(
        accessToken: String,
        playlistId: String,
        playlistTitle: String): SpotifyApiResource<Boolean>{
        //todo これfunでまとめよう
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        return try {
            val request = userService.updatePlaylistTitle(
                accessToken = generateBearer(accessToken),
                contentType = "application/json",
                playlistId = playlistId,
                body = UpdatePlaylistTitleBody(name = playlistTitle))
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        }catch (e: java.lang.Exception){
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun deleteTracksFromPlaylist(
        accessToken: String,
        playlistId: String,
        body: DeleteTracksBody
    ): SpotifyApiResource<Boolean> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        val request = userService.deleteTracksFromPlaylist(
            accessToken = generateBearer(accessToken),
            contentType = "application/json",
            playlistId = playlistId,
            body = body)
        return try {
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }
}

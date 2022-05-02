package com.kabos.spotifydj.data.repository

import com.kabos.spotifydj.data.api.SpotifyApi
import com.kabos.spotifydj.data.model.*
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants.Companion.APPLICATION_JSON
import com.kabos.spotifydj.data.model.apiResult.SpotifyApiErrorReason
import com.kabos.spotifydj.data.model.apiResult.ErrorResponse.Companion.toSpotifyApiErrorResponse
import com.kabos.spotifydj.data.model.apiResult.SpotifyApiResource
import com.kabos.spotifydj.data.model.feature.AudioFeature
import com.kabos.spotifydj.data.model.playback.Device
import com.kabos.spotifydj.data.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.data.model.playlist.PlaylistItem
import com.kabos.spotifydj.data.model.requestBody.*
import com.kabos.spotifydj.data.model.track.ArtistX
import com.kabos.spotifydj.data.model.track.Image
import com.kabos.spotifydj.data.model.track.TrackItems
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor(private val spotifyApi: SpotifyApi) {
    companion object {
        private const val SEARCH_TRACK_TYPE = "album,track,artist"
    }

    private fun <T> errorReasonHandler(body: Response<T>): SpotifyApiErrorReason {
        val apiError =
            body.toSpotifyApiErrorResponse() ?: return SpotifyApiErrorReason.UnKnown(null)
        return when (apiError.error.status) {
            401 -> SpotifyApiErrorReason.UnAuthorized
            404 -> SpotifyApiErrorReason.NotFound
            else -> SpotifyApiErrorReason.ResponseError(apiError.error.message)
        }

    }

    suspend fun getUsersProfile(): SpotifyApiResource<User> {

        return try {
            val request = spotifyApi.getUsersProfile()
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
        deviceId: String,
        contextUri: String
    ): SpotifyApiResource<Boolean> {


        return try {
            val request = spotifyApi.playback(
                deviceId = deviceId,
                body = PlaybackBody(uris = listOf(contextUri))
            )
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun pausePlayback(deviceId: String): SpotifyApiResource<Boolean> {

        return try {
            val request = spotifyApi.pausePlayback(
                deviceId = deviceId
            )
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun getUsersDevices(): SpotifyApiResource<List<Device>> {


        return try {
            val request = spotifyApi.getUsersDevices()
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!.devices)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    /**
     * Search
     * */

    suspend fun searchTrackInfo(keyword: String): SpotifyApiResource<List<TrackInfo>> {
        val trackInfos = mutableListOf<TrackInfo>()
        var errorReason: SpotifyApiErrorReason? = null
        coroutineScope {
            val trackItems = async {
                return@async when (val result = getTrackItemsByKeyword(keyword)) {
                    is SpotifyApiResource.Success -> result.data ?: listOf()
                    is SpotifyApiResource.Error -> {
                        errorReason = result.reason
                        listOf()
                    }
                }
            }.await()

            if (errorReason != null) return@coroutineScope

            async {
                trackItems.forEach { trackItem ->
                    when (val result = getAudioFeaturesById(trackItem.id)) {
                        is SpotifyApiResource.Success -> {
                            val audioFeature: AudioFeature = result.data ?: return@forEach
                            val trackInfo: TrackInfo = generateTrackInfo(trackItem, audioFeature)
                            trackInfos.add(trackInfo)
                        }
                    }
                }
            }.await()
        }

        return if (errorReason != null) SpotifyApiResource.Error(errorReason!!)
        else SpotifyApiResource.Success(trackInfos)
    }


    private fun generateTrackInfo(trackItems: TrackItems, audioFeature: AudioFeature): TrackInfo {
        val artists: List<ArtistX> = trackItems.artists
        val artistName: String = if (artists.isNotEmpty()) artists.first().name else ""
        val albumImages: List<Image> = trackItems.album.images
        val imageUrl: String = if (albumImages.isNotEmpty()) albumImages.first().url else ""
        return TrackInfo(
            id = trackItems.id,
            contextUri = audioFeature.uri,
            name = trackItems.name,
            artist = artistName,
            imageUrl = imageUrl,
            tempo = audioFeature.tempo,
            danceability = audioFeature.danceability,
            energy = audioFeature.energy
        )
    }

    private suspend fun getTrackItemsByKeyword(
        keyword: String
    ): SpotifyApiResource<List<TrackItems>> {


        return try {
            val request = spotifyApi.getTracksByKeyword(
                keyword = keyword,
                type = SEARCH_TRACK_TYPE
            )
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!.tracks.items)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    private suspend fun getAudioFeaturesById(
        id: String
    ): SpotifyApiResource<AudioFeature> {

        return try {
            val request = spotifyApi.getAudioFeaturesById(id)
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun getRecommendTrackInfos(
        trackInfo: TrackInfo,
        fetchUpperTrack: Boolean
    ): SpotifyApiResource<List<TrackInfo>> {
        val trackInfos = mutableListOf<TrackInfo>()
        var errorReason: SpotifyApiErrorReason? = null
        coroutineScope {
            val trackItems = async {
                return@async when (val result =
                    getRecommendTrackItems(trackInfo, fetchUpperTrack)) {
                    is SpotifyApiResource.Success -> result.data ?: listOf()
                    is SpotifyApiResource.Error -> {
                        errorReason = result.reason
                        listOf()
                    }
                }
            }.await()

            if (errorReason != null) return@coroutineScope

            async {
                trackItems.forEach { trackItem ->
                    when (val result = getAudioFeaturesById(trackItem.id)) {
                        is SpotifyApiResource.Success -> {
                            val audioFeature: AudioFeature = result.data ?: return@forEach
                            val trackInfo: TrackInfo = generateTrackInfo(trackItem, audioFeature)
                            trackInfos.add(trackInfo)
                        }
                    }
                }
            }.await()
        }

        return if (errorReason != null) SpotifyApiResource.Error(errorReason!!)
        else SpotifyApiResource.Success(trackInfos)
    }

    private suspend fun getRecommendTrackItems(
        trackInfo: TrackInfo,
        fetchUpperTrack: Boolean
    ): SpotifyApiResource<List<TrackItems>> {


        //UpperTrackListを返したいならEnergyを1.0~1.2、Downerは0.8~1.0に調整
        var minEnergyRate = RecommendParameter.MinEnergyRate.value //1.0
        var maxEnergyRate = RecommendParameter.MinEnergyRate.value //1.0
        if (fetchUpperTrack) maxEnergyRate *= 1.2
        if (!fetchUpperTrack) minEnergyRate *= 0.8

        val request = spotifyApi.getRecommendations(
            seedTrackId = trackInfo.id,
            minTempo = trackInfo.tempo * RecommendParameter.MinTempoRate.value,
            maxTempo = trackInfo.tempo * RecommendParameter.MaxTempoRate.value,
            minDancebility = trackInfo.danceability * RecommendParameter.MinDanceabilityRate.value,
            maxDancebility = trackInfo.danceability * RecommendParameter.MaxDanceabilityRate.value,
            minEnergy = trackInfo.energy * minEnergyRate,
            maxEnergy = trackInfo.energy * maxEnergyRate,
        )
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

    suspend fun getUsersPlaylist(): SpotifyApiResource<List<PlaylistItem>> {

        return try {
            val request = spotifyApi.getUsersAllPlaylists()
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()?.items ?: listOf())
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    // todo trackItemsの関数だけ変更すれば使いまわせる
    suspend fun getTrackInfosByPlaylistId(
        playlistId: String
    ): SpotifyApiResource<List<TrackInfo>> {
        val trackInfos = mutableListOf<TrackInfo>()
        var errorReason: SpotifyApiErrorReason? = null
        coroutineScope {
            val trackItems = async {
                return@async when (val result = getTrackItemsByPlaylistId(playlistId)) {
                    is SpotifyApiResource.Success -> result.data ?: listOf()
                    is SpotifyApiResource.Error -> {
                        errorReason = result.reason
                        listOf()
                    }
                }
            }.await()

            if (errorReason != null) return@coroutineScope

            async {
                trackItems.forEach { trackItem ->
                    when (val result = getAudioFeaturesById(trackItem.id)) {
                        is SpotifyApiResource.Success -> {
                            val audioFeature: AudioFeature = result.data ?: return@forEach
                            val trackInfo: TrackInfo = generateTrackInfo(trackItem, audioFeature)
                            trackInfos.add(trackInfo)
                        }
                    }
                }
            }.await()
        }

        return if (errorReason != null) SpotifyApiResource.Error(errorReason!!)
        else SpotifyApiResource.Success(trackInfos)
    }

    private suspend fun getTrackItemsByPlaylistId(
        playlistId: String
    ): SpotifyApiResource<List<TrackItems>> {

        val request = spotifyApi.getTracksByPlaylistId(
            playlistId = playlistId
        )
        return try {
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!.items.map { it.track })
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun createPlaylist(
        userId: String,
        title: String
    ): SpotifyApiResource<String> {

        return try {
            val request = spotifyApi.createPlaylist(
                userId = userId,
                body = CreatePlaylistBody(name = title)
            )
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()?.id.toString())
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun addTracksToPlaylist(
        playlistId: String,
        trackUris: List<String>
    ): SpotifyApiResource<Boolean> {

        return try {
            val request = spotifyApi.addTracksToPlaylist(
                contentType = APPLICATION_JSON,
                playlistId = playlistId,
                body = AddTracksBody(trackUris)
            )
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun reorderPlaylistsTracks(
        playlistId: String,
        initialPosition: Int,
        finalPosition: Int
    ): SpotifyApiResource<Boolean> {


        val request = spotifyApi.reorderPlaylistsTracks(
            contentType = APPLICATION_JSON,
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
        playlistId: String,
        playlistTitle: String
    ): SpotifyApiResource<Boolean> {


        return try {
            val request = spotifyApi.updatePlaylistTitle(
                contentType = APPLICATION_JSON,
                playlistId = playlistId,
                body = UpdatePlaylistTitleBody(name = playlistTitle)
            )
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: java.lang.Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun deleteTracksFromPlaylist(
        playlistId: String,
        trackUri: String
    ): SpotifyApiResource<Boolean> {


        val request = spotifyApi.deleteTracksFromPlaylist(
            contentType = APPLICATION_JSON,
            playlistId = playlistId,
            body = DeleteTracksBody(listOf(DeleteTrack(trackUri)))
        )
        return try {
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }
}

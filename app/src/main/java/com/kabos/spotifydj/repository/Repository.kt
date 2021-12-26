package com.kabos.spotifydj.repository

import com.bumptech.glide.load.resource.file.FileResource
import com.kabos.spotifydj.model.*
import com.kabos.spotifydj.model.apiConstants.ApiConstants.Companion.APPLICATION_JSON
import com.kabos.spotifydj.model.apiResult.SpotifyApiErrorReason
import com.kabos.spotifydj.model.apiResult.ErrorResponse.Companion.toSpotifyApiErrorResponse
import com.kabos.spotifydj.model.apiResult.SpotifyApiResource
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playback.Device
import com.kabos.spotifydj.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.requestBody.*
import com.kabos.spotifydj.model.track.ArtistX
import com.kabos.spotifydj.model.track.Image
import com.kabos.spotifydj.model.track.TrackItems
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class Repository @Inject constructor(private val spotifyApi: SpotifyApi) {
    companion object {
        private const val SEARCH_TRACK_TYPE = "album,track,artist"
    }

    //@HeaderのaccessTokenは必ず、この関数を通して入力する
    private fun generateBearer(accessToken: String) = "Bearer $accessToken"

    private fun <T> errorReasonHandler(body: Response<T>): SpotifyApiErrorReason {
        val apiError =
            body.toSpotifyApiErrorResponse() ?: return SpotifyApiErrorReason.UnKnown(null)
        return when (apiError.error.status) {
            401 -> SpotifyApiErrorReason.UnAuthorized
            404 -> SpotifyApiErrorReason.NotFound
            else -> SpotifyApiErrorReason.ResponseError(apiError.error.message)
        }

    }

    suspend fun getUsersProfile(accessToken: String): SpotifyApiResource<User> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        return try {
            val request = spotifyApi.getUsersProfile(generateBearer(accessToken))
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
        contextUri: String
    ): SpotifyApiResource<Boolean> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        return try {
            val request = spotifyApi.playback(
                accessToken = generateBearer(accessToken),
                deviceId = deviceId,
                body = PlaybackBody(uris = listOf(contextUri))
            )
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun pausePlayback(accessToken: String, deviceId: String): SpotifyApiResource<Boolean> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        return try {
            val request = spotifyApi.pausePlayback(
                accessToken = generateBearer(accessToken),
                deviceId = deviceId
            )
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun getUsersDevices(accessToken: String): SpotifyApiResource<List<Device>> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        return try {
            val request = spotifyApi.getUsersDevices(generateBearer(accessToken))
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!.devices)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    /**
     * Search
     * */

    suspend fun searchTrackInfo(accessToken: String, keyword: String): SpotifyApiResource<List<TrackInfo>> {
        val trackInfos = mutableListOf<TrackInfo>()
        var errorReason: SpotifyApiErrorReason? = null
        coroutineScope {
            val trackItems = async {
                return@async when(val result = getTrackItemsByKeyword(accessToken, keyword)) {
                    is SpotifyApiResource.Success -> result.data ?: listOf()
                    is SpotifyApiResource.Error -> {
                        errorReason = result.reason
                        listOf()
                    }
                }
            }.await()

            if (errorReason != null) return@coroutineScope

            async {
                trackItems.forEach{ trackItem ->
                    when (val result = getAudioFeaturesById(accessToken, trackItem.id)) {
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
        accessToken: String,
        keyword: String
    ): SpotifyApiResource<List<TrackItems>> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        return try {
            val request = spotifyApi.getTracksByKeyword(
                accessToken = generateBearer(accessToken),
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
        accessToken: String,
        id: String
    ): SpotifyApiResource<AudioFeature> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        return try {
            val request = spotifyApi.getAudioFeaturesById(generateBearer(accessToken), id)
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()!!)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    suspend fun getRecommendTrackInfos(
        accessToken: String,
        trackInfo: TrackInfo,
        fetchUpperTrack: Boolean
    ): SpotifyApiResource<List<TrackInfo>> {
        val trackInfos = mutableListOf<TrackInfo>()
        var errorReason: SpotifyApiErrorReason? = null
        coroutineScope {
            val trackItems = async {
                return@async when (val result = getRecommendTrackItems(accessToken, trackInfo, fetchUpperTrack)) {
                    is SpotifyApiResource.Success -> result.data ?: listOf()
                    is SpotifyApiResource.Error -> {
                        errorReason = result.reason
                        listOf()
                    }
                }
            }.await()

            if (errorReason != null) return@coroutineScope

            async {
                trackItems.forEach{ trackItem ->
                    when (val result = getAudioFeaturesById(accessToken, trackItem.id)) {
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

        val request = spotifyApi.getRecommendations(
            accessToken = generateBearer(accessToken),
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

    suspend fun getUsersAllPlaylist(accessToken: String): SpotifyApiResource<List<PlaylistItem>> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        return try {
            val request = spotifyApi.getUsersAllPlaylists(generateBearer(accessToken))
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()?.items ?: listOf())
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }
    // todo trackItemsの関数だけ変更すれば使いまわせる
    suspend fun getTrackInfosByPlaylistId(
        accessToken: String,
        playlistId: String
    ): SpotifyApiResource<List<TrackInfo>> {
        val trackInfos = mutableListOf<TrackInfo>()
        var errorReason: SpotifyApiErrorReason? = null
        coroutineScope {
            val trackItems = async {
                return@async when (val result = getTrackItemsByPlaylistId(accessToken, playlistId)) {
                    is SpotifyApiResource.Success -> result.data ?: listOf()
                    is SpotifyApiResource.Error -> {
                        errorReason = result.reason
                        listOf()
                    }
                }
            }.await()

            if (errorReason != null) return@coroutineScope

            async {
                trackItems.forEach{ trackItem ->
                    when (val result = getAudioFeaturesById(accessToken, trackItem.id)) {
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
        accessToken: String,
        playlistId: String
    ): SpotifyApiResource<List<TrackItems>> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        val request = spotifyApi.getTracksByPlaylistId(
            accessToken = generateBearer(accessToken),
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
        accessToken: String,
        userId: String,
        title: String
    ): SpotifyApiResource<String> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        return try {
            val request = spotifyApi.createPlaylist(
                accessToken = generateBearer(accessToken),
                userId = userId,
                body = CreatePlaylistBody(name = title)
            )
            if (request.isSuccessful) SpotifyApiResource.Success(request.body()?.id.toString())
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }

    //todo bodyは内部で処理したい
    suspend fun addTracksToPlaylist(
        accessToken: String,
        playlistId: String,
        body: AddTracksBody
    ): SpotifyApiResource<Boolean> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)
        return try {
            val request = spotifyApi.addTracksToPlaylist(
                accessToken = generateBearer(accessToken),
                contentType = APPLICATION_JSON,
                playlistId = playlistId,
                body = body
            )
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

        val request = spotifyApi.reorderPlaylistsTracks(
            accessToken = generateBearer(accessToken),
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
        accessToken: String,
        playlistId: String,
        playlistTitle: String
    ): SpotifyApiResource<Boolean> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        return try {
            val request = spotifyApi.updatePlaylistTitle(
                accessToken = generateBearer(accessToken),
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
        accessToken: String,
        playlistId: String,
        body: DeleteTracksBody
    ): SpotifyApiResource<Boolean> {
        if (accessToken.isEmpty()) return SpotifyApiResource.Error(SpotifyApiErrorReason.EmptyAccessToken)

        val request = spotifyApi.deleteTracksFromPlaylist(
            accessToken = generateBearer(accessToken),
            contentType = APPLICATION_JSON,
            playlistId = playlistId,
            body = body
        )
        return try {
            if (request.isSuccessful) SpotifyApiResource.Success(true)
            else SpotifyApiResource.Error(errorReasonHandler(request))
        } catch (e: Exception) {
            SpotifyApiResource.Error(SpotifyApiErrorReason.UnKnown(e))
        }
    }
}

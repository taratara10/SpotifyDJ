package com.kabos.spotifydj.data.repository

import com.kabos.spotifydj.data.api.PlaylistApi
import com.kabos.spotifydj.data.api.TrackApi
import com.kabos.spotifydj.data.model.RecommendParameter
import com.kabos.spotifydj.data.model.TrackInfo
import com.kabos.spotifydj.data.model.feature.AudioFeature
import com.kabos.spotifydj.data.model.track.ArtistX
import com.kabos.spotifydj.data.model.track.Image
import com.kabos.spotifydj.data.model.track.TrackItems
import com.kabos.spotifydj.util.errorHandling
import javax.inject.Inject

class TrackRepository @Inject constructor(
    private val trackApi: TrackApi,
    private val playlistApi: PlaylistApi
) {
    companion object {
        private const val SEARCH_TRACK_TYPE = "album,track,artist"
    }

    suspend fun searchTrackInfo(keyword: String): List<TrackInfo> {
        val trackItems = trackApi.getTracksByKeyword(keyword, SEARCH_TRACK_TYPE)
            .errorHandling().tracks.items
        return generateTrackInfo(trackItems)
    }

    suspend fun getRecommendTrackInfos(
        trackInfo: TrackInfo,
        fetchUpperTrack: Boolean
    ): List<TrackInfo> {
        val trackItems = getRecommendTrackItems(trackInfo, fetchUpperTrack)
        return generateTrackInfo(trackItems)
    }

    suspend fun getTrackInfosByPlaylistId(playlistId: String): List<TrackInfo> {
        val playlists = playlistApi.getTracksByPlaylistId(playlistId)
            .errorHandling().items.map { it.track }
        return generateTrackInfo(playlists)
    }

    private suspend fun generateTrackInfo(trackItems: List<TrackItems>): List<TrackInfo> {
        return trackItems.map { trackItem ->
            val feature = trackApi.getAudioFeaturesById(trackItem.id).errorHandling()
            mergeTrackItemsAndAudioFeature(trackItem, feature)
        }
    }

    private fun mergeTrackItemsAndAudioFeature(
        trackItems: TrackItems,
        audioFeature: AudioFeature
    ): TrackInfo {
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

    private suspend fun getRecommendTrackItems(
        trackInfo: TrackInfo,
        fetchUpperTrack: Boolean
    ): List<TrackItems> {
        //UpperTrackListを返したいならEnergyを1.0~1.2、Downerは0.8~1.0に調整
        var minEnergyRate = RecommendParameter.MinEnergyRate.value //1.0
        var maxEnergyRate = RecommendParameter.MinEnergyRate.value //1.0
        if (fetchUpperTrack) maxEnergyRate *= 1.2
        if (!fetchUpperTrack) minEnergyRate *= 0.8

        return trackApi.getRecommendations(
            seedTrackId = trackInfo.id,
            minTempo = trackInfo.tempo * RecommendParameter.MinTempoRate.value,
            maxTempo = trackInfo.tempo * RecommendParameter.MaxTempoRate.value,
            minDancebility = trackInfo.danceability * RecommendParameter.MinDanceabilityRate.value,
            maxDancebility = trackInfo.danceability * RecommendParameter.MaxDanceabilityRate.value,
            minEnergy = trackInfo.energy * minEnergyRate,
            maxEnergy = trackInfo.energy * maxEnergyRate,
        ).errorHandling().tracks
    }

}

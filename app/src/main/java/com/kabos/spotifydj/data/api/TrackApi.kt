package com.kabos.spotifydj.data.api

import com.kabos.spotifydj.data.model.RecommendTracks
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants
import com.kabos.spotifydj.data.model.feature.AudioFeature
import com.kabos.spotifydj.data.model.track.SearchTracks
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TrackApi {

    @GET("search")
    suspend fun getTracksByKeyword(
        @Query("q") keyword: String,
        @Query(ApiConstants.TYPE) type: String
    ): Response<SearchTracks>

    @GET("audio-features/{${ApiConstants.ID}}")
    suspend fun getAudioFeaturesById(
        @Path(ApiConstants.ID) id: String
    ): Response<AudioFeature>

    @GET("recommendations")
    suspend fun getRecommendations(
        @Query("seed_tracks") seedTrackId: String,
        @Query("min_tempo") minTempo: Double,
        @Query("max_tempo") maxTempo: Double,
        @Query("min_danceability") minDancebility: Double,
        @Query("max_danceability") maxDancebility: Double,
        @Query("min_energy") minEnergy: Double,
        @Query("max_energy") maxEnergy: Double,
    ): Response<RecommendTracks>

}

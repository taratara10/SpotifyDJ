package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.*
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.feature.AudioFeatures
import com.kabos.spotifydj.model.track.SearchTracks
import retrofit2.Response
import retrofit2.http.*

interface UserService {
    @GET("me")
    suspend fun getUser(@Header("Authorization") accessToken: String):Response<User>

    @GET("me/playlists")
    suspend fun getCurrentPlaylist(@Header("Authorization")accessToken: String): Response<Playlist>

    @GET("me/player/recently-played")
    suspend fun getRecentlyPlayed(@Header("Authorization")accessToken: String): Response<RecentlyPlaylist>

    @GET("me/player/devices")
    suspend fun getCurrentPlayback(@Header("Authorization")accessToken: String): Response<Devices>

    @PUT("me/player/play")
    suspend fun playback(@Header("Authorization")accessToken: String,
                         @Query("device_id")id: String,
                         @Body body:Playback
    )


    @GET("search")
    suspend fun getTracksByKeyword(
        @Header("Authorization")accessToken: String,
        @Query("q")keyword: String,
        @Query("type")type: String
    ):Response<SearchTracks>

    @GET("audio-features/{id}")
    suspend fun getAudioFeaturesById(
        @Header("Authorization")accessToken: String,
        @Path("id")id: String
    ): Response<AudioFeature>

    @GET("recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") accessToken: String,
        @Query("seed_tracks")seedTrackId: String,
        @Query("min_tempo") minTempo: Double,
        @Query("max_tempo") maxTempo: Double,
        @Query("min_danceability") minDancebility: Double,
        @Query("max_danceability") maxDancebility: Double,
        @Query("min_energy") minEnergy: Double,
        @Query("max_energy") maxEnergy: Double,
    ):Response<RecommendTracks>

    @POST("users/{user_id}/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") accessToken: String,
        @Path("user_id")userId: String,
        @Query("name")title: String
    )
}

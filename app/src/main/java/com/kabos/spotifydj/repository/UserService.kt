package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.*
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playlist.AddItemToPlaylistBody
import com.kabos.spotifydj.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.model.playlist.Playlist
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.track.SearchTracks
import retrofit2.Response
import retrofit2.http.*

interface UserService {
    @GET("me")
    suspend fun getUsersProfile(@Header("Authorization") accessToken: String):Response<User>


//
//    @GET("me/player/recently-played")
//    suspend fun getRecentlyPlayed(@Header("Authorization")accessToken: String): Response<RecentlyPlaylist>
//
//    @GET("me/player/devices")
//    suspend fun getCurrentPlayback(@Header("Authorization")accessToken: String): Response<Devices>

    @PUT("me/player/play")
    suspend fun playback(
        @Header("Authorization")accessToken: String,
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

    /**
     *  playlist
     * */

    @GET("me/playlists")
    suspend fun getUsersAllPlaylists(@Header("Authorization")accessToken: String): Response<Playlist>

    @GET("playlists/{playlist_id}/tracks")
    suspend fun getPlaylistItem(
        @Header("Authorization") accessToken: String,
        @Path("playlist_id")playlistId: String,
    )

    @POST("users/{user_id}/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") accessToken: String,
        @Path("user_id")userId: String,
        @Body body: CreatePlaylistBody
    ):Response<PlaylistItem>

    @POST("playlists/{playlist_id}/tracks")
    suspend fun addItemsToPlaylist(
        @Header("Authorization") accessToken: String,
        @Header("Content-Type") contentType: String,
        @Path("playlist_id")playlistId: String,
        @Body body: AddItemToPlaylistBody
    )

    @DELETE("playlists/{playlist_id}/tracks")
    suspend fun deleteItemsFromPlaylist(
        @Header("Authorization") accessToken: String,
        @Header("Content-Type") contentType: String,
        @Path("playlist_id")playlistId: String,
        @Path("tracks")tracks: Array<String>
    )
}

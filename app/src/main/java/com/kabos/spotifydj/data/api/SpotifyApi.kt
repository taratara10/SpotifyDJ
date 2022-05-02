package com.kabos.spotifydj.repository

import com.kabos.spotifydj.data.model.*
import com.kabos.spotifydj.data.model.PlaylistById.PlaylistById
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants.Companion.AUTHORIZATION
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants.Companion.CONTENT_TYPE
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants.Companion.DEVICE_ID
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants.Companion.ID
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants.Companion.PLAYLIST_ID
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants.Companion.TYPE
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants.Companion.USER_ID
import com.kabos.spotifydj.data.model.feature.AudioFeature
import com.kabos.spotifydj.data.model.playback.Devices
import com.kabos.spotifydj.data.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.data.model.playlist.Playlist
import com.kabos.spotifydj.data.model.playlist.PlaylistItem
import com.kabos.spotifydj.data.model.requestBody.*
import com.kabos.spotifydj.data.model.track.SearchTracks
import okhttp3.internal.http.StatusLine
import retrofit2.Response
import retrofit2.http.*

interface SpotifyApi {
    @GET("me")
    suspend fun getUsersProfile(@Header(AUTHORIZATION) accessToken: String): Response<User>


    /**
     * Player
     * */
    @GET("me/player/devices")
    suspend fun getUsersDevices(
        @Header(AUTHORIZATION) accessToken: String
    ): Response<Devices>

    @PUT("me/player/play")
    suspend fun playback(
        @Header(AUTHORIZATION) accessToken: String,
        @Query(DEVICE_ID) deviceId: String,
        @Body body: PlaybackBody
    ): Response<Unit>

    @PUT("me/player/pause")
    suspend fun pausePlayback(
        @Header(AUTHORIZATION) accessToken: String,
        @Query(DEVICE_ID) deviceId: String,
    ): Response<Unit>


    /**
     *  Search
     * */
    @GET("search")
    suspend fun getTracksByKeyword(
        @Header(AUTHORIZATION) accessToken: String,
        @Query("q") keyword: String,
        @Query(TYPE) type: String
    ): Response<SearchTracks>

    @GET("audio-features/{$ID}")
    suspend fun getAudioFeaturesById(
        @Header(AUTHORIZATION) accessToken: String,
        @Path(ID) id: String
    ): Response<AudioFeature>

    @GET("recommendations")
    suspend fun getRecommendations(
        @Header(AUTHORIZATION) accessToken: String,
        @Query("seed_tracks") seedTrackId: String,
        @Query("min_tempo") minTempo: Double,
        @Query("max_tempo") maxTempo: Double,
        @Query("min_danceability") minDancebility: Double,
        @Query("max_danceability") maxDancebility: Double,
        @Query("min_energy") minEnergy: Double,
        @Query("max_energy") maxEnergy: Double,
    ): Response<RecommendTracks>


    /**
     *  playlist
     * */
    @GET("me/playlists")
    suspend fun getUsersAllPlaylists(@Header(AUTHORIZATION) accessToken: String): Response<Playlist>

    @GET("playlists/{$PLAYLIST_ID}/tracks")
    suspend fun getTracksByPlaylistId(
        @Header(AUTHORIZATION) accessToken: String,
        @Path(PLAYLIST_ID) playlistId: String,
    ): Response<PlaylistById>

    @POST("users/{$USER_ID}/playlists")
    suspend fun createPlaylist(
        @Header(AUTHORIZATION) accessToken: String,
        @Path(USER_ID) userId: String,
        @Body body: CreatePlaylistBody
    ): Response<PlaylistItem>

    @POST("playlists/{$PLAYLIST_ID}/tracks")
    suspend fun addTracksToPlaylist(
        @Header(AUTHORIZATION) accessToken: String,
        @Header(CONTENT_TYPE) contentType: String,
        @Path(PLAYLIST_ID) playlistId: String,
        @Body body: AddTracksBody
    ): Response<SnapshotId>

    @PUT("playlists/{$PLAYLIST_ID}")
    suspend fun updatePlaylistTitle(
        @Header(AUTHORIZATION) accessToken: String,
        @Header(CONTENT_TYPE) contentType: String,
        @Path(PLAYLIST_ID) playlistId: String,
        @Body body: UpdatePlaylistTitleBody
    ): Response<Unit>

    @PUT("playlists/{$PLAYLIST_ID}/tracks")
    suspend fun reorderPlaylistsTracks(
        @Header(AUTHORIZATION) accessToken: String,
        @Header(CONTENT_TYPE) contentType: String,
        @Path(PLAYLIST_ID) playlistId: String,
        @Body body: ReorderBody
    ): Response<SnapshotId>

    @HTTP(method = "DELETE", path = "playlists/{$PLAYLIST_ID}/tracks", hasBody = true)
    suspend fun deleteTracksFromPlaylist(
        @Header(AUTHORIZATION) accessToken: String,
        @Header(CONTENT_TYPE) contentType: String,
        @Path(PLAYLIST_ID) playlistId: String,
        @Body body: DeleteTracksBody
    ): Response<SnapshotId>
}

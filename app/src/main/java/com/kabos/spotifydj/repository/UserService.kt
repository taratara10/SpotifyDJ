package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.*
import com.kabos.spotifydj.model.PlaylistById.PlaylistById
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playback.Devices
import com.kabos.spotifydj.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.model.playlist.Playlist
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.requestBody.*
import com.kabos.spotifydj.model.track.SearchTracks
import okhttp3.internal.http.StatusLine
import retrofit2.Response
import retrofit2.http.*

interface UserService {
    @GET("me")
    suspend fun getUsersProfile(@Header("Authorization") accessToken: String):Response<User>


    /**
     * Player
     * */
    @GET("me/player/devices")
    suspend fun getUsersDevices (
        @Header("Authorization")accessToken: String
    ): Response<Devices>

    @PUT("me/player/play")
    suspend fun playback(
        @Header("Authorization")accessToken: String,
        @Query("device_id")deviceId: String,
        @Body body: PlaybackBody
    ): Response<Unit>

    @PUT("me/player/pause")
    suspend fun pausePlayback(
        @Header("Authorization")accessToken: String,
        @Query("device_id")deviceId: String,
    ):Response<Unit>


    /**
     *  Search
     * */
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
    suspend fun getTracksByPlaylistId(
        @Header("Authorization") accessToken: String,
        @Path("playlist_id")playlistId: String,
    ):Response<PlaylistById>

    @POST("users/{user_id}/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") accessToken: String,
        @Path("user_id")userId: String,
        @Body body: CreatePlaylistBody
    ):Response<PlaylistItem>

    @POST("playlists/{playlist_id}/tracks")
    suspend fun addTracksToPlaylist(
        @Header("Authorization") accessToken: String,
        @Header("Content-Type") contentType: String,
        @Path("playlist_id")playlistId: String,
        @Body body: AddTracksBody
    ):Response<SnapshotId>

    @PUT("playlists/{playlist_id}")
    suspend fun updatePlaylistTitle(
        @Header("Authorization") accessToken: String,
        @Header("Content-Type") contentType: String,
        @Path("playlist_id")playlistId: String,
        @Body body: UpdatePlaylistTitleBody
    ):Response<Unit>

    @PUT("playlists/{playlist_id}/tracks")
    suspend fun reorderPlaylistsTracks(
        @Header("Authorization") accessToken: String,
        @Header("Content-Type") contentType: String,
        @Path("playlist_id") playlistId: String,
        @Body body: ReorderBody
    ):Response<SnapshotId>

    @HTTP(method = "DELETE", path = "playlists/{playlist_id}/tracks", hasBody = true)
    suspend fun deleteTracksFromPlaylist(
        @Header("Authorization") accessToken: String,
        @Header("Content-Type") contentType: String,
        @Path("playlist_id")playlistId: String,
        @Body body: DeleteTracksBody
    ):Response<SnapshotId>
}

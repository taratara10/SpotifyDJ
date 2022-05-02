package com.kabos.spotifydj.data.api

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

interface PlaylistApi {

    @GET("me/playlists")
    suspend fun getUsersAllPlaylists(): Response<Playlist>

    @GET("playlists/{$PLAYLIST_ID}/tracks")
    suspend fun getTracksByPlaylistId(
        @Path(PLAYLIST_ID) playlistId: String,
    ): Response<PlaylistById>

    @POST("users/{$USER_ID}/playlists")
    suspend fun createPlaylist(
        @Path(USER_ID) userId: String,
        @Body body: CreatePlaylistBody
    ): Response<PlaylistItem>

    @POST("playlists/{$PLAYLIST_ID}/tracks")
    suspend fun addTracksToPlaylist(
        @Header(CONTENT_TYPE) contentType: String,
        @Path(PLAYLIST_ID) playlistId: String,
        @Body body: AddTracksBody
    ): Response<SnapshotId>

    @PUT("playlists/{$PLAYLIST_ID}")
    suspend fun updatePlaylistTitle(
        @Header(CONTENT_TYPE) contentType: String,
        @Path(PLAYLIST_ID) playlistId: String,
        @Body body: UpdatePlaylistTitleBody
    ): Response<Unit>

    @PUT("playlists/{$PLAYLIST_ID}/tracks")
    suspend fun reorderPlaylistsTracks(
        @Header(CONTENT_TYPE) contentType: String,
        @Path(PLAYLIST_ID) playlistId: String,
        @Body body: ReorderBody
    ): Response<SnapshotId>

    @HTTP(method = "DELETE", path = "playlists/{$PLAYLIST_ID}/tracks", hasBody = true)
    suspend fun deleteTracksFromPlaylist(
        @Header(CONTENT_TYPE) contentType: String,
        @Path(PLAYLIST_ID) playlistId: String,
        @Body body: DeleteTracksBody
    ): Response<SnapshotId>
}

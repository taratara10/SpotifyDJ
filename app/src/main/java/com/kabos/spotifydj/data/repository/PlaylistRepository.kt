package com.kabos.spotifydj.data.repository

import com.kabos.spotifydj.data.api.PlaylistApi
import com.kabos.spotifydj.data.model.TrackInfo
import com.kabos.spotifydj.data.model.User
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants
import com.kabos.spotifydj.data.model.feature.AudioFeature
import com.kabos.spotifydj.data.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.data.model.playlist.PlaylistItem
import com.kabos.spotifydj.data.model.requestBody.*
import com.kabos.spotifydj.data.model.track.TrackItems
import com.kabos.spotifydj.util.errorHandling
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class PlaylistRepository @Inject constructor(private val playlistApi: PlaylistApi) {


    suspend fun getUsersPlaylist(): List<PlaylistItem> {
        return playlistApi.getUsersAllPlaylists().errorHandling().items
    }

    private suspend fun getTrackItemsByPlaylistId(playlistId: String): List<TrackItems> {
        return playlistApi.getTracksByPlaylistId(playlistId).errorHandling().items.map { it.track }
    }

    suspend fun createPlaylist(
        userId: String,
        title: String
    ): String {

        return playlistApi.createPlaylist(
            userId = userId,
            body = CreatePlaylistBody(name = title)
        ).errorHandling().id

    }

    suspend fun addTracksToPlaylist(
        playlistId: String,
        trackUris: List<String>
    ): Boolean {

        playlistApi.addTracksToPlaylist(
            contentType = ApiConstants.APPLICATION_JSON,
            playlistId = playlistId,
            body = AddTracksBody(trackUris)
        ).errorHandling()
        return true
    }

    suspend fun reorderPlaylistsTracks(
        playlistId: String,
        initialPosition: Int,
        finalPosition: Int
    ): Boolean {
        playlistApi.reorderPlaylistsTracks(
            contentType = ApiConstants.APPLICATION_JSON,
            playlistId = playlistId,
            body = ReorderBody(
                range_start = initialPosition,
                insert_before = finalPosition
            )
        )
        return true

    }

    suspend fun updatePlaylistTitle(
        playlistId: String,
        playlistTitle: String
    ): Boolean {

        playlistApi.updatePlaylistTitle(
            contentType = ApiConstants.APPLICATION_JSON,
            playlistId = playlistId,
            body = UpdatePlaylistTitleBody(name = playlistTitle)
        )

        return true
    }

    suspend fun deleteTracksFromPlaylist(
        playlistId: String,
        trackUri: String
    ) {
        playlistApi.deleteTracksFromPlaylist(
            contentType = ApiConstants.APPLICATION_JSON,
            playlistId = playlistId,
            body = DeleteTracksBody(listOf(DeleteTrack(trackUri)))
        )
    }
}

package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.PlaylistById.PlaylistById
import com.kabos.spotifydj.model.RecommendTracks
import com.kabos.spotifydj.model.User
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playback.Devices
import com.kabos.spotifydj.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.model.playlist.Playlist
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.requestBody.AddTracksBody
import com.kabos.spotifydj.model.requestBody.DeleteTracksBody
import com.kabos.spotifydj.model.requestBody.PlaybackBody
import com.kabos.spotifydj.model.track.SearchTracks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.lang.IllegalArgumentException

class RepositoryTest {

    class UserServiceMock: UserService {
        var userResponse: Response<User>? = null
        override suspend fun getUsersProfile(accessToken: String): Response<User>
        = userResponse ?: throw IllegalArgumentException("UserResponse is null")

        var devicesResponse: Response<Devices>? = null
        override suspend fun getUsersDevices(accessToken: String): Response<Devices>
        = devicesResponse ?: throw IllegalArgumentException("DevicesResponse is null")

        override suspend fun playback(accessToken: String, deviceId: String, body: PlaybackBody) {
            TODO("Not yet implemented")
        }

        override suspend fun pausePlayback(accessToken: String, deviceId: String) {
            TODO("Not yet implemented")
        }

        override suspend fun getTracksByKeyword(
            accessToken: String,
            keyword: String,
            type: String
        ): Response<SearchTracks> {
            TODO("Not yet implemented")
        }

        override suspend fun getAudioFeaturesById(
            accessToken: String,
            id: String
        ): Response<AudioFeature> {
            TODO("Not yet implemented")
        }

        override suspend fun getRecommendations(
            accessToken: String,
            seedTrackId: String,
            minTempo: Double,
            maxTempo: Double,
            minDancebility: Double,
            maxDancebility: Double,
            minEnergy: Double,
            maxEnergy: Double
        ): Response<RecommendTracks> {
            TODO("Not yet implemented")
        }

        override suspend fun getUsersAllPlaylists(accessToken: String): Response<Playlist> {
            TODO("Not yet implemented")
        }

        override suspend fun getTracksByPlaylistId(
            accessToken: String,
            playlistId: String
        ): Response<PlaylistById> {
            TODO("Not yet implemented")
        }

        override suspend fun createPlaylist(
            accessToken: String,
            userId: String,
            body: CreatePlaylistBody
        ): Response<PlaylistItem> {
            TODO("Not yet implemented")
        }

        override suspend fun addTracksToPlaylist(
            accessToken: String,
            contentType: String,
            playlistId: String,
            body: AddTracksBody
        ) {
            TODO("Not yet implemented")
        }

        override suspend fun deleteTracksFromPlaylist(
            accessToken: String,
            contentType: String,
            playlistId: String,
            body: DeleteTracksBody
        ) {
            TODO("Not yet implemented")
        }

    }

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun test_getUsersProfile() {
        val service = UserServiceMock()
        val repository = Repository(service)
        val user = User(
            id = "abc123",
            email ="sample@email",
            display_name = "name",
            country = "ja",
            birthday= "1234"
        )

        runBlocking {
            service.userResponse = Response.success(user)
            when(val request = repository.getUsersProfile("")){
                //後で実装する
            }
        }
    }

    @Test
    fun playbackTrack() {
    }

    @Test
    fun pausePlayback() {
    }

    @Test
    fun getUsersDevices() {
    }

    @Test
    fun getTracksByKeyword() {
    }

    @Test
    fun getAudioFeaturesById() {
    }

    @Test
    fun getRecommendTracks() {
    }

    @Test
    fun getUsersAllPlaylist() {
    }

    @Test
    fun getTracksByPlaylistId() {
    }

    @Test
    fun createPlaylist() {
    }

    @Test
    fun addTracksToPlaylist() {
    }

    @Test
    fun deleteTracksFromPlaylist() {
    }
}

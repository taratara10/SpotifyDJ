package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.PlaylistById.PlaylistById
import com.kabos.spotifydj.model.RecommendTracks
import com.kabos.spotifydj.model.SnapshotId
import com.kabos.spotifydj.model.User
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.feature.AudioFeatures
import com.kabos.spotifydj.model.networkUtil.Reason
import com.kabos.spotifydj.model.networkUtil.UserResult
import com.kabos.spotifydj.model.playback.Devices
import com.kabos.spotifydj.model.playlist.CreatePlaylistBody
import com.kabos.spotifydj.model.playlist.Playlist
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.requestBody.AddTracksBody
import com.kabos.spotifydj.model.requestBody.DeleteTracksBody
import com.kabos.spotifydj.model.requestBody.PlaybackBody
import com.kabos.spotifydj.model.requestBody.ReorderBody
import com.kabos.spotifydj.model.track.SearchTracks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

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

        var searchTracksResponse: Response<SearchTracks>? = null
        override suspend fun getTracksByKeyword(
            accessToken: String,
            keyword: String,
            type: String
        ): Response<SearchTracks>
        = searchTracksResponse ?: throw IllegalArgumentException("SearchTracksResponse is null")

        var audioFeaturesResponse: Response<AudioFeature>? = null
        override suspend fun getAudioFeaturesById(
            accessToken: String,
            id: String
        ): Response<AudioFeature>
        = audioFeaturesResponse ?: throw IllegalArgumentException("AudioFeatureResponse is null")

        var recommendTracksResponse: Response<RecommendTracks>? = null
        override suspend fun getRecommendations(
            accessToken: String,
            seedTrackId: String,
            minTempo: Double,
            maxTempo: Double,
            minDancebility: Double,
            maxDancebility: Double,
            minEnergy: Double,
            maxEnergy: Double
        ): Response<RecommendTracks>
        = recommendTracksResponse ?: throw IllegalArgumentException("RecommendTrackResponse is null")

        var playlistResponse: Response<Playlist>? = null
        override suspend fun getUsersAllPlaylists(accessToken: String): Response<Playlist>
        = playlistResponse ?: throw IllegalArgumentException("PlaylistResponse is null")

        var playlistByIdResponse: Response<PlaylistById>? = null
        override suspend fun getTracksByPlaylistId(
            accessToken: String,
            playlistId: String
        ): Response<PlaylistById>
        = playlistByIdResponse ?: throw IllegalArgumentException("PlaylistByIdResponse is null")


        var playlistItemResponse: Response<PlaylistItem>? = null
        override suspend fun createPlaylist(
            accessToken: String,
            userId: String,
            body: CreatePlaylistBody
        ): Response<PlaylistItem>
        = playlistItemResponse ?: throw IllegalArgumentException("PlaylistItemResponse is null")


        override suspend fun addTracksToPlaylist(
            accessToken: String,
            contentType: String,
            playlistId: String,
            body: AddTracksBody
        ): Response<SnapshotId> {
            TODO("Not yet implemented")
        }

        override suspend fun reorderPlaylistsTracks(
            accessToken: String,
            contentType: String,
            playlistId: String,
            body: ReorderBody
        ): Response<SnapshotId> {
            TODO("Not yet implemented")
        }

        override suspend fun deleteTracksFromPlaylist(
            accessToken: String,
            contentType: String,
            playlistId: String,
            body: DeleteTracksBody
        ): Response<SnapshotId> {
            TODO("Not yet implemented")
        }

    }

    val errorBody400 = "{\n" +
            "  \"error\": {\n" +
            "    \"status\": 400,\n" +
            "    \"message\": \"invalid id\"\n" +
            "  }\n" +
            "}"
    val errorBody401 = "{\n" +
            "  \"error\": {\n" +
            "    \"status\": 401,\n" +
            "    \"message\": \"The access token expired\"\n" +
            "  }\n" +
            "}"

    val errorBody403 = "{\n" +
            "  \"error\": {\n" +
            "    \"status\": 403,\n" +
            "    \"message\": \"Forbidden\"\n" +
            "  }\n" +
            "}"

    val errorBody404 = "{\n" +
            "  \"error\": {\n" +
            "    \"status\": 404,\n" +
            "    \"message\": \"Not Found\"\n" +
            "  }\n" +
            "}"


    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun test_getUsersProfile_isSuccess() {
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
            when(val result = repository.getUsersProfile("token")){
                is UserResult.Success -> assertEquals(user, result.data)
                is UserResult.Failure -> fail("Response must be UserResult.Success, but it is $result")
            }
        }
    }

    @Test
    fun test_getUsersProfile_emptyAccessToken(){
        val service = UserServiceMock()
        val repository = Repository(service)

        runBlocking {
            when(val result = repository.getUsersProfile("")){
                is UserResult.Success -> fail("Response must be UserResult.Failure, but it is $result")
                is UserResult.Failure -> assertEquals(Reason.EmptyAccessToken, result.reason)
            }
        }
    }

    //private fun errorReasonHandler()のテストも兼ねてる
    @Test
    fun test_getUsersProfile_unAuthorized(){
        val service = UserServiceMock()
        val repository = Repository(service)

        runBlocking {
            val body = ResponseBody.create(null, errorBody401)
            service.userResponse = Response.error(401,body)

            when(val result = repository.getUsersProfile("token")){
                is UserResult.Success -> fail("Response must be UserResult.Failure, but it is $result")
                is UserResult.Failure -> assertEquals(Reason.UnAuthorized, result.reason)
            }
        }
    }

    //private fun errorReasonHandler()のテストも兼ねてる
    @Test
    fun test_getUsersProfile_notFound(){
        val service = UserServiceMock()
        val repository = Repository(service)

        runBlocking {
            val body = ResponseBody.create(null, errorBody404)
            service.userResponse = Response.error(404,body)

            when(val result = repository.getUsersProfile("token")){
                is UserResult.Success -> fail("Response must be UserResult.Failure, but it is $result")
                is UserResult.Failure -> assertEquals(Reason.NotFound, result.reason)
            }
        }
    }
    //private fun errorReasonHandler()のテストも兼ねてる
    @Test
    fun test_getUsersProfile_responseError(){
        val service = UserServiceMock()
        val repository = Repository(service)

        runBlocking {
            val body400 = ResponseBody.create(null, errorBody400)
            service.userResponse = Response.error(400,body400)
            when(val result = repository.getUsersProfile("token")){
                is UserResult.Success -> fail("Response must be UserResult.Failure, but it is $result")
                is UserResult.Failure -> assertEquals(Reason.ResponseError("invalid id"), result.reason)
            }

            val body403 = ResponseBody.create(null, errorBody403)
            service.userResponse = Response.error(403, body403)
            when(val result = repository.getUsersProfile("token")){
                is UserResult.Success -> fail("Response must be UserResult.Failure, but it is $result")
                is UserResult.Failure -> assertEquals(Reason.ResponseError("Forbidden"), result.reason)
            }
        }
    }
    //private fun errorReasonHandler()のテストも兼ねてる
    @Test
    fun test_getUsersProfile_fetchFailed(){
        val service = UserServiceMock()
        val repository = Repository(service)

        runBlocking {
            when(val result = repository.getUsersProfile("token")){
                is UserResult.Success -> fail("Response must be UserResult.Failure, but it is $result")
                is UserResult.Failure -> assertEquals(Reason.UnKnown(IllegalArgumentException()).javaClass, result.reason.javaClass)
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

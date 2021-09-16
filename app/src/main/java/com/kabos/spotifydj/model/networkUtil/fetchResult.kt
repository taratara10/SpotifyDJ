package com.kabos.spotifydj.model.networkUtil

import com.kabos.spotifydj.model.SnapshotId
import com.kabos.spotifydj.model.User
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.track.TrackItems
import java.lang.Exception


sealed class UserResult{
     data class Success(val data: User): UserResult()
     data class Failure(val reason: Reason): UserResult()
}

sealed class TrackItemsResult {
     data class Success(val data: List<TrackItems>) : TrackItemsResult()
     data class Failure(val reason: Reason) : TrackItemsResult()
}

sealed class AudioFeatureResult {
     data class Success(val data: AudioFeature): AudioFeatureResult()
     data class Failure(val reason: Reason): AudioFeatureResult()
}

sealed class PlaylistItemsResult {
     data class Success(val data: List<PlaylistItem>): PlaylistItemsResult()
     data class Failure(val reason: Reason): PlaylistItemsResult()
}

sealed class EditPlaylistResult {
     object Success:EditPlaylistResult()
     data class Failure(val reason: Reason): EditPlaylistResult()
}

sealed class Reason {
     object EmptyAccessToken : Reason()
     object UnAuthorized: Reason() //401 The access token expired
     object NotFound: Reason()     //404
     data class ResponseError(val message: String): Reason() //400 BadRequest or 403 Forbidden
     data class UnKnown(val exception: Exception): Reason()  //Non-network reason
}

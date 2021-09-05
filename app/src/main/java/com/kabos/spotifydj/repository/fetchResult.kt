package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.User
import com.kabos.spotifydj.model.track.TrackItems
import java.lang.Exception


sealed class Reason {
     object EmptyAccessToken :Reason()
     object UnAuthorized: Reason()
     object NotFound: Reason()
     data class ResponseError(val message: String): Reason()
     data class UnKnown(val exception: Exception): Reason()
}

sealed class UserResult{
     data class Success(val data: User): UserResult()
     data class Failure(val reason: Reason): UserResult()
}

sealed class TrackItemsResult {
     data class Success(val data: List<TrackItems>?) : TrackItemsResult()
     data class Failure(val reason: Reason) : TrackItemsResult()
}

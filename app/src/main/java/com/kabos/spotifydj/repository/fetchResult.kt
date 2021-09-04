package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.User
import com.kabos.spotifydj.model.track.TrackItems
import java.lang.Exception

sealed class FetchResult{
     data class Success(val data: String): FetchResult()

     data class Failure(val reason: Reason): FetchResult(){
          sealed class Reason {
               object EmptyAccessToken :Reason()
               object UnAuthorized: Reason()
               data class UnKnown(val exception: Exception): Reason()
          }
     }
}

sealed class UserResult{
     data class Success(val data: User): UserResult()

     data class Failure(val reason: Reason): UserResult(){
          sealed class Reason {
               object EmptyAccessToken :Reason()
               object UnAuthorized: Reason()
               data class UnKnown(val exception: Exception): Reason()
          }
     }
}

sealed class TrackItemsResult {
     data class Success(val data: List<TrackItems>?): TrackItemsResult()

     data class Failure(val reason: Reason): TrackItemsResult(){
          sealed class Reason {
               object EmptyAccessToken :Reason()
               object UnAuthorized: Reason()
               data class UnKnown(val exception: Exception): Reason()
          }
     }
}

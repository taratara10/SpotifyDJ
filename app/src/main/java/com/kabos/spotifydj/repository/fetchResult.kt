package com.kabos.spotifydj.repository

import java.lang.Exception

sealed class FetchResult<T>(
     val data: T? = null,
     val exception: Exception? = null
) {
     class Success<T>(data: T): FetchResult<T>(data)
     class Failure<T>(data: T? = null, exception: Exception): FetchResult<T>(data,exception) {
          sealed class Reason {
               object EmptyAccessToken :Reason()
               object UnAuthorized: Reason()
               class UnKnown(exception: Exception): Reason()
          }
     }
}

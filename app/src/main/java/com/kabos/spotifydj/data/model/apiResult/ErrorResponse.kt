package com.kabos.spotifydj.data.model.apiResult

import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber
import java.lang.Exception

data class ErrorResponse(
    val error: ErrorContent
){
    companion object {
        fun <T> Response<T>.toSpotifyApiErrorResponse(): ErrorResponse? {
            val converter = Moshi.Builder().build().adapter(ErrorResponse::class.java)
            return try {
                 val errorBody = this.errorBody()
                 return if (errorBody != null) {
                     converter.fromJson(errorBody.string())
                 } else null
            } catch (e: Exception) {
                Timber.d("Error convertSpotifyApiErrorResponse $e")
                null
            }
        }
    }
}


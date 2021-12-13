package com.kabos.spotifydj.model.apiResult

import com.squareup.moshi.Moshi
import retrofit2.Response
import java.lang.Exception

data class ErrorResponse(
    val error: ErrorContent
){
    companion object {
        private val converter = Moshi.Builder().build().adapter(ErrorResponse::class.java)
        fun <T> Response<T>.toSpotifyApiErrorResponse(): ErrorResponse? {
            return try {
                 val errorBody = this.errorBody()
                 return if (errorBody != null) {
                     converter.fromJson(errorBody.string())
                 } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}

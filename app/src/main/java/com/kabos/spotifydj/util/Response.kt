package com.kabos.spotifydj.util

import com.kabos.spotifydj.data.model.apiResult.ErrorResponse
import com.kabos.spotifydj.data.model.exception.SpotifyApiException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import timber.log.Timber
import java.lang.Exception

fun <T> Response<T>.errorHandling(): T {
    return if (this.isSuccessful) this.body()!!
    else throw handleSpotifyApiException(this)
}

private fun <T> handleSpotifyApiException(body: Response<T>): SpotifyApiException {
    val apiError =
        body.toSpotifyApiErrorResponse() ?: return SpotifyApiException.UnKnown(null)
    return when (apiError.error.status) {
        401 -> SpotifyApiException.UnAuthorized
        404 -> SpotifyApiException.NotFound
        else -> SpotifyApiException.ResponseError(apiError.error.message)
    }

}

fun <T> Response<T>.toSpotifyApiErrorResponse(): ErrorResponse? {
    val converter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(ErrorResponse::class.java)
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

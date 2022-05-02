package com.kabos.spotifydj.data.model.apiResult

sealed class SpotifyApiResource<T>(
    val data: T? = null,
    val reason: SpotifyApiErrorReason? = null
) {
    class Success<T>(data: T) : SpotifyApiResource<T>(data)
    class Error<T>(reason: SpotifyApiErrorReason) : SpotifyApiResource<T>(null ,reason)
}

package com.kabos.spotifydj.model.apiResult

sealed class SpotifyApiResource<T>(
    val data: T? = null,
    val reason: SpotifyApiErrorReason? = null
    ) {
        class Success<T>(data: T) : SpotifyApiResource<T>(data)
        class Loading<T>(data: T? = null) : SpotifyApiResource<T>(data)
        class Error<T>(reason: SpotifyApiErrorReason, data: T? = null) : SpotifyApiResource<T>(data, reason)
    }

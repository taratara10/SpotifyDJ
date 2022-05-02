package com.kabos.spotifydj.data.model.apiResult

import java.lang.Exception

sealed class SpotifyApiErrorReason {
    object EmptyAccessToken: SpotifyApiErrorReason()
    object UnAuthorized: SpotifyApiErrorReason() //401 The access token expired
    object NotFound: SpotifyApiErrorReason()     //404
    data class ResponseError(val message: String): SpotifyApiErrorReason() //400 BadRequest or 403 Forbidden
    data class UnKnown(val exception: Exception?): SpotifyApiErrorReason()  //Non-network reason
}

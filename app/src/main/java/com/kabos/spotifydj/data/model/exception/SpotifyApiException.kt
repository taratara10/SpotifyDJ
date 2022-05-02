package com.kabos.spotifydj.data.model.exception

import java.io.IOException

sealed class SpotifyApiException: Exception() {
    object UnAuthorized: IOException() //401 The access token expired
    object NotFound: SpotifyApiException()     //404
    data class ResponseError(val error : String): SpotifyApiException() //400 BadRequest or 403 Forbidden
    data class UnKnown(val exception: Exception?): SpotifyApiException()  //Non-network reason
}

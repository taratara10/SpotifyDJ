package com.kabos.spotifydj.data.model.exception

sealed class SpotifyApiException: Exception() {
    object EmptyAccessToken: SpotifyApiException()
    object UnAuthorized: SpotifyApiException() //401 The access token expired
    object NotFound: SpotifyApiException()     //404
    data class ResponseError(val error : String): SpotifyApiException() //400 BadRequest or 403 Forbidden
    data class UnKnown(val exception: Exception?): SpotifyApiException()  //Non-network reason
}

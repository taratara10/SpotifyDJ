package com.kabos.spotifydj.model.requestBody

data class AddTracksBody(
    val uris: List<String>,
    val position: Int? = null
)

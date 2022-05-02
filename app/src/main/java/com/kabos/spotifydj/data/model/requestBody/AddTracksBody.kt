package com.kabos.spotifydj.data.model.requestBody

data class AddTracksBody(
    val uris: List<String>,
    val position: Int? = null
)

package com.kabos.spotifydj.model.playlist

data class AddItemToPlaylistBody(
    val uris: List<String>,
    val position: Int? = null
)

package com.kabos.spotifydj.data.model.playlist

data class CreatePlaylistBody(
    val name: String,
    val public: Boolean = false,
    val description: String = ""
)

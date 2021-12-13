package com.kabos.spotifydj.model.playlist

data class Playlist(
    val href: String,
    val items: List<PlaylistItem>,
    val limit: Int,
    val next: Any,
    val offset: Int,
    val previous: String?,
    val total: Int
)




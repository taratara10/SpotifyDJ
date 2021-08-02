package com.kabos.spotifydj.model.PlaylistById

data class PlaylistById(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: Any,
    val total: Int
)
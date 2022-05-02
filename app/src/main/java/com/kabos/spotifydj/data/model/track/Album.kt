package com.kabos.spotifydj.data.model.track

data class Album(
    val album_type: String,
    val artists: List<Artist>,
    val available_markets: List<String>,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val type: String,
    val uri: String
)

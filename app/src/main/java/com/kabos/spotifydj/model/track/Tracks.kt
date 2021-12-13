package com.kabos.spotifydj.model.track

data class Tracks(
    val href: String,
    val items: List<TrackItems>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: String?,
    val total: Int
)

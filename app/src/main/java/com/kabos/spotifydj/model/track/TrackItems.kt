package com.kabos.spotifydj.model.track

data class TrackItems(
    val album: Album,
    val artists: List<ArtistX>,
    val available_markets: List<String>,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val href: String,
    val id: String,
    val is_local: Boolean,
    val name: String,
    val popularity: Int,
    val _url: String?,
    val track_number: Int,
    val type: String,
    val uri: String
)

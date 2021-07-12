package com.kabos.spotifydj.model

data class RecentlyPlaylist(
    val href: String,
    val items: List<PlayListItem>,
    val limit: Int,
    val next: String
)

data class PlayListItem(
    val played_at: String,
    val track: Track
)
data class Track(
    val available_markets: List<String>,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val href: String,
    val id: String,
    val name: String,
    val preview_url: String,
    val track_number: Int,
    val type: String,
    val uri: String
)

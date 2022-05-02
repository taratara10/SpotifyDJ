package com.kabos.spotifydj.data.model.PlaylistById

import com.kabos.spotifydj.data.model.track.TrackItems

data class PlaylistById(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: String?,
    val offset: Int,
    val previous: String?,
    val total: Int
)


data class Item(
    val added_at: String,
    val is_local: Boolean,
    val primary_color: String?,
    val track: TrackItems,
)

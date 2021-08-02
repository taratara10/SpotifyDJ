package com.kabos.spotifydj.model.PlaylistById

import com.kabos.spotifydj.model.track.TrackItems

data class PlaylistById(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: Any,
    val total: Int
)


data class Item(
    val added_at: String,
    val is_local: Boolean,
    val primary_color: Any,
    val track: TrackItems,
)
package com.kabos.spotifydj.model.PlaylistById

import com.kabos.spotifydj.model.track.TrackItems

data class Item(
    val added_at: String,
    val added_by: AddedBy,
    val is_local: Boolean,
    val primary_color: Any,
    val track: TrackItems,
    val video_thumbnail: VideoThumbnail
)
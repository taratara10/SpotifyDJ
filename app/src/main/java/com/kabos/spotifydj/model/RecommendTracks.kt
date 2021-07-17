package com.kabos.spotifydj.model

import com.kabos.spotifydj.model.track.TrackItems

data class RecommendTracks(
    val seeds: List<Seeds>,
    val tracks: List<TrackItems>
)

data class Seeds(
    val afterFilteringSize: Int,
    val afterRelinkingSize: Int,
    val href: String,
    val id: String,
    val initialPoolSize: Int,
    val type: String
)

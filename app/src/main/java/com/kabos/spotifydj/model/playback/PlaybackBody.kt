package com.kabos.spotifydj.model.playback

data class PlaybackBody(
    val context_uri: String,
    val offset: Offset? = null,
    val position_ms: Int? = 0
)


data class Offset(
    val position: Int?
)



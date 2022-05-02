package com.kabos.spotifydj.data.model.requestBody

data class PlaybackBody(
    val uris:List<String>,
//    val offset: Offset? = null,
//    val position_ms: Int? = 0
)


data class Offset(
    val position: Int?
)



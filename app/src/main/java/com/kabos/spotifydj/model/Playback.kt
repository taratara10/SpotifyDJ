package com.kabos.spotifydj.model

data class Playback(
    val context_uri: String,
    val offset: Offset,
    val position_ms: Int
)
data class Offset(
    val position: Int
)

data class Devices(
    val devices: List<Device>
)

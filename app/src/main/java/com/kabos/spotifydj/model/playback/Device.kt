package com.kabos.spotifydj.model.playback

data class Device(
    val id: String,
    val is_active: Boolean,
    val is_restricted: Boolean,
    val name: String,
    val type: String,
    val volume_percent: Int
)
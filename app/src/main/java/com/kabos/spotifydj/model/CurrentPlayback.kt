package com.kabos.spotifydj.model

data class CurrentPlayback(
    val context: Context,
    val currently_playing_type: String,
    val device: Device,
    val is_playing: Boolean,
    val item: Item,
    val progress_ms: String,
    val repeat_state: String,
    val shuffle_state: Boolean,
    val timestamp: Long
)
data class Context(
    val external_urls: ExternalUrls,
    val href: String,
    val type: String,
    val uri: String
)
data class Device(
    val id: String,
    val is_active: Boolean,
    val is_restricted: Boolean,
    val name: String,
    val type: String,
    val volume_percent: Int
)

data class ExternalUrls(
    val spotify: String
)

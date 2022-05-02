package com.kabos.spotifydj.data.model.playback

data class CurrentPlayback(
    val context: Context,
    val currently_playing_type: String,
    val device: Device,
    val is_playing: Boolean,
    val progress_ms: String,
    val repeat_state: String,
    val shuffle_state: Boolean,
    val timestamp: Long
)

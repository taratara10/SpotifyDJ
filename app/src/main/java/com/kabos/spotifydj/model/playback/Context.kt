package com.kabos.spotifydj.model.playback

data class Context(
    val external_urls: ExternalUrls,
    val href: String,
    val type: String,
    val uri: String
)
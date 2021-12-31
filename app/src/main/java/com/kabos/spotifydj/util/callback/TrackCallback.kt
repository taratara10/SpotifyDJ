package com.kabos.spotifydj.util.callback

import com.kabos.spotifydj.model.TrackInfo

interface TrackCallback {
    fun addTrack(trackInfo: TrackInfo)
    fun playback(trackInfo: TrackInfo)
    fun onClick(trackInfo: TrackInfo)
}

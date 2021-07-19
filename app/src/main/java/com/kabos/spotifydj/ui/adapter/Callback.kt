package com.kabos.spotifydj.ui.adapter

import com.kabos.spotifydj.model.TrackInfo

interface AdapterCallback {
    fun addTrack(trackInfo: TrackInfo)
    fun playback(trackInfo: TrackInfo)
    fun onClick(trackInfo: TrackInfo)
}

interface navigateCallback

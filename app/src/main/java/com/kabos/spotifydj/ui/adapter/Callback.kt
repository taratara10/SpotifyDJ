package com.kabos.spotifydj.ui.adapter

import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.model.playlist.PlaylistItem

interface AdapterCallback {
    fun addTrack(trackInfo: TrackInfo)
    fun playback(trackInfo: TrackInfo)
    fun onClick(trackInfo: TrackInfo)
}

interface PlaylistCallback {
    fun onClick(playlistItem: PlaylistItem)
}


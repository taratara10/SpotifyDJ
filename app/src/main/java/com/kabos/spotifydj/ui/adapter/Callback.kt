package com.kabos.spotifydj.ui.adapter

import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.model.playlist.PlaylistItem

interface AdapterCallback {
    fun addTrack(trackInfo: TrackInfo)
    fun playback(trackInfo: TrackInfo)
    fun onClick(trackInfo: TrackInfo)
}

//Dialog
interface PlaylistCallback {
    fun onClick(playlistItem: PlaylistItem)
}

//Playlist Fragment
interface DragTrackCallback {
    fun onClick(trackInfo: TrackInfo)
    fun playback(trackInfo: TrackInfo)
    fun onSwiped(position: Int)
    fun onDropped(initial:Int, final:Int)
}

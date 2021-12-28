package com.kabos.spotifydj.util.callback

import com.kabos.spotifydj.model.TrackInfo

interface DragTrackCallback {
    fun onClick(trackInfo: TrackInfo)
    fun playback(trackInfo: TrackInfo)
    fun onSwiped(position: Int)
    fun onDropped(initial:Int, final:Int)
}

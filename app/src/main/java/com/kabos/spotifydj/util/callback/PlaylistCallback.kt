package com.kabos.spotifydj.util.callback

import com.kabos.spotifydj.model.playlist.PlaylistItem

interface PlaylistCallback {
    fun onClick(playlistItem: PlaylistItem)
}


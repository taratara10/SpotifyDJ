package com.kabos.spotifydj.util.callback

import com.kabos.spotifydj.data.model.playlist.PlaylistItem

interface PlaylistCallback {
    fun onClick(playlistItem: PlaylistItem)
}


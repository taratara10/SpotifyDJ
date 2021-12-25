package com.kabos.spotifydj.util

sealed class Pager(val position: Int, val name: String) {
    object Search: Pager(0, "SEARCH")
    object Recommend: Pager(1, "RECOMMEND")
    object Playlist: Pager(2, "PLAYLIST")
}

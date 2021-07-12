package com.kabos.spotifydj.model

data class User(
    val id: String,
    val email: String,
    val display_name: String,
    val country: String,
    val birthday: String
)


data class Playlist(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: Any,
    val offset: Int,
    val previous: Any,
    val total: Int
)

data class Item(
    val collaborative: Boolean,
    val href: String,
    val id: String,
    val images: List<Any>,
    val name: String,
    val `public`: Boolean,
    val snapshot_id: String,
    val type: String,
    val uri: String
)

package com.kabos.spotifydj.data.model

data class User(
    val id: String,
    val email: String,
    val display_name: String,
    val country: String?,
    val birthday: String?
)


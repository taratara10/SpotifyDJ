package com.kabos.spotifydj.model.networkUtil

data class ApiError(
    val error: Error
)

data class Error(
    val message: String,
    val status: Int
)

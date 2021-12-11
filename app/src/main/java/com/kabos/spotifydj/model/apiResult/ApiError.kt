package com.kabos.spotifydj.model.apiResult

data class ApiError(
    val error: Error
)

data class Error(
    val message: String,
    val status: Int
)

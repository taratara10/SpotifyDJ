package com.kabos.spotifydj.data.model.requestBody

data class ReorderBody(
    val insert_before: Int,
    val range_start: Int,
    val range_length: Int = 1
)

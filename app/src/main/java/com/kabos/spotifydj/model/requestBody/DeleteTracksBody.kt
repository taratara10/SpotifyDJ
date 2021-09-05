package com.kabos.spotifydj.model.requestBody

data class DeleteTracksBody(
    val tracks: List<DeleteTrack>
)

data class DeleteTrack(
    val uri: String
)

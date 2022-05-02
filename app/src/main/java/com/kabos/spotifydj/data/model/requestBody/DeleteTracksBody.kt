package com.kabos.spotifydj.data.model.requestBody

data class DeleteTracksBody(
    val tracks: List<DeleteTrack>
)

data class DeleteTrack(
    val uri: String
)

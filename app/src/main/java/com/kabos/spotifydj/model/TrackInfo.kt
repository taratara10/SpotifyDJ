package com.kabos.spotifydj.model

data class TrackInfo(
   val id: String,
   val name: String,
   val artist: String,
   val imageUrl: String,
   val tempo: Double,
   val danceability: Double,
   val energy: Double

)

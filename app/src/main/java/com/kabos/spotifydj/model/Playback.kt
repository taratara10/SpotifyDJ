package com.kabos.spotifydj.model

import android.bluetooth.BluetoothClass

data class Playback(
    val context_uri: String,
    val offset: Offset,
    val position_ms: Int
)
data class Offset(
    val position: Int
)



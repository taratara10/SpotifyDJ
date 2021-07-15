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

//Device class を作る

data class Devices(
    val devices: List<BluetoothClass.Device>
)

data class Image(
    val height: Int,
    val url: String,
    val width: Int
)

data class ExternalUrlsXX(
    val spotify: String
)

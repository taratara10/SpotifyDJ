package com.kabos.spotifydj.model

//playlistをadd/removeした際に返ってくる結果。レスポンスの確認用で、データは特に使わない。
data class SnapshotId(
    val snapshot_id: String
)

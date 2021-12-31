package com.kabos.spotifydj.model.playlist

import com.kabos.spotifydj.util.constant.PlaylistConstant

data class PlaylistItem(
    val collaborative: Boolean,
    val description: String,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val owner: Owner,
    val primary_color: Any? = null,
    val `public`: Boolean,
    val snapshot_id: String,
    val type: String,
    val uri: String
) {
    companion object {
        fun createNewPlaylistItem(): PlaylistItem {
            return PlaylistItem(
                collaborative = false,
                description = "",
                external_urls = ExternalUrls(""),
                href = "",
                id = PlaylistConstant.CREATE_NEW_PLAYLIST_ID,
                images = listOf(Image(url = "firstItem")),
                name = "新規作成",
                owner = Owner("", ExternalUrlsX(""),"","","",""),
                public = false,
                snapshot_id = "",
                type = "",
                uri = ""
            )
        }
    }
}

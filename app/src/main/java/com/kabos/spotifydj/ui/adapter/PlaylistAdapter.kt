package com.kabos.spotifydj.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kabos.spotifydj.databinding.AdapterUsersPlaylistBinding
import com.kabos.spotifydj.model.playlist.PlaylistItem

class PlaylistAdapter(private val callback: PlaylistCallback)
    : androidx.recyclerview.widget.ListAdapter<PlaylistItem, PlaylistViewHolder>(PlaylistDiffCallback){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AdapterUsersPlaylistBinding.inflate(layoutInflater, parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position), callback)
    }
}

class PlaylistViewHolder(private val binding: AdapterUsersPlaylistBinding)
    : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: PlaylistItem, callback: PlaylistCallback){
        binding.apply {
            tvPlaylistTitle.text = item.name

            Glide.with(root.context)
                .load(item.images[0].url)
                .into(ivPlaylistImage)

            adapterUserPlaylist.setOnClickListener {
                callback.onClick(item)
            }
        }
    }

}


private object PlaylistDiffCallback: DiffUtil.ItemCallback<PlaylistItem>(){
    override fun areItemsTheSame(oldItem: PlaylistItem, newItem: PlaylistItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PlaylistItem, newItem: PlaylistItem): Boolean {
        return oldItem == newItem
    }

}

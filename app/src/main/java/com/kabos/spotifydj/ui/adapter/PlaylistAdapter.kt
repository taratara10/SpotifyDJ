package com.kabos.spotifydj.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.ListItemPlaylistBinding
import com.kabos.spotifydj.model.playlist.*

class PlaylistAdapter(private val callback: PlaylistCallback)
    : ListAdapter<PlaylistItem, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemPlaylistBinding.inflate(layoutInflater, parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlaylistViewHolder(private val binding: ListItemPlaylistBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PlaylistItem){
            binding.apply {
                title.text = item.name
                if (item.images.isNotEmpty()){
                    if (item.images[0].url == "firstItem"){
                        playlistImage.setImageResource(R.drawable.ic_baseline_add_circle_outline_24)
                    }else {
                        Glide.with(root.context)
                            .load(item.images[0].url)
                            .into(playlistImage)
                    }
                }
                listItemPlaylist.setOnClickListener {
                    callback.onClick(item)
                }
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

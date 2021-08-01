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

            //
            when(item.images.size){
                0 -> {setImage(root.context,ivPlaylistImage1,item.images[0].url)}
                1 -> {
                    setImage(root.context,ivPlaylistImage1,item.images[0].url)
                    setImage(root.context,ivPlaylistImage1,item.images[1].url)
                }
                2 -> {
                    setImage(root.context,ivPlaylistImage1,item.images[0].url)
                    setImage(root.context,ivPlaylistImage1,item.images[1].url)
                    setImage(root.context,ivPlaylistImage1,item.images[2].url)
                }
                else -> {
                    setImage(root.context,ivPlaylistImage1,item.images[0].url)
                    setImage(root.context,ivPlaylistImage1,item.images[1].url)
                    setImage(root.context,ivPlaylistImage1,item.images[2].url)
                    setImage(root.context,ivPlaylistImage1,item.images[3].url)
                }
            }
            adapterUserPlaylist.setOnClickListener {
                callback.onClick(item)
            }
        }
    }
    private fun setImage(context:Context,imageView: ImageView, url:String){
        Glide.with(context)
            .load(url)
            .into(imageView)
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

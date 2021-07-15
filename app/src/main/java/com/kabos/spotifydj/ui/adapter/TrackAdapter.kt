package com.kabos.spotifydj.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kabos.spotifydj.databinding.AdapterTrackBinding.inflate
import com.kabos.spotifydj.databinding.AdapterTrackBinding
import com.kabos.spotifydj.model.track.TrackItems
import kotlin.coroutines.coroutineContext

class TrackAdapter(private val callback: () -> Unit): androidx.recyclerview.widget.ListAdapter<TrackItems, TrackViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = inflate(layoutInflater, parent, false)
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position), callback)
    }
}

class TrackViewHolder(private val binding: AdapterTrackBinding)
    : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: TrackItems, callback: () -> Unit){
        binding.apply {
            tvTrackName.text = item.name
            tvArtistName.text = item.artists[0].name
            Glide.with(root.context)
                .load(item.album.images[0].url)
                .into(ivTrackImage)

        }
    }
}


private object DiffCallback: DiffUtil.ItemCallback<TrackItems>(){
    override fun areItemsTheSame(oldItem: TrackItems, newItem: TrackItems): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: TrackItems, newItem: TrackItems): Boolean {
        return oldItem == newItem
    }

}

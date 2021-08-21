package com.kabos.spotifydj.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kabos.spotifydj.databinding.AdapterTrackBinding.inflate
import com.kabos.spotifydj.databinding.AdapterTrackBinding
import com.kabos.spotifydj.model.TrackInfo

class TrackAdapter(private val callback: AdapterCallback)
    : androidx.recyclerview.widget.ListAdapter<TrackInfo, TrackViewHolder>(DiffCallback) {
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
    fun bind(item: TrackInfo, callback: AdapterCallback){
        binding.apply {
            tvTrackName.text = item.name
            tvArtistName.text = item.artist
            tvTempo.text = "BPM: ${Math.round(item.tempo *10.0)/10.0}"
            Glide.with(root.context)
                .load(item.imageUrl)
                .into(ivTrackImage)

            if (item.isPlayback) {
                ivPlaybackPause.visibility = View.VISIBLE
                ivPlayback.visibility = View.GONE
            }else {
                ivPlaybackPause.visibility = View.GONE
                ivPlayback.visibility = View.VISIBLE
            }

            ivPlayback.setOnClickListener {
                callback.playback(item)
            }
            ivAddTrack.setOnClickListener {
                callback.addTrack(item)
            }
            trackAdapter.setOnClickListener {
                callback.onClick(item)
            }

        }
    }
}


private object DiffCallback: DiffUtil.ItemCallback<TrackInfo>(){
    override fun areItemsTheSame(oldItem: TrackInfo, newItem: TrackInfo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: TrackInfo, newItem: TrackInfo): Boolean {
        return oldItem.isPlayback == newItem.isPlayback
    }

}

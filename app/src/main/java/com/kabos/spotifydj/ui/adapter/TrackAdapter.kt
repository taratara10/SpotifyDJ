package com.kabos.spotifydj.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.ListItemTrackBinding
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.util.callback.TrackCallback

class TrackAdapter(private val callback: TrackCallback)
    : ListAdapter<TrackInfo, TrackAdapter.TrackViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemTrackBinding.inflate(layoutInflater, parent, false)
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TrackViewHolder(private val binding: ListItemTrackBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrackInfo){
            binding.apply {
                trackName.text = item.name
                artistName.text = item.artist
                trackTempo.text = root.context.getString(R.string.track_tempo, item.tempo)
                Glide.with(root.context)
                    .load(item.imageUrl)
                    .into(trackImage)
                playback.isVisible = !item.isPlayback
                playbackPause.isVisible = item.isPlayback

                // todo pauseなくていいの？
                playback.setOnClickListener {
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
}

private object DiffCallback: DiffUtil.ItemCallback<TrackInfo>(){
    override fun areItemsTheSame(oldItem: TrackInfo, newItem: TrackInfo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: TrackInfo, newItem: TrackInfo): Boolean {
        return oldItem.isPlayback == newItem.isPlayback
    }

}

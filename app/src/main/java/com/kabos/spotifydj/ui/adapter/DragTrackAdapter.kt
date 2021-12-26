package com.kabos.spotifydj.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.ernestoyaquello.dragdropswiperecyclerview.util.DragDropSwipeDiffCallback
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.AdapterDragTrackBinding
import com.kabos.spotifydj.model.TrackInfo
import kotlin.math.roundToInt

class DragTrackAdapter(private val callback: DragTrackCallback, dataset: List<TrackInfo>)
    : DragDropSwipeAdapter<TrackInfo, DragTrackAdapter.DragTrackViewHolder>(dataset)  {
    val onItemDragListener = object :OnItemDragListener<TrackInfo>{
        override fun onItemDragged(previousPosition: Int, newPosition: Int, item: TrackInfo) {
            callback.onDropped(previousPosition,newPosition)
        }
        override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: TrackInfo) {  }
    }

    val onItemSwipeListener = object :OnItemSwipeListener<TrackInfo>{
        override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: TrackInfo): Boolean {
            callback.onSwiped(position)
            return false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DragTrackViewHolder {
        val binding = AdapterDragTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DragTrackViewHolder(binding)
    }

    override fun onBindViewHolder(item: TrackInfo, viewHolder: DragTrackViewHolder, position: Int) {
        viewHolder.bind(item)
    }

    override fun createDiffUtil(oldList: List<TrackInfo>, newList: List<TrackInfo>)
    : DragDropSwipeDiffCallback<TrackInfo> {
        return object : DragDropSwipeDiffCallback<TrackInfo>(oldList, newList) {
            override fun isSameContent(oldItem: TrackInfo, newItem: TrackInfo): Boolean {
                return oldItem == newItem
            }

            override fun isSameItem(oldItem: TrackInfo, newItem: TrackInfo): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getViewToTouchToStartDraggingItem(
        item: TrackInfo,
        viewHolder: DragTrackViewHolder,
        position: Int
    ): View {
        return viewHolder.binding.dragIndicator
    }


    override fun getViewHolder(itemView: View): DragTrackViewHolder {
        TODO("Not yet implemented")
    }

    fun submitList(tracks: List<TrackInfo>){
        this.dataSet = tracks
    }

    inner class DragTrackViewHolder(val binding: AdapterDragTrackBinding)
        :  DragDropSwipeAdapter.ViewHolder(binding.root) {
        fun bind(item: TrackInfo){
            binding.apply {
                trackName.text = item.name
                artistName.text = item.artist
                trackTempo.text = root.context.getString(R.string.track_tempo, item.tempo)
                Glide.with(root.context)
                    .load(item.imageUrl)
                    .into(trackImage)
                playback.setOnClickListener {
                    callback.playback(item)
                }
                trackAdapter.setOnClickListener {
                    callback.onClick(item)
                }
            }
        }
    }
}




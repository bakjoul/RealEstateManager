package com.bakjoul.realestatemanager.ui.details

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.databinding.FragmentDetailsMediaItemBinding
import com.bumptech.glide.Glide

class DetailsAdapter : ListAdapter<DetailsMediaItemViewState, DetailsAdapter.ViewHolder>(DetailsDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentDetailsMediaItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: FragmentDetailsMediaItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetailsMediaItemViewState) {
            Glide.with(binding.detailsMediaItemPhoto.context)
                .load(item.url)
                .into(binding.detailsMediaItemPhoto)
            binding.detailsMediaItemDescription.text = item.description
            binding.detailsMediaItem.setOnClickListener { Log.d("test", "photo ${item.id} clicked") }
        }
    }

    object DetailsDiffCallback : DiffUtil.ItemCallback<DetailsMediaItemViewState>() {
        override fun areItemsTheSame(oldItem: DetailsMediaItemViewState, newItem: DetailsMediaItemViewState): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DetailsMediaItemViewState, newItem: DetailsMediaItemViewState): Boolean =
            oldItem == newItem
    }
}

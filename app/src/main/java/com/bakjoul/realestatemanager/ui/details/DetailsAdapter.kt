package com.bakjoul.realestatemanager.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.databinding.PhotosItemBinding
import com.bumptech.glide.Glide

class DetailsAdapter : ListAdapter<DetailsMediaItemViewState, DetailsAdapter.ViewHolder>(DetailsDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        PhotosItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: PhotosItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetailsMediaItemViewState) {
            Glide.with(binding.detailsPhotoItemImageView.context)
                .load(item.url)
                .into(binding.detailsPhotoItemImageView)
            binding.detailsPhotoItemDescription.text = item.description
            binding.detailsPhotoItem.setOnClickListener { item.onPhotoClicked() }
        }
    }

    object DetailsDiffCallback : DiffUtil.ItemCallback<DetailsMediaItemViewState>() {
        override fun areItemsTheSame(oldItem: DetailsMediaItemViewState, newItem: DetailsMediaItemViewState): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DetailsMediaItemViewState, newItem: DetailsMediaItemViewState): Boolean = oldItem == newItem
    }
}

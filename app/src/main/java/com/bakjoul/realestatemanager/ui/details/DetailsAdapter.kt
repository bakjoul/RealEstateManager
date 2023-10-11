package com.bakjoul.realestatemanager.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.databinding.FragmentDetailsPhotosItemBinding
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListItemViewState
import com.bumptech.glide.Glide

class DetailsAdapter : ListAdapter<PhotoListItemViewState, DetailsAdapter.ViewHolder>(DetailsDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentDetailsPhotosItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: FragmentDetailsPhotosItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PhotoListItemViewState) {
            Glide.with(binding.detailsPhotoItemImageView.context).load(item.url).into(binding.detailsPhotoItemImageView)
            binding.detailsPhotoItemDescription.text = item.description
            binding.detailsPhotoItem.setOnClickListener { item.onPhotoClicked() }
        }
    }

    object DetailsDiffCallback : DiffUtil.ItemCallback<PhotoListItemViewState>() {
        override fun areItemsTheSame(oldItem: PhotoListItemViewState, newItem: PhotoListItemViewState): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PhotoListItemViewState, newItem: PhotoListItemViewState): Boolean = oldItem == newItem
    }
}

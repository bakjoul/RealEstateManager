package com.bakjoul.realestatemanager.ui.photos

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentPhotosThumbnailItemBinding
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListItemViewState
import com.bumptech.glide.Glide

class PhotosAdapter : ListAdapter<PhotoListItemViewState, PhotosAdapter.ViewHolder>(PhotosDiffCallback) {

    private var selectedItem: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentPhotosThumbnailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedItem)
    }

    class ViewHolder(private val binding: FragmentPhotosThumbnailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PhotoListItemViewState, isSelected: Boolean) {
            Glide.with(binding.photosThumbnailItemImageView.context).load(item.url).into(binding.photosThumbnailItemImageView)
            binding.photosThumbnailItemDescription.text = item.description
            binding.photosThumbnailItem.setOnClickListener { item.onPhotoClicked() }

            if (isSelected) {
                binding.photosThumbnailItemOverlay.visibility = View.GONE
                binding.photosThumbnailItemFrame.setBackgroundResource(R.drawable.photo_frame_bkg)
            } else {
                binding.photosThumbnailItemFrame.setBackgroundResource(R.color.grey_transparent)
                binding.photosThumbnailItemOverlay.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItem(position: Int) {
        selectedItem = position
        notifyDataSetChanged()
    }

    object PhotosDiffCallback : DiffUtil.ItemCallback<PhotoListItemViewState>() {
        override fun areItemsTheSame(oldItem: PhotoListItemViewState, newItem: PhotoListItemViewState): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PhotoListItemViewState, newItem: PhotoListItemViewState): Boolean = oldItem == newItem
    }
}

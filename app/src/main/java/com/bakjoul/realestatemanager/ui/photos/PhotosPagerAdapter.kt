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
import com.bakjoul.realestatemanager.ui.common_model.PhotoItemViewState
import com.bumptech.glide.Glide

class PhotosPagerAdapter : ListAdapter<PhotoItemViewState, PhotosPagerAdapter.ViewHolder>(PhotosDiffCallback) {

    private var selectedItem: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentPhotosThumbnailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedItem)
    }

    class ViewHolder(private val binding: FragmentPhotosThumbnailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PhotoItemViewState, isSelected: Boolean) {
            Glide.with(binding.photosThumbnailItemImageView.context).load(item.url).into(binding.photosThumbnailItemImageView)
            binding.photosThumbnailItemDescription.text = item.description
            binding.photosThumbnailItem.setOnClickListener { item.onPhotoClicked() }

            if (isSelected) {
                binding.photosThumbnailItemOverlay.visibility = View.GONE
                binding.photosThumbnailItemFrame.setBackgroundResource(R.drawable.photo_frame_bkg)
            } else {
                binding.photosThumbnailItemFrame.setBackgroundResource(android.R.color.transparent)
                binding.photosThumbnailItemOverlay.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItem(position: Int) {
        selectedItem = position
        notifyDataSetChanged()
    }

    object PhotosDiffCallback : DiffUtil.ItemCallback<PhotoItemViewState>() {
        override fun areItemsTheSame(oldItem: PhotoItemViewState, newItem: PhotoItemViewState): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PhotoItemViewState, newItem: PhotoItemViewState): Boolean = oldItem == newItem
    }
}

package com.bakjoul.realestatemanager.ui.photos

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.PhotosItemBinding
import com.bakjoul.realestatemanager.ui.details.DetailsMediaItemViewState
import com.bumptech.glide.Glide

class PhotosPagerAdapter : ListAdapter<DetailsMediaItemViewState, PhotosPagerAdapter.ViewHolder>(PhotosDiffCallback) {

    private var selectedItem: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        PhotosItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedItem)
    }

    class ViewHolder(private val binding: PhotosItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetailsMediaItemViewState, isSelected: Boolean) {
            Glide.with(binding.detailsPhotoItemImageView.context).load(item.url).into(binding.detailsPhotoItemImageView)
            binding.detailsPhotoItemDescription.text = item.description
            binding.detailsPhotoItem.setOnClickListener { item.onPhotoClicked() }

            if (isSelected) {
                binding.detailsPhotoItemOverlay.visibility = View.GONE
                binding.detailsPhotoItemFrame.setBackgroundResource(R.drawable.photo_frame_bkg)
            } else {
                binding.detailsPhotoItemFrame.setBackgroundResource(android.R.color.transparent)
                binding.detailsPhotoItemOverlay.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItem(position: Int) {
        selectedItem = position
        notifyDataSetChanged()
    }

    object PhotosDiffCallback : DiffUtil.ItemCallback<DetailsMediaItemViewState>() {
        override fun areItemsTheSame(oldItem: DetailsMediaItemViewState, newItem: DetailsMediaItemViewState): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DetailsMediaItemViewState, newItem: DetailsMediaItemViewState): Boolean = oldItem == newItem
    }
}

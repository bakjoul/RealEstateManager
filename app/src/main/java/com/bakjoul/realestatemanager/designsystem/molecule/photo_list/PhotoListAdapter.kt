package com.bakjoul.realestatemanager.designsystem.molecule.photo_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ViewPhotoItemBinding
import com.bumptech.glide.Glide

class PhotoListAdapter : ListAdapter<PhotoListItemViewState, PhotoListAdapter.ViewHolder>(PhotoDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ViewPhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ViewPhotoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PhotoListItemViewState) {
            Glide.with(binding.photoItemImageView.context).load(item.uri).into(binding.photoItemImageView)
            binding.photoItemDescription.text = item.description

            when (item.selectType) {
                SelectType.SELECTED -> {
                    binding.photoItemShade.visibility = View.GONE
                    binding.photoItemFrame.setBackgroundResource(R.drawable.photo_frame_bkg)
                    binding.photoItemFrame.setPadding(2, 2, 2, 2)
                }
                SelectType.NOT_SELECTED -> {
                    binding.photoItemFrame.setBackgroundResource(R.color.grey_transparent)
                    binding.photoItemShade.visibility = View.VISIBLE
                    binding.photoItemFrame.setPadding(2, 2, 2, 2)
                }
                else -> {
                    binding.photoItemShade.visibility = View.GONE
                    binding.photoItemFrame.setBackgroundResource(0)
                    binding.photoItemFrame.setPadding(0, 0, 0, 0)
                }
            }

            binding.photoItem.setOnClickListener { item.onPhotoClicked() }

            if (item.isFeatured != null) {
                binding.photoItemFeature.setImageResource(
                    if (item.isFeatured) {
                        R.drawable.star_filled_24
                    } else {
                        R.drawable.star_24
                    }
                )
            }

            if (item.onFeaturePhotoClicked != null) {
                binding.photoItemFeature.visibility = View.VISIBLE
                binding.photoItemFeature.setOnClickListener {
                    item.onFeaturePhotoClicked.invoke()
                }
            } else {
                binding.photoItemFeature.visibility = View.GONE
            }

            if (item.onDeletePhotoClicked != null) {
                binding.photoItemDelete.visibility = View.VISIBLE
                binding.photoItemDelete.setOnClickListener {
                    item.onDeletePhotoClicked.invoke(item.id, item.uri)
                }
            } else {
                binding.photoItemDelete.visibility = View.GONE
            }

            if (item.onDescriptionClicked != null) {
                binding.photoItemDescription.setOnClickListener {
                    item.onDescriptionClicked.invoke(item.id, item.description)
                }
            }
        }
    }

    object PhotoDiffCallback : DiffUtil.ItemCallback<PhotoListItemViewState>() {
        override fun areItemsTheSame(oldItem: PhotoListItemViewState, newItem: PhotoListItemViewState): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PhotoListItemViewState, newItem: PhotoListItemViewState): Boolean = oldItem == newItem
    }
}

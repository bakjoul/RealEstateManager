package com.bakjoul.realestatemanager.ui.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.databinding.FragmentAddPropertyPhotoItemBinding
import com.bumptech.glide.Glide

class AddPropertyPhotoAdapter : ListAdapter<AddPropertyPhotoItemViewState, AddPropertyPhotoAdapter.ViewHolder>(AddPropertyPhotoDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentAddPropertyPhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: FragmentAddPropertyPhotoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AddPropertyPhotoItemViewState) {
            Glide.with(binding.addPropertyPhotoItemImageView.context).load(item.url).into(binding.addPropertyPhotoItemImageView)
            binding.addPropertyPhotoItemDescription.text = item.description
            binding.addPropertyPhotoItem.setOnClickListener { item.onPhotoClicked() }
            binding.addPropertyPhotoItemDelete.setOnClickListener { item.onDeletePhotoClicked() }
        }
    }

    object AddPropertyPhotoDiffCallback : DiffUtil.ItemCallback<AddPropertyPhotoItemViewState>() {
        override fun areItemsTheSame(oldItem: AddPropertyPhotoItemViewState, newItem: AddPropertyPhotoItemViewState): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AddPropertyPhotoItemViewState, newItem: AddPropertyPhotoItemViewState): Boolean = oldItem == newItem
    }
}

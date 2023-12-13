package com.bakjoul.realestatemanager.ui.drafts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentDraftsItemBinding
import com.bumptech.glide.Glide

class DraftsAdapter : ListAdapter<DraftsItemViewState, DraftsAdapter.ViewHolder>(ViewHolder.DraftsDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentDraftsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: FragmentDraftsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DraftsItemViewState) {
            if (item.photoUrl.isEmpty()) {
                binding.draftItemPhoto.setImageResource(R.drawable.baseline_photo_24)
            } else {
                Glide.with(binding.draftItemPhoto.context).load(item.photoUrl).into(binding.draftItemPhoto)
            }
            binding.draftItemLastUpdate.text = item.lastUpdate
            binding.draftItemTypeAndLocation.text = item.typeAndLocation
            binding.draftItemOverview.text = item.overview
            binding.draftItemDescription.text = item.description
            binding.draftsItemLayout.setOnClickListener { item.onDraftItemClicked.invoke() }
        }

        object DraftsDiffCallback : DiffUtil.ItemCallback<DraftsItemViewState>() {
            override fun areItemsTheSame(oldItem: DraftsItemViewState, newItem: DraftsItemViewState): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: DraftsItemViewState, newItem: DraftsItemViewState): Boolean = oldItem == newItem
        }
    }
}

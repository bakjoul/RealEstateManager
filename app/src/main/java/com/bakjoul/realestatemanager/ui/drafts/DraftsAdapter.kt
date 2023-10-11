package com.bakjoul.realestatemanager.ui.drafts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.databinding.FragmentDraftsItemBinding

class DraftsAdapter : ListAdapter<DraftsItemViewState, DraftsAdapter.ViewHolder>(ViewHolder.DraftsDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentDraftsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: FragmentDraftsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DraftsItemViewState) {

        }

        object DraftsDiffCallback : DiffUtil.ItemCallback<DraftsItemViewState>() {
            override fun areItemsTheSame(oldItem: DraftsItemViewState, newItem: DraftsItemViewState): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: DraftsItemViewState, newItem: DraftsItemViewState): Boolean = oldItem == newItem
        }
    }
}

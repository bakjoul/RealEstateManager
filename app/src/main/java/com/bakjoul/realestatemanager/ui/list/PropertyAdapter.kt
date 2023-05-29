package com.bakjoul.realestatemanager.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.databinding.FragmentListItemBinding

class PropertyAdapter : ListAdapter<PropertyItemViewState, PropertyAdapter.ViewHolder>(PropertyDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: FragmentListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PropertyItemViewState) {
            // TODO photo
            binding.listItemType.text = item.type
            binding.listItemCity.text = item.city
            binding.listItemPrice.text = item.price
        }
    }

    object PropertyDiffCallback : DiffUtil.ItemCallback<PropertyItemViewState>() {
        override fun areItemsTheSame(oldItem: PropertyItemViewState, newItem: PropertyItemViewState): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PropertyItemViewState, newItem: PropertyItemViewState): Boolean = oldItem == newItem
    }
}

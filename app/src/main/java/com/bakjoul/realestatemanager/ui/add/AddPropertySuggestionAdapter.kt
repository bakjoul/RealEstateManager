package com.bakjoul.realestatemanager.ui.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.databinding.FragmentAddPropertySuggestionItemBinding

class AddPropertySuggestionAdapter : ListAdapter<AddPropertySuggestionItemViewState, AddPropertySuggestionAdapter.ViewHolder>(AddPropertySuggestionDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentAddPropertySuggestionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: FragmentAddPropertySuggestionItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AddPropertySuggestionItemViewState) {
            binding.addPropertySuggestionTextView.text = item.address
            binding.addPropertySuggestionItem.setOnClickListener { item.onSuggestionClicked() }
        }
    }

    object AddPropertySuggestionDiffCallback : DiffUtil.ItemCallback<AddPropertySuggestionItemViewState>() {
        override fun areItemsTheSame(oldItem: AddPropertySuggestionItemViewState, newItem: AddPropertySuggestionItemViewState): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AddPropertySuggestionItemViewState, newItem: AddPropertySuggestionItemViewState): Boolean = oldItem == newItem
    }
}

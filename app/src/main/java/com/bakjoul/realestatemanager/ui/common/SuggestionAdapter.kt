package com.bakjoul.realestatemanager.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.databinding.ViewSuggestionItemBinding

class SuggestionAdapter : ListAdapter<SuggestionItemViewState, SuggestionAdapter.ViewHolder>(
    SuggestionDiffCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ViewSuggestionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ViewSuggestionItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SuggestionItemViewState) {
            binding.suggestionTextView.text = item.description
            binding.suggestionItem.setOnClickListener { item.onSuggestionClicked() }
        }
    }

    object SuggestionDiffCallback : DiffUtil.ItemCallback<SuggestionItemViewState>() {
        override fun areItemsTheSame(oldItem: SuggestionItemViewState, newItem: SuggestionItemViewState): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SuggestionItemViewState, newItem: SuggestionItemViewState): Boolean = oldItem == newItem
    }
}

package com.bakjoul.realestatemanager.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentListItemBinding
import com.bumptech.glide.Glide

class PropertyAdapter : ListAdapter<PropertyItemViewState, PropertyAdapter.ViewHolder>(ViewHolder.PropertyDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: FragmentListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PropertyItemViewState) {
            Glide.with(binding.listItemPhoto.context)
                .load(item.photoUrl)
                .into(binding.listItemPhoto)
            binding.listItemType.text = item.type
            binding.listItemCity.text = item.city
            binding.listItemFeatures?.text = item.features
            binding.listItemPrice.text = item.price

            if (item.currencyRate.isEmpty()) {
                binding.listItemInfo?.visibility = View.GONE
            } else {
                binding.listItemInfo?.visibility = View.VISIBLE
                binding.listItemInfo?.setOnClickListener {
                    val tooltipView = LayoutInflater.from(binding.root.context).inflate(R.layout.tooltip_info, null)
                    val tooltipTextView = tooltipView.findViewById<TextView>(R.id.tooltip_text)

                    tooltipTextView.text = item.currencyRate
                    PopupWindow(binding.root.context).apply {
                        contentView = tooltipView
                        width = ViewGroup.LayoutParams.WRAP_CONTENT
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        isOutsideTouchable = true
                        showAsDropDown(binding.listItemInfo, 24, 0)
                    }
                }
            }

            binding.listItemLayout.setOnClickListener {
                item.onPropertyClicked.invoke()
            }
        }

        object PropertyDiffCallback : DiffUtil.ItemCallback<PropertyItemViewState>() {
            override fun areItemsTheSame(oldItem: PropertyItemViewState, newItem: PropertyItemViewState): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PropertyItemViewState, newItem: PropertyItemViewState): Boolean = oldItem == newItem
        }
    }
}


package com.bakjoul.realestatemanager.ui.list

import android.annotation.SuppressLint
import android.graphics.Paint
import android.text.SpannableString
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
import com.bakjoul.realestatemanager.ui.utils.DensityUtil
import com.bumptech.glide.Glide

class PropertyAdapter : ListAdapter<PropertyItemViewState, PropertyAdapter.ViewHolder>(ViewHolder.PropertyDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: FragmentListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("InflateParams")
        fun bind(item: PropertyItemViewState) {
            if (item.photoUrl.isEmpty()) {
                binding.listItemPhoto.setImageResource(R.drawable.baseline_photo_24)
                binding.listItemPhoto.scaleX = 1.33f
            } else {
                Glide.with(binding.listItemPhoto.context).load(item.photoUrl).into(binding.listItemPhoto)
            }
            binding.listItemPhotoOverlay.visibility = if (item.isSold) View.VISIBLE else View.GONE
            binding.listItemSold.visibility = if (item.isSold) View.VISIBLE else View.GONE
            binding.listItemType.text = item.type.toCharSequence(binding.root.context)
            binding.listItemCity.text = item.city
            binding.listItemFeatures.text = item.features.toCharSequence(binding.root.context)
            binding.listItemPrice.text = item.price
            binding.listItemPrice.paintFlags = if (item.isSold) binding.listItemPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else 0

            if (item.currencyRate.isEmpty()) {
                binding.listItemInfo.visibility = View.GONE
            } else {
                binding.listItemInfo.visibility = View.VISIBLE
                binding.listItemInfo.setOnClickListener {
                    val tooltipView = LayoutInflater.from(binding.root.context).inflate(R.layout.fragment_list_tooltip, null)
                    val tooltipTextView = tooltipView.findViewById<TextView>(R.id.tooltip_text)

                    tooltipTextView.text = SpannableString.valueOf(item.currencyRate)
                    PopupWindow(binding.root.context).apply {
                        contentView = tooltipView
                        width = ViewGroup.LayoutParams.WRAP_CONTENT
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        isOutsideTouchable = true
                        showAsDropDown(binding.listItemInfo, DensityUtil.dip2px(binding.root.context, 12f), 0)
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


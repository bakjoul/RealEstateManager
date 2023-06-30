package com.bakjoul.realestatemanager.ui.details

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.databinding.FragmentDetailsViewpagerItemBinding
import com.bumptech.glide.Glide

class DetailsPagerAdapter : RecyclerView.Adapter<DetailsPagerAdapter.ViewHolder>() {
    private var photoUrls: List<String> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentDetailsViewpagerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photoUrls[position])
    }

    override fun getItemCount(): Int = photoUrls.size

    class ViewHolder(private val binding: FragmentDetailsViewpagerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photoUrl: String) {
            Glide.with(binding.photoItemImageView.context)
                .load(photoUrl)
                .into(binding.photoItemImageView)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(photoUrls: List<String>) {
        this.photoUrls = photoUrls
        notifyDataSetChanged()
    }
}

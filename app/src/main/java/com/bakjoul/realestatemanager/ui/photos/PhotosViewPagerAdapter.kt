package com.bakjoul.realestatemanager.ui.photos

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.databinding.FragmentPhotosViewpagerItemBinding
import com.bumptech.glide.Glide

class PhotosViewPagerAdapter : RecyclerView.Adapter<PhotosViewPagerAdapter.ViewHolder>() {
    private var photoUrls: List<String> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentPhotosViewpagerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photoUrls[position])
    }

    override fun getItemCount(): Int = photoUrls.size

    class ViewHolder(private val binding: FragmentPhotosViewpagerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photoUrl: String) {
            Glide.with(binding.photosViewpagerItemImageView.context).load(photoUrl).into(binding.photosViewpagerItemImageView)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(photoUrls: List<String>) {
        this.photoUrls = photoUrls
        notifyDataSetChanged()
    }
}

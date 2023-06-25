package com.bakjoul.realestatemanager.ui.photos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bakjoul.realestatemanager.databinding.FragmentPhotoItemBinding
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bumptech.glide.Glide

class PhotosPagerAdapter(private val photos: List<PhotoEntity>) : RecyclerView.Adapter<PhotosPagerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        FragmentPhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    class ViewHolder(private val binding: FragmentPhotoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: PhotoEntity) {
            Glide.with(binding.photoItemImageView.context)
                .load(photo.url)
                .into(binding.photoItemImageView)
        }
    }
}

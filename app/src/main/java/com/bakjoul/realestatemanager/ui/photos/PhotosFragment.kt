package com.bakjoul.realestatemanager.ui.photos

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.viewpager2.widget.ViewPager2
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentPhotosBinding
import com.bakjoul.realestatemanager.ui.utils.DensityUtil
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PhotosFragment @Inject constructor() : DialogFragment(R.layout.fragment_photos) {

    private val binding by viewBinding { FragmentPhotosBinding.bind(it) }
    private val viewModel: PhotosViewModel by viewModels()

    private var isViewPagerFirstOpening: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDialogWindow()

        binding.photosViewPagerCloseButton.setOnClickListener { dismiss() }

        // Photos ViewPager
        val viewPagerAdapter = PhotosViewPagerAdapter()
        binding.photosViewPager.adapter = viewPagerAdapter
        binding.photosDotsIndicator.attachTo(binding.photosViewPager)
        binding.photosViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (!isViewPagerFirstOpening) {
                    viewModel.updateCurrentPhotoId(position)
                }
            }
        })

        // ViewPager thumbnails RecyclerView
        val thumbnailsAdapter = PhotosPagerAdapter()
        binding.photosThumbnailsRecyclerView.adapter = thumbnailsAdapter
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.photosThumbnailsRecyclerView)

        viewModel.photosViewStateLiveData.observe(viewLifecycleOwner) { viewState ->

            viewPagerAdapter.updateData(viewState.photosUrls)
            if (viewState.currentPhotoId != -1 && isViewPagerFirstOpening) {
                binding.photosViewPager.setCurrentItem(viewState.currentPhotoId, false)
                isViewPagerFirstOpening = false
            } else if (viewState.currentPhotoId != -1) {
                binding.photosViewPager.setCurrentItem(viewState.currentPhotoId, true)
            }

            thumbnailsAdapter.submitList(viewState.thumbnails)
            if (viewState.currentPhotoId != -1) {
                binding.photosThumbnailsRecyclerView.smoothScrollToPosition(viewState.currentPhotoId)
                thumbnailsAdapter.setSelectedItem(viewState.currentPhotoId)
            }
        }
    }

    private fun setDialogWindow() {
        val backgroundColor = ColorDrawable(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        val inset = InsetDrawable(backgroundColor, DensityUtil.dip2px(requireContext(), 4f))
        dialog?.window?.setBackgroundDrawable(inset)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
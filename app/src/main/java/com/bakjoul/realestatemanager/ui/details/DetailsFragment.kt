package com.bakjoul.realestatemanager.ui.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.viewpager2.widget.ViewPager2
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentDetailsBinding
import com.bakjoul.realestatemanager.ui.utils.DensityUtil
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private val binding by viewBinding { FragmentDetailsBinding.bind(it) }
    private val viewModel by viewModels<DetailsViewModel>()

    private var isViewPagerFirstOpening: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detailsToolbar.setPadding(0,0,0,0)  // Removes toolbar padding on tablets

        // Medias RecyclerView
        val recyclerViewAdapter = DetailsAdapter()
        binding.detailsMediaRecyclerView.adapter = recyclerViewAdapter

        // Medias ViewPager
        val viewPagerAdapter = DetailsPagerAdapter()
        binding.detailsViewPager.adapter = viewPagerAdapter
        binding.detailsViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.photosDotsIndicator.attachTo(binding.detailsViewPager)
        binding.detailsViewPagerCloseButton.setOnClickListener { closeViewPager() }
        binding.detailsViewPagerConstraintLayout.setOnClickListener { closeViewPager() }
        binding.detailsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (!isViewPagerFirstOpening) {
                    viewModel.updateCurrentPhotoId(position)
                }
            }
        })

        binding.detailsFabBack?.setOnClickListener {
            viewModel.resetCurrentPhotoId()
            viewModel.resetCurrentPropertyId()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.detailsFabClose?.setOnClickListener {
            viewModel.resetCurrentPhotoId()
            viewModel.resetCurrentPropertyId()
        }

        // ViewPager thumbnails
        val thumbnailsAdapter = DetailsPagerThumbnailsAdapter()
        binding.detailsThumnnailsRecyclerView.adapter = thumbnailsAdapter
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.detailsThumnnailsRecyclerView)

        setToolbarInfoAnimation()

        viewModel.detailsLiveData.observe(viewLifecycleOwner) { details ->
            Glide.with(binding.detailsToolbarPhoto)
                .load(details.mainPhotoUrl)
                .into(binding.detailsToolbarPhoto)
            binding.detailsToolbarType.text = details.type
            binding.detailsToolbarPrice.text = details.price
            binding.detailsToolbarPrice.paintFlags = if (details.isSold) binding.detailsToolbarPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else 0
            binding.detailsToolbarSold.visibility = if (details.isSold) View.VISIBLE else View.GONE
            binding.detailsToolbarCity.text = details.city
            binding.detailsToolbarSurface.text = details.surface
            binding.detailsToolbarSaleStatus.text = details.sale_status
            binding.detailsDescriptionText.text = details.description
            binding.detailsItemSurface.setText(details.surface)
            binding.detailsItemRooms.setText(details.rooms)
            binding.detailsItemBedrooms.setText(details.bedrooms)
            binding.detailsItemBathrooms.setText(details.bathrooms)
            setTooltip(details.poiSchool, binding.detailsPoiSchool, getString(R.string.tooltip_school))
            setTooltip(details.poiStore, binding.detailsPoiStore, getString(R.string.tooltip_store))
            setTooltip(details.poiPark, binding.detailsPoiPark, getString(R.string.tooltip_park))
            setTooltip(details.poiRestaurant, binding.detailsPoiRestaurant, getString(R.string.tooltip_restaurant))
            setTooltip(details.poiHospital, binding.detailsPoiHospital, getString(R.string.tooltip_hospital))
            setTooltip(details.poiBus, binding.detailsPoiBus, getString(R.string.tooltip_bus))
            setTooltip(details.poiSubway, binding.detailsPoiSubway, getString(R.string.tooltip_subway))
            setTooltip(details.poiTramway, binding.detailsPoiTramway, getString(R.string.tooltip_tramway))
            setTooltip(details.poiTrain, binding.detailsPoiTrain, getString(R.string.tooltip_train))
            setTooltip(details.poiAirport, binding.detailsPoiAirport, getString(R.string.tooltip_airport))
            binding.detailsItemLocation.setText(details.location)

            recyclerViewAdapter.submitList(details.medias)

            viewPagerAdapter.updateData(details.photoUrls)
            if (details.clickedPhotoId != -1 && isViewPagerFirstOpening) {
                binding.detailsViewPager.setCurrentItem(details.clickedPhotoId, false)
                isViewPagerFirstOpening = false
            } else if (details.clickedPhotoId != -1) {
                binding.detailsViewPager.setCurrentItem(details.clickedPhotoId, true)
            }
            if (details.clickedPhotoId == -1) {
                if (binding.detailsViewPagerConstraintLayout.visibility != View.INVISIBLE) {
                    binding.detailsViewPagerConstraintLayout.visibility = View.INVISIBLE
                }
            } else {
                if (binding.detailsViewPagerConstraintLayout.visibility != View.VISIBLE) {
                    binding.detailsViewPagerConstraintLayout.visibility = View.VISIBLE
                }
            }

            thumbnailsAdapter.submitList(details.medias)
            if (details.clickedPhotoId != -1) {
                binding.detailsThumnnailsRecyclerView.smoothScrollToPosition(details.clickedPhotoId)
                thumbnailsAdapter.setSelectedItem(details.clickedPhotoId)
            }

            binding.detailsItemLocation.setOnLongClickListener {
                val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("address", details.clipboardAddress)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(requireContext(), getString(R.string.details_address_clipboard), Toast.LENGTH_SHORT).show()
                true
            }
            Glide.with(binding.detailsStaticMap)
                .load(details.staticMapUrl)
                .into(binding.detailsStaticMap)
            binding.detailsStaticMap.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${details.mapsAddress}"))
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
        }
    }

    private fun closeViewPager() {
        binding.detailsViewPagerConstraintLayout.visibility = View.INVISIBLE
        viewModel.resetCurrentPhotoId()
        isViewPagerFirstOpening = true
    }

    private fun setToolbarInfoAnimation() {
        val toolbarInfo = binding.detailsToolbarTypePriceStatusContainer
        val initialMargin = DensityUtil.dip2px(requireContext(), 0f)
        val maximumMargin = DensityUtil.dip2px(requireContext(), 30f)
        val fabHeight = DensityUtil.dip2px(requireContext(), 48f)
        val layoutParams = toolbarInfo.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = initialMargin

        var previousVerticalOffset = 0
        var previousMargin = initialMargin // Avoids unnecessary updates

        binding.detailsAppbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val animatedMargin = (maximumMargin * (previousVerticalOffset - verticalOffset) / fabHeight)

            val newMargin = if (abs(verticalOffset) >= (totalScrollRange - fabHeight)) {
                if (previousVerticalOffset == 0) {
                    previousVerticalOffset = verticalOffset
                }
                val tempMargin = initialMargin + animatedMargin // Prevents the margin from going below the initial one
                if (tempMargin < initialMargin) {
                    initialMargin
                } else {
                    tempMargin
                }
            } else {
                previousVerticalOffset = 0
                initialMargin
            }
            // Checks if the new margin is different from the previous one
            if (previousMargin != newMargin) {
                layoutParams.marginStart = newMargin
                toolbarInfo.layoutParams = layoutParams
                toolbarInfo.requestLayout()

                // Updates previous margin
                previousMargin = newMargin
            }
        }
    }

    private fun setTooltip(isNearby: Boolean, poi: View, text: String) {
        if (isNearby) {
            poi.visibility = View.VISIBLE
            val tooltip = createPopupWindow(poi.context, text)
            poi.setOnClickListener {
                tooltip.showAsDropDown(poi, DensityUtil.dip2px(poi.context, 12f), 0)
            }
        } else {
            poi.visibility = View.GONE
        }
    }

    private fun createPopupWindow(context: Context, text: String): PopupWindow {
        val tooltipView = LayoutInflater.from(context).inflate(R.layout.tooltip_layout, null)
        val tooltipTextView = tooltipView.findViewById<TextView>(R.id.tooltip_text)
        tooltipTextView.text = text

        return PopupWindow(context).apply {
            contentView = tooltipView
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            isOutsideTouchable = true
        }
    }
}

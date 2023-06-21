package com.bakjoul.realestatemanager.ui.details

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DetailsAdapter()
        binding.detailsMediaRecyclerView.adapter = adapter

        binding.detailsFabBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        setToolbarInfoAnimation()

        viewModel.detailsLiveData.observe(viewLifecycleOwner) { details ->
            Glide.with(binding.detailsToolbarPhoto)
                .load(details.photoUrl)
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
            binding.detailsPoiSchool.visibility = if (details.poiSchool) View.VISIBLE else View.GONE
            binding.detailsPoiStore.visibility = if (details.poiStore) View.VISIBLE else View.GONE
            binding.detailsPoiPark.visibility = if (details.poiPark) View.VISIBLE else View.GONE
            binding.detailsPoiRestaurant.visibility = if (details.poiRestaurant) View.VISIBLE else View.GONE
            binding.detailsPoiHospital.visibility = if (details.poiHospital) View.VISIBLE else View.GONE
            binding.detailsPoiBus.visibility = if (details.poiBus) View.VISIBLE else View.GONE
            binding.detailsPoiSubway.visibility = if (details.poiSubway) View.VISIBLE else View.GONE
            binding.detailsPoiTramway.visibility = if (details.poiTramway) View.VISIBLE else View.GONE
            binding.detailsPoiTrain.visibility = if (details.poiTrain) View.VISIBLE else View.GONE
            binding.detailsPoiAirport.visibility = if (details.poiAirport) View.VISIBLE else View.GONE
            binding.detailsItemLocation.setText(details.location)

            adapter.submitList(details.media)

            Glide.with(binding.detailsStaticMap)
                .load(details.staticMapUrl)
                .into(binding.detailsStaticMap)
        }
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
}

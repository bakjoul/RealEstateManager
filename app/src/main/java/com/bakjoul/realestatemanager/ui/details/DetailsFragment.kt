package com.bakjoul.realestatemanager.ui.details

import android.animation.ValueAnimator
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentDetailsBinding
import com.bakjoul.realestatemanager.ui.utils.DensityUtil
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
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

        setInfoPadding()
        binding.detailsFabBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

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

    private fun setInfoPadding() {
        val startPadding = DensityUtil.dip2px(requireContext(), 0f)
        val endPadding = DensityUtil.dip2px(requireContext(), 30f)
        val paddingAnimation = ValueAnimator.ofInt(startPadding, endPadding).apply {
            duration = 250
            addUpdateListener { animator -> binding.detailsToolbarTypeAndPrice.setPadding(animator.animatedValue as Int, 0, 0, 0) }
        }

        binding.detailsAppbar.addOnOffsetChangedListener(object :
            AppBarLayout.OnOffsetChangedListener {
            var isInfoAlignedLeft = true

            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if (abs(verticalOffset) >= appBarLayout!!.totalScrollRange - binding.detailsFabBack.height && isInfoAlignedLeft) {
                    paddingAnimation.start()
                    isInfoAlignedLeft = false
                } else if (abs(verticalOffset) < appBarLayout.totalScrollRange - binding.detailsFabBack.height && !isInfoAlignedLeft) {
                    paddingAnimation.reverse()
                    isInfoAlignedLeft = true
                }
            }
        })
    }
}

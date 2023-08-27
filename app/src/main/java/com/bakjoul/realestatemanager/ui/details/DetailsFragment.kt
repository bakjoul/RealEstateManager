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
import androidx.activity.OnBackPressedCallback
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

        binding.detailsToolbar.setPadding(0, 0, 0, 0)  // Removes toolbar padding on tablets

        if (!resources.getBoolean(R.bool.isTablet)) {
            setToolbarInfoAnimation()
        }

        handleOnBackPressed()

        // Medias RecyclerView
        val recyclerViewAdapter = DetailsAdapter()
        binding.detailsMediaRecyclerView.adapter = recyclerViewAdapter

        binding.detailsFabBack?.setOnClickListener { viewModel.onBackButtonPressed() }

        viewModel.detailsLiveData.observe(viewLifecycleOwner) { details ->
            Glide.with(binding.detailsToolbarPhoto).load(details.mainPhotoUrl).into(binding.detailsToolbarPhoto)
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
            setTooltip(details.poiSchool, binding.detailsPoiSchool, getString(R.string.property_poi_desc_school))
            setTooltip(details.poiStore, binding.detailsPoiStore, getString(R.string.property_poi_desc_store))
            setTooltip(details.poiPark, binding.detailsPoiPark, getString(R.string.property_poi_desc_park))
            setTooltip(details.poiRestaurant, binding.detailsPoiRestaurant, getString(R.string.property_poi_desc_restaurant))
            setTooltip(details.poiHospital, binding.detailsPoiHospital, getString(R.string.property_poi_desc_hospital))
            setTooltip(details.poiBus, binding.detailsPoiBus, getString(R.string.property_poi_desc_bus))
            setTooltip(details.poiSubway, binding.detailsPoiSubway, getString(R.string.property_poi_desc_subway))
            setTooltip(details.poiTramway, binding.detailsPoiTramway, getString(R.string.property_poi_desc_tramway))
            setTooltip(details.poiTrain, binding.detailsPoiTrain, getString(R.string.property_poi_desc_train))
            setTooltip(details.poiAirport, binding.detailsPoiAirport, getString(R.string.property_poi_desc_airport))
            binding.detailsItemLocation.setText(details.location)

            recyclerViewAdapter.submitList(details.medias)

            binding.detailsItemLocation.setOnLongClickListener {
                val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("address", details.clipboardAddress)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(requireContext(), getString(R.string.property_address_clipboard), Toast.LENGTH_SHORT).show()
                true
            }
            Glide.with(binding.detailsStaticMap).load(details.staticMapUrl).into(binding.detailsStaticMap)
            binding.detailsStaticMap.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${details.mapsAddress}"))
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
        }

    }

    private fun setToolbarInfoAnimation() {
        val toolbarInfo = binding.detailsToolbarTypePriceStatusContainer
        val xMargin = DensityUtil.dip2px(requireContext(), 30f)
        val startAnimationY = DensityUtil.dip2px(requireContext(), 56f)
        val layoutParams = toolbarInfo.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = 0

        var previousMargin = 0 // Avoids unnecessary updates

        binding.detailsAppbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->

            val startOfAnimation = appBarLayout.totalScrollRange - startAnimationY
            val endOfAnimation = appBarLayout.totalScrollRange

            val animatedMargin = if (abs(verticalOffset) > startOfAnimation) {
                xMargin * (abs(verticalOffset) - startOfAnimation) / (endOfAnimation - startOfAnimation)
            } else {
                0
            }

            // Checks if the new margin is different from the previous one
            if (previousMargin != animatedMargin) {
                layoutParams.marginStart = animatedMargin
                toolbarInfo.layoutParams = layoutParams

                // Updates previous margin
                previousMargin = animatedMargin
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

    private fun handleOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onBackButtonPressed()
            }
        })
    }
}

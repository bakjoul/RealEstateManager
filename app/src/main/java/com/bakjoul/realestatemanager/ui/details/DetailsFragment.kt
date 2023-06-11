package com.bakjoul.realestatemanager.ui.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentDetailsBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private val binding by viewBinding { FragmentDetailsBinding.bind(it) }
    private val viewModel by viewModels<DetailsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DetailsAdapter()
        binding.detailsMediaRecyclerView.adapter = adapter

        viewModel.detailsLiveData.observe(viewLifecycleOwner) { details ->
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
        }
    }
}

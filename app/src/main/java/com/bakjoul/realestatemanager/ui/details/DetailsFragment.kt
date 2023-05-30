package com.bakjoul.realestatemanager.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<DetailsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDescriptionItems()
        viewModel.detailsLiveData.observe(viewLifecycleOwner) { details ->
            binding.detailsItemSurface.detailsItemText.text = details.surface
        }
    }

    private fun setDescriptionItems() {
        // Surface
        binding.detailsItemSurface.detailsItemIcon.setImageResource(R.drawable.baseline_settings_overscan_24)
        binding.detailsItemSurface.detailsItemLabel.text = getString(R.string.details_label_surface)
        // Rooms
        binding.detailsItemRooms.detailsItemIcon.setImageResource(R.drawable.baseline_home_24)
        binding.detailsItemRooms.detailsItemLabel.text = getString(R.string.details_label_rooms)
        // Bedrooms
        binding.detailsItemBedrooms.detailsItemIcon.setImageResource(R.drawable.bed_24)
        binding.detailsItemBedrooms.detailsItemLabel.text = getString(R.string.details_label_bedrooms)
        // Bathrooms
        binding.detailsItemBathrooms.detailsItemIcon.setImageResource(R.drawable.hot_tub_24)
        binding.detailsItemBathrooms.detailsItemLabel.text = getString(R.string.details_label_bathrooms)
        // Location
        binding.detailsItemLocation.detailsItemIcon.setImageResource(R.drawable.baseline_location_on_24)
        binding.detailsItemLocation.detailsItemLabel.text = getString(R.string.details_label_location)
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}

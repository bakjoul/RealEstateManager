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

        viewModel.detailsLiveData.observe(viewLifecycleOwner) { details ->
            binding.detailsDescriptionText.text = details.description
            binding.detailsItemSurface.setText(details.surface)
            binding.detailsItemRooms.setText(details.rooms)
            binding.detailsItemBedrooms.setText(details.bedrooms)
            binding.detailsItemBathrooms.setText(details.bathrooms)
        }
    }
}

package com.bakjoul.realestatemanager.ui.camera.photo_preview

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentPhotoPreviewBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoPreviewFragment : Fragment(R.layout.fragment_photo_preview) {

    private val binding by viewBinding { FragmentPhotoPreviewBinding.bind(it) }
    private val viewModel by viewModels<PhotoPreviewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.viewStateLiveData.observe(viewLifecycleOwner) { viewState ->
            Glide.with(binding.photoPreview).load(viewState.photoUri).into(binding.photoPreview)

            binding.photoPreviewCancelButton.setOnClickListener { viewModel.onCancelButtonClicked(viewState.photoUri) }

            binding.photoPreviewDoneButton.setOnClickListener {
                // TODO CHECK IF DESCRIPTION IS NOT EMPTY
                Toast.makeText(requireContext(), "Photo successfully added", Toast.LENGTH_SHORT).show()

                viewModel.onDoneButtonClicked()
            }
        }
    }
}

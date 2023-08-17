package com.bakjoul.realestatemanager.ui.camera.photo_preview

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentPhotoPreviewBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoPreviewFragment : Fragment(R.layout.fragment_photo_preview) {

    private val binding by viewBinding { FragmentPhotoPreviewBinding.bind(it) }
    private val viewModel by viewModels<PhotoPreviewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.viewStateLiveData.observe(viewLifecycleOwner) { viewState ->
            binding.photoPreview.setImageURI(viewState.photoUri)

            binding.photoPreviewCancelButton.setOnClickListener {
                viewState.photoUri?.let {
                    requireContext().contentResolver.delete(it, null, null)
                }
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            binding.photoPreviewDoneButton.setOnClickListener {
                // TODO CHECK IF DESCRIPTION IS NOT EMPTY
                    Toast.makeText(requireContext(), "Photo successfully added", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
            }
        }
    }
}
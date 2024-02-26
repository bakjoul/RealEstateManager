package com.bakjoul.realestatemanager.ui.photo_preview

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentPhotoPreviewBinding
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.showAsToast
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoPreviewFragment : Fragment(R.layout.fragment_photo_preview) {

    private val binding by viewBinding { FragmentPhotoPreviewBinding.bind(it) }
    private val viewModel by viewModels<PhotoPreviewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.photoPreviewCancelButton.setOnClickListener { viewModel.onCancelButtonClicked() }
        binding.photoPreviewDoneButton.setOnClickListener { viewModel.onDoneButtonClicked() }

        binding.photoPreviewDescriptionEditText.doAfterTextChanged {
            viewModel.onDescriptionChanged(it)
        }

        viewModel.viewStateLiveData.observe(viewLifecycleOwner) {
            Glide.with(binding.photoPreview).load(it.photoUri).into(binding.photoPreview)
            binding.photoPreviewDescription.error = it.descriptionError?.toCharSequence(requireContext())
        }

        viewModel.viewActionLiveData.observeEvent(viewLifecycleOwner) {
            when (it) {
                is PhotoPreviewViewAction.ShowToast -> it.message.showAsToast(requireContext())
            }
        }
    }
}

package com.bakjoul.realestatemanager.ui.edit_description

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentEditPhotoDescriptionBinding
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.hideKeyboard
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditPhotoDescriptionFragment @Inject constructor() :
    DialogFragment(R.layout.fragment_edit_photo_description) {

    companion object {
        fun newInstance(photoId: Long, description: String): EditPhotoDescriptionFragment {
            val args = Bundle().apply {
                putLong("photoId", photoId)
                putString("description", description)
            }
            val fragment = EditPhotoDescriptionFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val binding by viewBinding { FragmentEditPhotoDescriptionBinding.bind(it) }
    private val viewModel by viewModels<EditPhotoDescriptionViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editPhotoDescriptionEditText.setText(arguments?.getString("description"))
        binding.editPhotoDescriptionEditText.doAfterTextChanged { viewModel.onDescriptionChanged(it) }
        binding.editPhotoDescriptionEditText.requestFocus()

        binding.editPhotoCancelButton.setOnClickListener { viewModel.onCancelButtonClicked() }
        binding.editPhotoSaveButton.setOnClickListener { viewModel.onSaveButtonClicked() }

        viewModel.viewStateLiveData.observe(viewLifecycleOwner) {
            binding.editPhotoDescription.error = it.descriptionError?.toCharSequence(requireContext())
        }

        viewModel.viewActionLiveData.observeEvent(viewLifecycleOwner) {
            when (it) {
                EditPhotoDescriptionViewAction.CloseDialog -> {
                    hideKeyboard()
                    dismiss()
                }
            }
        }
    }
}

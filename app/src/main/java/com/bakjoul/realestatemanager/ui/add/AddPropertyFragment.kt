package com.bakjoul.realestatemanager.ui.add

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentAddPropertyBinding
import com.bakjoul.realestatemanager.ui.camera.activity.CameraActivity
import com.bakjoul.realestatemanager.ui.utils.CustomThemeDialog
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.hideKeyboard
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPropertyFragment : DialogFragment(R.layout.fragment_add_property) {

    private companion object {
        private const val DIALOG_WINDOW_WIDTH = 0.5
        private const val DIALOG_WINDOW_HEIGHT = 0.9
    }

    private val binding by viewBinding { FragmentAddPropertyBinding.bind(it) }
    private val viewModel by viewModels<AddPropertyViewModel>()

    private val requestCameraPermissionLauncher = activityResultLauncher()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : CustomThemeDialog(requireContext(), R.style.FullScreenDialog) {
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                if (currentFocus != null) {
                    hideKeyboard()
                    currentFocus!!.clearFocus()
                }
                return super.dispatchTouchEvent(ev)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (resources.getBoolean(R.bool.isTablet)) {
            val width = (resources.displayMetrics.widthPixels * DIALOG_WINDOW_WIDTH).toInt()
            val height = (resources.displayMetrics.heightPixels * DIALOG_WINDOW_HEIGHT).toInt()
            dialog?.window?.setLayout(width, height)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val suggestionsRVAdapter = AddPropertySuggestionAdapter()
        binding.addPropertyAddressSuggestionsRecyclerView.adapter = suggestionsRVAdapter
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
        binding.addPropertyAddressSuggestionsRecyclerView.addItemDecoration(divider)

        // Property type radio group
        binding.addPropertyTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onPropertyTypeChanged(checkedId)
        }

        // Date picker for sale status
        val materialDateBuilder: MaterialDatePicker.Builder<*> =
            MaterialDatePicker.Builder.datePicker()
        val materialDatePicker: MaterialDatePicker<*> = materialDateBuilder.build()

        binding.addPropertyDateTextInputEditText.setOnClickListener {
            materialDatePicker.show(childFragmentManager, "DATE_PICKER")
        }

        materialDatePicker.addOnPositiveButtonClickListener {
            viewModel.onDateChanged(it)
            binding.addPropertyDateTextInputEditText.setText(materialDatePicker.headerText)
        }

        // Sale status toggle
        binding.addPropertyTypeSaleStatusToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onSaleStatusChanged(isChecked)
        }

        // Surface plus minus view
        binding.addPropertySurfacePlusMinusView.getValueEditText()
            .setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Detects when layout is ready to be drawn
                    binding.root.viewTreeObserver.addOnPreDrawListener(object :
                        ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            // Removes listener to prevent repeated calls
                            binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                            // Selects all text
                            binding.addPropertySurfacePlusMinusView.getValueEditText().selectAll()
                            return true
                        }
                    })
                }
            }

        // Disable surface minus button when value is 0
        binding.addPropertySurfacePlusMinusView.getValueEditText().doAfterTextChanged { editable ->
            val surfaceText = editable?.toString()
            val surface = surfaceText?.toDoubleOrNull() ?: 0.0

            if (surfaceText.isNullOrEmpty()) {
                binding.addPropertySurfacePlusMinusView.setValueEditText("0")
            } else {
                binding.addPropertySurfacePlusMinusView.isEnabled = surface != 0.0
                binding.addPropertySurfacePlusMinusView.decrementButton().alpha = if (surface == 0.0) 0.5f else 1f

                viewModel.onSurfaceValueChanged(surface)
            }
        }

        binding.addPropertySurfacePlusMinusView.decrementButton().setOnClickListener {
            viewModel.decrementSurface(binding.addPropertySurfacePlusMinusView.getDoubleValue())
        }

        binding.addPropertySurfacePlusMinusView.incrementButton().setOnClickListener {
            viewModel.incrementSurface(binding.addPropertySurfacePlusMinusView.getDoubleValue())
        }

        // Rooms plus minus view
        binding.addPropertyRoomsPlusMinusView.getValueEditText()
            .setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Detects when layout is ready to be drawn
                    binding.root.viewTreeObserver.addOnPreDrawListener(object :
                        ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            // Removes listener to prevent repeated calls
                            binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                            // Selects all text
                            binding.addPropertyRoomsPlusMinusView.getValueEditText().selectAll()
                            return true
                        }
                    })
                }
            }

        // Disable rooms minus button when value is 0
        binding.addPropertyRoomsPlusMinusView.getValueEditText().doAfterTextChanged { editable ->
            val roomsText = editable?.toString()
            val rooms = roomsText?.toIntOrNull() ?: 0

            if (roomsText.isNullOrEmpty()) {
                binding.addPropertyRoomsPlusMinusView.setValueEditText("0")
            } else {
                binding.addPropertyRoomsPlusMinusView.isEnabled = rooms != 0
                binding.addPropertyRoomsPlusMinusView.decrementButton().alpha =
                    if (rooms == 0) 0.5f else 1f

                viewModel.onRoomsValueChanged(rooms)
            }
        }

        binding.addPropertyRoomsPlusMinusView.decrementButton().setOnClickListener {
            viewModel.decrementRooms(binding.addPropertyRoomsPlusMinusView.getIntValue())
        }

        binding.addPropertyRoomsPlusMinusView.incrementButton().setOnClickListener {
            viewModel.incrementRooms(binding.addPropertyRoomsPlusMinusView.getIntValue())
        }

        // Bathrooms plus minus view
        binding.addPropertyBathroomsPlusMinusView.getValueEditText()
            .setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Detects when layout is ready to be drawn
                    binding.root.viewTreeObserver.addOnPreDrawListener(object :
                        ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            // Removes listener to prevent repeated calls
                            binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                            // Selects all text
                            binding.addPropertyBathroomsPlusMinusView.getValueEditText().selectAll()
                            return true
                        }
                    })
                }
            }

        // Disable bathrooms minus button when value is 0
        binding.addPropertyBathroomsPlusMinusView.getValueEditText()
            .doAfterTextChanged { editable ->
                val bathroomsText = editable?.toString()
                val bathrooms = bathroomsText?.toIntOrNull() ?: 0

                if (bathroomsText.isNullOrEmpty()) {
                    binding.addPropertyBathroomsPlusMinusView.setValueEditText("0")
                } else {
                    binding.addPropertyBathroomsPlusMinusView.isEnabled = bathrooms != 0
                    binding.addPropertyBathroomsPlusMinusView.decrementButton().alpha =
                        if (bathrooms == 0) 0.5f else 1f

                    viewModel.onBathroomsValueChanged(bathrooms)
                }
            }

        binding.addPropertyBathroomsPlusMinusView.decrementButton().setOnClickListener {
            viewModel.decrementBathrooms(binding.addPropertyBathroomsPlusMinusView.getIntValue())
        }

        binding.addPropertyBathroomsPlusMinusView.incrementButton().setOnClickListener {
            viewModel.incrementBathrooms(binding.addPropertyBathroomsPlusMinusView.getIntValue())
        }

        // Bedrooms plus minus view
        binding.addPropertyBedroomsPlusMinusView.getValueEditText()
            .setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Detects when layout is ready to be drawn
                    binding.root.viewTreeObserver.addOnPreDrawListener(object :
                        ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            // Removes listener to prevent repeated calls
                            binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                            // Selects all text
                            binding.addPropertyBedroomsPlusMinusView.getValueEditText().selectAll()
                            return true
                        }
                    })
                }
            }

        // Disable bedrooms minus button when value is 0
        binding.addPropertyBedroomsPlusMinusView.getValueEditText().doAfterTextChanged { editable ->
            val bedroomsText = editable?.toString()
            val bedrooms = bedroomsText?.toIntOrNull() ?: 0

            if (bedroomsText.isNullOrEmpty()) {
                binding.addPropertyBedroomsPlusMinusView.setValueEditText("0")
            } else {
                binding.addPropertyBedroomsPlusMinusView.isEnabled = bedrooms != 0
                binding.addPropertyBedroomsPlusMinusView.decrementButton().alpha =
                    if (bedrooms == 0) 0.5f else 1f

                viewModel.onBedroomsValueChanged(bedrooms)
            }
        }

        // Hides keyboard and clear focus when done is pressed
        binding.addPropertyBedroomsPlusMinusView.getValueEditText().imeOptions = EditorInfo.IME_ACTION_DONE
        binding.addPropertyBedroomsPlusMinusView.getValueEditText()
            .setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    binding.addPropertyBedroomsPlusMinusView.getValueEditText().clearFocus()
                    hideKeyboard()
                    true
                } else {
                    false
                }
            }

        binding.addPropertyBedroomsPlusMinusView.decrementButton().setOnClickListener {
            viewModel.decrementBedrooms(binding.addPropertyBedroomsPlusMinusView.getIntValue())
        }

        binding.addPropertyBedroomsPlusMinusView.incrementButton().setOnClickListener {
            viewModel.incrementBedrooms(binding.addPropertyBedroomsPlusMinusView.getIntValue())
        }

        // Chip listeners
        val poiChips = listOf(
            binding.addPropertyAmenitiesSchoolChip,
            binding.addPropertyAmenitiesStoreChip,
            binding.addPropertyAmenitiesParkChip,
            binding.addPropertyAmenitiesRestaurantChip,
            binding.addPropertyAmenitiesHospitalChip,
            binding.addPropertyTransportationBusChip,
            binding.addPropertyTransportationSubwayChip,
            binding.addPropertyTransportationTramwayChip,
            binding.addPropertyTransportationTrainChip,
            binding.addPropertyTransportationAirportChip
        )

        poiChips.forEach {
            it.setOnCheckedChangeListener { chip, isChecked ->
                viewModel.onChipCheckedChanged(chip, isChecked)
            }
        }

        // Address text input
        binding.addPropertyAddressTextInputLayout.isEndIconVisible = false
        binding.addPropertyAddressTextInputEditText.doAfterTextChanged { editable ->
            val address = editable?.toString() ?: ""

            binding.addPropertyAddressTextInputLayout.isEndIconVisible = address.isNotEmpty()
            viewModel.onAddressChanged(address)
        }

        binding.addPropertyAddressTextInputLayout.setEndIconOnClickListener {
            viewModel.onAddressTextCleared()
            binding.addPropertyAddressTextInputEditText.setText("")
            binding.addPropertyComplementaryAddressTextInputEditText.setText("")
        }

        // Hides suggestions if user click on the close button
        binding.addPropertyAddressSuggestionsCloseButton.setOnClickListener {
            binding.addPropertyAddressSuggestionsContainer.visibility = View.GONE
        }

        // Hides suggestions if user click on the root view
        binding.root.setOnClickListener {
            binding.addPropertyAddressSuggestionsContainer.visibility = View.GONE
        }

        // Complementary address text input
        binding.addPropertyComplementaryAddressTextInputLayout.isEndIconVisible = false
        binding.addPropertyComplementaryAddressTextInputEditText.doAfterTextChanged { editable ->
            val complementaryAddress = editable?.toString() ?: ""

            binding.addPropertyComplementaryAddressTextInputLayout.isEndIconVisible = complementaryAddress.isNotEmpty()
            viewModel.onComplementaryAddressChanged(complementaryAddress)
        }

        binding.addPropertyComplementaryAddressTextInputLayout.setEndIconOnClickListener {
            viewModel.onComplementaryAddressTextCleared()
            binding.addPropertyComplementaryAddressTextInputEditText.setText("")
        }

        // Description text input
        binding.addPropertyDescriptionTextInputLayout.isEndIconVisible = false
        binding.addPropertyDescriptionTextInputEditText.doAfterTextChanged { editable ->
            val description = editable?.toString() ?: ""

            binding.addPropertyDescriptionTextInputLayout.isEndIconVisible = description.isNotEmpty()
            viewModel.onDescriptionChanged(description)
        }

        binding.addPropertyDescriptionTextInputLayout.setEndIconOnClickListener {
            viewModel.onDescriptionTextCleared()
            binding.addPropertyDescriptionTextInputEditText.setText("")
        }

        // Camera
        binding.addPropertyCameraImageButton.setOnClickListener { onCameraButtonClicked() }

        // Done button
        binding.addPropertyDoneFab.setOnClickListener { viewModel.onDoneButtonClicked() }

        viewModel.viewStateLiveData.observe(viewLifecycleOwner) {
            binding.addPropertyDateTextInputLayout.hint = it.dateHint
            binding.addPropertyPriceTextInputLayout.hint = it.priceHint
            binding.addPropertySurfacePlusMinusView.setLabel(it.surfaceLabel)
            if (binding.addPropertySurfacePlusMinusView.getFormattedDoubleValue() != it.surface) {
                binding.addPropertySurfacePlusMinusView.setValueEditText(it.surface)
            }
            if (binding.addPropertyRoomsPlusMinusView.getFormattedIntValue() != it.numberOfRooms) {
                binding.addPropertyRoomsPlusMinusView.setValueEditText(it.numberOfRooms)
            }
            if (binding.addPropertyBathroomsPlusMinusView.getFormattedIntValue() != it.numberOfBathrooms) {
                binding.addPropertyBathroomsPlusMinusView.setValueEditText(it.numberOfBathrooms)
            }
            if (binding.addPropertyBedroomsPlusMinusView.getFormattedIntValue() != it.numberOfBedrooms) {
                binding.addPropertyBedroomsPlusMinusView.setValueEditText(it.numberOfBedrooms)
            }

            // Updates address fields on autocomplete selection
            if (it.address != null && it.address != binding.addPropertyAddressTextInputEditText.text.toString()) {
                viewModel.onAddressTextUpdatedByAutocomplete()
                binding.addPropertyAddressTextInputEditText.setText(it.address)
            }

            // Hides suggestions when a suggestion is selected
            if (it.addressPredictions.isEmpty() || (it.address != null && it.address == binding.addPropertyAddressTextInputEditText.text.toString())) {
                binding.addPropertyAddressSuggestionsContainer.visibility = View.GONE
            } else {
                binding.addPropertyAddressSuggestionsContainer.visibility = View.VISIBLE
            }
            suggestionsRVAdapter.submitList(it.addressPredictions)

            binding.addPropertyStateRegionTextInputEditText.setText(it.state ?: "")
            binding.addPropertyCityTextInputEditText.setText(it.city ?: "")
            binding.addPropertyZipcodeTextInputEditText.setText(it.zipcode ?: "")
        }

        viewModel.viewActionLiveData.observeEvent(viewLifecycleOwner) {
            Log.d("test", "add property fragment observed event: $it")
            when (it) {
                AddPropertyViewAction.HideSuggestions -> {
                    binding.addPropertyAddressSuggestionsContainer.visibility = View.GONE
                    binding.addPropertyAddressTextInputEditText.clearFocus()
                    hideKeyboard()
                }

                AddPropertyViewAction.OpenCamera -> {
                    startActivity(Intent(requireContext(), CameraActivity::class.java))
                }

                AddPropertyViewAction.CloseDialog -> dismiss()

                AddPropertyViewAction.OpenSettings -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        viewModel.onCancel()
    }

    private fun activityResultLauncher() =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.onCameraPermissionGranted()
            } else {
                Snackbar
                    .make(binding.root, getString(R.string.add_property_camera_permission_denied), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.snackbar_settings)) {
                        onChangeSettingsClicked()
                    }.show()
            }
        }

    private fun onChangeSettingsClicked() = viewModel.onChangeSettingsClicked()

    private fun onCameraButtonClicked() {
        when {
            // If permission already granted, start camera activity
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.onCameraPermissionGranted()
            }

            // If permission denied but not permanently, show rationale
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA) -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.dialog_permission_required))
                    .setMessage(getString(R.string.add_property_camera_dialog_message))
                    .setPositiveButton(getString(R.string.snackbar_settings)) { _, _ ->
                        onChangeSettingsClicked()
                    }
                    .setNegativeButton(getString(R.string.dialog_dismiss), null)
                    .show()
            }

            // Else, request permission
            else -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

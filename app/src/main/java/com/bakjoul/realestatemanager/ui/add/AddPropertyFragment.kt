package com.bakjoul.realestatemanager.ui.add

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
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
import java.math.BigDecimal
import java.text.DecimalFormat

@AndroidEntryPoint
class AddPropertyFragment : DialogFragment(R.layout.fragment_add_property) {

    private companion object {
        private const val DIALOG_WINDOW_WIDTH = 0.5
        private const val DIALOG_WINDOW_HEIGHT = 0.9
    }

    private val binding by viewBinding { FragmentAddPropertyBinding.bind(it) }
    private val viewModel by viewModels<AddPropertyViewModel>()

    private val requestCameraPermissionLauncher = activityResultLauncher()
    private val materialDateBuilder: MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()
    private var currentCurrency: DecimalFormat? = null

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
            dialog?.window?.setWindowAnimations(R.style.SlideInBottomAnimation)
        } else {
            dialog?.window?.setWindowAnimations(R.style.SlideInRightAnimation)
        }

        setToolbar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val suggestionsAdapter = AddPropertySuggestionAdapter()
        binding.addPropertyAddressSuggestionsRecyclerView.adapter = suggestionsAdapter
        val suggestionsDivider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        suggestionsDivider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.suggestions_divider)!!)
        binding.addPropertyAddressSuggestionsRecyclerView.addItemDecoration(suggestionsDivider)

        val photosAdapter = AddPropertyPhotoAdapter()
        binding.addPropertyPhotosRecyclerView?.adapter = photosAdapter  // TODO update other layout
        val photosDivider = DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL)
        photosDivider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.photos_divider)!!)
        binding.addPropertyPhotosRecyclerView?.addItemDecoration(photosDivider)

        // Property type radio group
        binding.addPropertyTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onPropertyTypeChanged(checkedId)
        }

        // Date picker for sale since
        val forSaleSinceDatePicker: MaterialDatePicker<*> = materialDateBuilder.build()

        binding.addPropertyForSaleSinceTextInputEditText.setOnClickListener {
            forSaleSinceDatePicker.show(childFragmentManager, "FOR_SALE_DATE_PICKER")
        }

        forSaleSinceDatePicker.addOnPositiveButtonClickListener {
            viewModel.onForSaleSinceDateChanged(it)
            binding.addPropertyForSaleSinceTextInputEditText.setText(forSaleSinceDatePicker.headerText)
        }

        // Sale status toggle
        binding.addPropertyTypeSaleStatusToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onSaleStatusChanged(isChecked)
        }

        // Price text input end icon
        binding.addPropertyPriceTextInputLayout.isEndIconVisible = false
        binding.addPropertyPriceTextInputLayout.setEndIconOnClickListener {
            viewModel.onPriceTextCleared()
            binding.addPropertyPriceTextInputEditText.setText("")
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
        binding.addPropertySurfacePlusMinusView.getValueEditText()
            .doAfterTextChanged { editable ->
                val surfaceText = editable?.toString()
                val surface = surfaceText?.toBigDecimalOrNull() ?: BigDecimal.ZERO

                if (surfaceText.isNullOrEmpty()) {
                    binding.addPropertySurfacePlusMinusView.setValueEditText("0")
                } else {
                    binding.addPropertySurfacePlusMinusView.decrementButton().isEnabled =
                        surface != BigDecimal.ZERO
                    binding.addPropertySurfacePlusMinusView.decrementButton().alpha =
                        if (surface == BigDecimal.ZERO) 0.5f else 1f

                    viewModel.onSurfaceValueChanged(surface)
                }
            }

        binding.addPropertySurfacePlusMinusView.decrementButton().setOnClickListener {
            viewModel.decrementSurface(binding.addPropertySurfacePlusMinusView.getBigDecimalValue())
        }

        binding.addPropertySurfacePlusMinusView.incrementButton().setOnClickListener {
            viewModel.incrementSurface(binding.addPropertySurfacePlusMinusView.getBigDecimalValue())
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
        binding.addPropertyRoomsPlusMinusView.getValueEditText()
            .doAfterTextChanged { editable ->
                val roomsText = editable?.toString()
                val rooms = roomsText?.toIntOrNull() ?: 0

                if (roomsText.isNullOrEmpty()) {
                    binding.addPropertyRoomsPlusMinusView.setValueEditText("0")
                } else {
                    binding.addPropertyRoomsPlusMinusView.decrementButton().isEnabled = rooms != 0
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
                    binding.addPropertyBathroomsPlusMinusView.decrementButton().isEnabled = bathrooms != 0
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
        binding.addPropertyBedroomsPlusMinusView.getValueEditText()
            .doAfterTextChanged { editable ->
                val bedroomsText = editable?.toString()
                val bedrooms = bedroomsText?.toIntOrNull() ?: 0

                if (bedroomsText.isNullOrEmpty()) {
                    binding.addPropertyBedroomsPlusMinusView.setValueEditText("0")
                } else {
                    binding.addPropertyBedroomsPlusMinusView.decrementButton().isEnabled = bedrooms != 0
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
        listOf(
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
        ).forEach {
            it.setOnCheckedChangeListener { chip, isChecked ->
                viewModel.onChipCheckedChanged(chip.toString(), isChecked)
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

        viewModel.viewStateLiveData.observe(viewLifecycleOwner) { viewState ->

            // Sale date picker
            if (viewState.isSold) {
                binding.addPropertySoldOnTextInputLayout.visibility = View.VISIBLE
                val soldOnDatePicker: MaterialDatePicker<*> = materialDateBuilder.build()

                binding.addPropertySoldOnTextInputEditText.setOnClickListener {
                    soldOnDatePicker.show(childFragmentManager, "SOLD_ON_DATE_PICKER")
                }

                soldOnDatePicker.addOnPositiveButtonClickListener {
                    viewModel.onSoldOnDateChanged(it)
                    binding.addPropertySoldOnTextInputEditText.setText(soldOnDatePicker.headerText)
                }
            } else {
                binding.addPropertySoldOnTextInputLayout.visibility = View.GONE
            }

            // Price formatting
            binding.addPropertyPriceTextInputLayout.hint = viewState.priceHint
            if (currentCurrency != viewState.currencyFormat) {
                binding.addPropertyPriceTextInputEditText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        s?.let { price ->
                            val originalText = price.toString()
                            binding.addPropertyPriceTextInputLayout.isEndIconVisible = originalText.isNotEmpty()

                            if (originalText.isNotEmpty()) {
                                val bigDecimalPrice = BigDecimal(originalText.replace(",", "").replace(" ", ""))
                                viewModel.onPriceChanged(bigDecimalPrice)

                                try {
                                    val parsed = viewState.currencyFormat.parse(originalText)
                                    val formatted = viewState.currencyFormat.format(parsed)
                                    if (formatted != originalText) {
                                        binding.addPropertyPriceTextInputEditText.removeTextChangedListener(this)
                                        binding.addPropertyPriceTextInputEditText.setText(formatted)
                                        binding.addPropertyPriceTextInputEditText.setSelection(formatted.length)
                                        binding.addPropertyPriceTextInputEditText.addTextChangedListener(this)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                })
            }
            currentCurrency = viewState.currencyFormat

            binding.addPropertySurfacePlusMinusView.setLabel(viewState.surfaceLabel)
            if (binding.addPropertySurfacePlusMinusView.getFormattedBigDecimalValue() != viewState.surface) {
                binding.addPropertySurfacePlusMinusView.setValueEditText(viewState.surface)
            }
            if (binding.addPropertyRoomsPlusMinusView.getFormattedIntValue() != viewState.numberOfRooms) {
                binding.addPropertyRoomsPlusMinusView.setValueEditText(viewState.numberOfRooms)
            }
            if (binding.addPropertyBathroomsPlusMinusView.getFormattedIntValue() != viewState.numberOfBathrooms) {
                binding.addPropertyBathroomsPlusMinusView.setValueEditText(viewState.numberOfBathrooms)
            }
            if (binding.addPropertyBedroomsPlusMinusView.getFormattedIntValue() != viewState.numberOfBedrooms) {
                binding.addPropertyBedroomsPlusMinusView.setValueEditText(viewState.numberOfBedrooms)
            }

            // Updates address fields on autocomplete selection
            if (viewState.address != null && viewState.address != binding.addPropertyAddressTextInputEditText.text.toString()) {
                viewModel.onAddressTextUpdatedByAutocomplete()
                binding.addPropertyAddressTextInputEditText.setText(viewState.address)
            }

            // Hides suggestions when a suggestion is selected
            if (viewState.addressPredictions.isEmpty() || (viewState.address != null && viewState.address == binding.addPropertyAddressTextInputEditText.text.toString())) {
                binding.addPropertyAddressSuggestionsContainer.visibility = View.GONE
            } else {
                binding.addPropertyAddressSuggestionsContainer.visibility = View.VISIBLE
            }
            suggestionsAdapter.submitList(viewState.addressPredictions)

            binding.addPropertyStateRegionTextInputEditText.setText(viewState.state ?: "")
            binding.addPropertyCityTextInputEditText.setText(viewState.city ?: "")
            binding.addPropertyZipcodeTextInputEditText.setText(viewState.zipcode ?: "")

            photosAdapter.submitList(viewState.photos)
            isUpdating = false
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

        viewModel.closeDialog()
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

    private fun setToolbar() {
        val toolbar = binding.addPropertyToolbar
        toolbar?.setTitle(R.string.add_property_title)

        toolbar?.setNavigationOnClickListener { viewModel.closeDialog() }
        binding.addPropertyAppbarCloseButton?.setOnClickListener { viewModel.closeDialog() }
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

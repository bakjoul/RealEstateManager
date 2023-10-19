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
import android.widget.Toast
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

        val photosDivider = DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL)
        photosDivider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.photos_divider)!!)
        binding.addPropertyPhotoListView.addItemDecoration(photosDivider)

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
        binding.addPropertySurfacePlusMinusView.addOnValueChangedListener { newValue ->
            viewModel.onSurfaceChanged(newValue)
        }

        // Rooms plus minus views
        binding.addPropertyRoomsPlusMinusView.addOnValueChangedListener { newValue ->
            viewModel.onRoomsCountChanged(newValue)
        }

        // Bathrooms plus minus views
        binding.addPropertyBathroomsPlusMinusView.addOnValueChangedListener { newValue ->
            viewModel.onBathroomsCountChanged(newValue)
        }

        // Bedrooms plus minus views
        binding.addPropertyBedroomsPlusMinusView.addOnValueChangedListener { newValue ->
            viewModel.onBedroomsCountChanged(newValue)
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
                viewModel.onChipCheckedChanged(chip.text.toString(), isChecked)
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

            // Updates address fields on autocomplete selection
            if (viewState.address != null && viewState.address != binding.addPropertyAddressTextInputEditText.text.toString()) {
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

            binding.addPropertyPhotoListView.bind(viewState.photos)
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

                is AddPropertyViewAction.ShowToast -> Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
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

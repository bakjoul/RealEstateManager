package com.bakjoul.realestatemanager.ui.add

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentAddPropertyBinding
import com.bakjoul.realestatemanager.ui.utils.hideKeyboard
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPropertyFragment : Fragment(R.layout.fragment_add_property) {

    private val binding by viewBinding { FragmentAddPropertyBinding.bind(it) }
    private val viewModel by viewModels<AddPropertyViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Property type radio group
        binding.addPropertyTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onPropertyTypeChanged(checkedId)
        }

        // Date picker for sale status
        val materialDateBuilder: MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()
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
        binding.addPropertySurfacePlusMinusView.getValueEditText().setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Detects when layout is ready to be drawn
                binding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
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

        // Disable minus button when value is 0
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
        binding.addPropertyRoomsPlusMinusView.getValueEditText().setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Detects when layout is ready to be drawn
                binding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
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

        // Disable minus button when value is 0
        binding.addPropertyRoomsPlusMinusView.getValueEditText().doAfterTextChanged { editable ->
            val roomsText = editable?.toString()
            val rooms = roomsText?.toIntOrNull() ?: 0

            if (roomsText.isNullOrEmpty()) {
                binding.addPropertyRoomsPlusMinusView.setValueEditText("0")
            } else {
                binding.addPropertyRoomsPlusMinusView.isEnabled = rooms != 0
                binding.addPropertyRoomsPlusMinusView.decrementButton().alpha = if (rooms == 0) 0.5f else 1f

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
        binding.addPropertyBathroomsPlusMinusView.getValueEditText().setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Detects when layout is ready to be drawn
                binding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
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

        // Disable minus button when value is 0
        binding.addPropertyBathroomsPlusMinusView.getValueEditText().doAfterTextChanged { editable ->
            val bathroomsText = editable?.toString()
            val bathrooms = bathroomsText?.toIntOrNull() ?: 0

            if (bathroomsText.isNullOrEmpty()) {
                binding.addPropertyBathroomsPlusMinusView.setValueEditText("0")
            } else {
                binding.addPropertyBathroomsPlusMinusView.isEnabled = bathrooms != 0
                binding.addPropertyBathroomsPlusMinusView.decrementButton().alpha = if (bathrooms == 0) 0.5f else 1f

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
        binding.addPropertyBedroomsPlusMinusView.getValueEditText().setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Detects when layout is ready to be drawn
                binding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
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

        // Disable minus button when value is 0
        binding.addPropertyBedroomsPlusMinusView.getValueEditText().doAfterTextChanged { editable ->
            val bedroomsText = editable?.toString()
            val bedrooms = bedroomsText?.toIntOrNull() ?: 0

            if (bedroomsText.isNullOrEmpty()) {
                binding.addPropertyBedroomsPlusMinusView.setValueEditText("0")
            } else {
                binding.addPropertyBedroomsPlusMinusView.isEnabled = bedrooms != 0
                binding.addPropertyBedroomsPlusMinusView.decrementButton().alpha = if (bedrooms == 0) 0.5f else 1f

                viewModel.onBedroomsValueChanged(bedrooms)
            }
        }

        binding.addPropertyBedroomsPlusMinusView.getValueEditText().imeOptions = EditorInfo.IME_ACTION_DONE

        binding.addPropertyBedroomsPlusMinusView.getValueEditText().setOnEditorActionListener { _, actionId, _ ->
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

        binding.addPropertyAddressTextInputEditText.doAfterTextChanged { editable ->
            val address = editable?.toString() ?: ""
            viewModel.onAddressChanged(address)
        }

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

            Log.d("test", "onViewCreated: ${it.addressPredictions}")
        }
    }
}

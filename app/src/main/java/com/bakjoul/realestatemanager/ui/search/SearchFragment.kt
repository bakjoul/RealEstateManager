package com.bakjoul.realestatemanager.ui.search

import android.app.Dialog
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.databinding.FragmentSearchBinding
import com.bakjoul.realestatemanager.domain.search.model.SearchDurationUnit
import com.bakjoul.realestatemanager.ui.utils.CustomBottomSheetDialog
import com.bakjoul.realestatemanager.ui.utils.hideKeyboard
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BottomSheetDialogFragment(R.layout.fragment_search) {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private val binding by viewBinding { FragmentSearchBinding.bind(it) }
    private val viewModel by viewModels<SearchViewModel>()

    override fun onStart() {
        super.onStart()

        val behavior = (dialog as BottomSheetDialog).behavior
        setPinnedBottomView(behavior)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : CustomBottomSheetDialog(requireContext()) {
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                if (currentFocus != null) {
                    val focusedViewRect = Rect()
                    currentFocus!!.getGlobalVisibleRect(focusedViewRect)
                    val x = ev.x.toInt()
                    val y = ev.y.toInt()
                    if (!focusedViewRect.contains(x, y)) {
                        hideKeyboard()
                        currentFocus!!.clearFocus()
                    }
                }
                return super.dispatchTouchEvent(ev)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchCloseButton.setOnClickListener { dismiss() }  // TODO refactor later
        binding.searchApplyButton.setOnClickListener { viewModel.onApplyButtonClicked() }
        binding.searchResetButton.setOnClickListener { Log.d("test", "reset button clicked") }

        binding.searchLocationTextInputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.view_spinner_item,
            resources.getStringArray(R.array.search_date_duration_unit_options)
        )
        binding.searchDateUnitAutoCompleteTextView.setAdapter(adapter)
        binding.searchDateUnitAutoCompleteTextView.threshold = (Integer.MAX_VALUE)
        binding.searchDateDurationTextInputEditText.transformationMethod = null

        // Status button toggle group
        binding.searchStatusButtonToggleGroup.addOnButtonCheckedListener { buttonGroup, buttonId, isChecked ->
            if (isChecked) {
                viewModel.onStatusChanged(buttonId)
                binding.root.findViewById<MaterialButton>(buttonGroup.checkedButtonId).isClickable = false
            } else {
                buttonGroup.children.forEach {
                    if (it.id != buttonGroup.checkedButtonId) {
                        it.isClickable = true
                    }
                }
            }
        }

        // Duration text input
        binding.searchDateDurationTextInputLayout.isEndIconVisible = false
        binding.searchDateDurationTextInputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Detects when the view is ready to be drawn
                binding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        // Removes the listener to avoid multiple calls
                        binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                        // Selects all text in the EditText
                        binding.searchDateDurationTextInputEditText.selectAll()
                        return true
                    }
                })
            }
        }
        binding.searchDateDurationTextInputEditText.doAfterTextChanged {
            val duration = it.toString().toIntOrNull()

            binding.searchDateDurationTextInputLayout.isEndIconVisible = duration != null
            viewModel.onDurationChanged(duration)
        }
        binding.searchDateDurationTextInputLayout.setEndIconOnClickListener {
            binding.searchDateDurationTextInputEditText.text = null
        }

        // Duration unit
        binding.searchDateUnitAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            viewModel.onDurationUnitChanged(SearchDurationUnit.values()[position])
        }

        // Type
        setChipGroupListeners(
            binding.searchTypeChipGroup,
            binding.searchTypeResetButton,
            viewModel::onTypeChipCheckedChanged
        )

        // Price
        binding.searchPriceRangeSliderView.addOnRangeChangedListener {
            viewModel.onPriceRangeChanged(it)
        }

        // Surface
        binding.searchSurfaceRangeSliderView.addOnRangeChangedListener {
            viewModel.onSurfaceRangeChanged(it)
        }

        // Rooms
        binding.searchRoomsPlusMinusView.addOnValueChangedListener { newValue ->
            viewModel.onRoomsCountChanged(newValue)
        }

        // Bathrooms
        binding.searchBathroomsPlusMinusView.addOnValueChangedListener { newValue ->
            viewModel.onBathroomsCountChanged(newValue)
        }

        // Bedrooms
        binding.searchBedroomsPlusMinusView.addOnValueChangedListener { newValue ->
            viewModel.onBedroomsCountChanged(newValue)
        }

        // Amenities
        setChipGroupListeners(
            binding.searchAmenitiesChipGroup,
            binding.searchAmenitiesResetButton,
            viewModel::onPoiChipCheckedChanged
        )

        // Transportation
        setChipGroupListeners(
            binding.searchTransportationChipGroup,
            binding.searchTransportationResetButton,
            viewModel::onPoiChipCheckedChanged
        )

        viewModel.viewStateLiveData.observe(viewLifecycleOwner) { viewState ->
            // Price range slider
            binding.searchPriceRangeSliderView.setTitle(viewState.priceLabel.toCharSequence(requireContext()).toString())
            binding.searchPriceRangeSliderView.setValues(viewState.minPrice, viewState.maxPrice)
            binding.searchPriceRangeSliderView.setLabelFormatter {
                if (viewState.currency == AppCurrency.EUR) {
                    "${it.toInt()}${viewState.priceLabelFormatter.toCharSequence(requireContext())}"
                } else {
                    "${viewState.priceLabelFormatter.toCharSequence(requireContext())}${it.toInt()}"
                }
            }
            binding.searchPriceRangeSliderView.setMinValueHelperText(viewState.minPriceHelperText.toCharSequence(requireContext()).toString())
            binding.searchPriceRangeSliderView.setMaxValueHelperText(viewState.maxPriceHelperText.toCharSequence(requireContext()).toString())

            // Surface range slider
            binding.searchSurfaceRangeSliderView.setTitle(viewState.surfaceLabel.toCharSequence(requireContext()).toString())
            binding.searchSurfaceRangeSliderView.setValues(viewState.minSurface, viewState.maxSurface)
            binding.searchSurfaceRangeSliderView.setLabelFormatter {
                "${it.toInt()}${viewState.surfaceLabelFormatter.toCharSequence(requireContext())}"
            }
            binding.searchSurfaceRangeSliderView.setMinValueHelperText(viewState.minSurfaceHelperText.toCharSequence(requireContext()).toString())
            binding.searchSurfaceRangeSliderView.setMaxValueHelperText(viewState.maxSurfaceHelperText.toCharSequence(requireContext()).toString())
        }
    }

    private fun setPinnedBottomView(behavior: BottomSheetBehavior<FrameLayout>) {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        binding.searchBottomLinearLayout.setBackgroundResource(typedValue.resourceId)

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.searchBottomLinearLayout.y =
                    ((bottomSheet.parent as View).height - bottomSheet.top - binding.searchBottomLinearLayout.height).toFloat()
            }
        }.apply {
            binding.root.post {
                onSlide(binding.root.parent as View, 0f)
            }
        })
    }

    private fun setChipGroupListeners(
        chipGroup: ChipGroup,
        resetButton: Button,
        updateValues: (Int, Boolean) -> Unit
    ) {
        chipGroup.children.forEach { it as Chip
            it.setOnCheckedChangeListener { chip, isChecked ->
                updateValues(chip.id, isChecked)
            }
        }
        resetButton.setOnClickListener {
            chipGroup.clearCheck()
            resetButton.visibility = View.GONE
        }
        chipGroup.setOnCheckedStateChangeListener { group, _ ->
            if (group.checkedChipIds.size > 0) {
                resetButton.visibility = View.VISIBLE
            } else {
                resetButton.visibility = View.GONE
            }
        }
    }
}

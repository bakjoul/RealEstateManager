package com.bakjoul.realestatemanager.ui.search

import android.app.Dialog
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentSearchBinding
import com.bakjoul.realestatemanager.ui.utils.CustomBottomSheetDialog
import com.bakjoul.realestatemanager.ui.utils.hideKeyboard
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SearchFragment : BottomSheetDialogFragment(R.layout.fragment_search) {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private val binding by viewBinding { FragmentSearchBinding.bind(it) }

    override fun onStart() {
        super.onStart()

        val behavior = (dialog as BottomSheetDialog).behavior
        behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
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
        binding.searchApplyButton.setOnClickListener { Log.d("test", "apply button clicked") }
        binding.searchResetButton.setOnClickListener { Log.d("test", "reset button clicked") }

        binding.searchLocationTextInputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        binding.searchPriceRangeSliderView.setValues(100000f, 500000f)
        binding.searchPriceRangeSliderView.setLabelFormatter { "${it.toInt()} €" }
        binding.searchPriceRangeSliderView.setMinValueHelperText("Min.: 1000000")
        binding.searchPriceRangeSliderView.setMaxValueHelperText("Max.: 5000000")

        binding.searchSurfaceRangeSliderView.setValues(50f, 200f)
        binding.searchSurfaceRangeSliderView.setLabelFormatter { "${it.toInt()} sq m" }
        binding.searchSurfaceRangeSliderView.setMinValueHelperText("Min.: 50")
        binding.searchSurfaceRangeSliderView.setMaxValueHelperText("Max.: 200")

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.view_spinner_item,
            resources.getStringArray(R.array.search_date_duration_unit_options)
        )
        binding.searchDateUnitAutoCompleteTextView.setAdapter(adapter)
        binding.searchDateUnitAutoCompleteTextView.threshold = (Integer.MAX_VALUE)
        binding.searchDateDurationTextInputEditText.transformationMethod = null
    }

    private fun setPinnedBottomView(behavior: BottomSheetBehavior<FrameLayout>) {
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
}

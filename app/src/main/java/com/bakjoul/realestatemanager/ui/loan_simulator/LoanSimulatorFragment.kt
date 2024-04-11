package com.bakjoul.realestatemanager.ui.loan_simulator

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.loan_simulator.model.DurationUnit
import com.bakjoul.realestatemanager.databinding.FragmentLoanSimulatorBinding
import com.bakjoul.realestatemanager.ui.utils.CustomThemeDialog
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.PointBeforeNumberFilter
import com.bakjoul.realestatemanager.ui.utils.hideKeyboard
import com.bakjoul.realestatemanager.ui.utils.showAsToast
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

@AndroidEntryPoint
class LoanSimulatorFragment : DialogFragment(R.layout.fragment_loan_simulator) {

    companion object {
        private const val PORTRAIT_DIALOG_WINDOW_WIDTH =  0.9
        private const val LANDSCAPE_DIALOG_WINDOW_WIDTH = 0.5
    }

    private val binding by viewBinding { FragmentLoanSimulatorBinding.bind(it) }
    private val viewModel by viewModels<LoanSimulatorViewModel>()
    private val clipboardManager by lazy { requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    private var currentCurrency: DecimalFormat? = null
    private var isMonthlyResultsCardExpanded = false
    private var isYearlyResultsCardExpanded = false
    private var isTotalResultsCardExpanded = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : CustomThemeDialog(requireContext(), R.style.FloatingDialog) {
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

    override fun onStart() {
        super.onStart()

        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        if (resources.getBoolean(R.bool.isTablet)) {
            val width = (resources.displayMetrics.widthPixels * LANDSCAPE_DIALOG_WINDOW_WIDTH).toInt()
            dialog?.window?.setLayout(width, height)
        } else {
            val width = (resources.displayMetrics.widthPixels * PORTRAIT_DIALOG_WINDOW_WIDTH).toInt()
            dialog?.window?.setLayout(width, height)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loanSimulatorAppbarCloseButton.setOnClickListener {
            viewModel.onCloseButtonClicked()
        }

        binding.loanSimulatorDurationUnitAutoCompleteTextView.setText(getString(DurationUnit.YEARS.unitName), false)
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.fragment_loan_simulator_duration_unit_spinner_item,
            resources.getStringArray(R.array.duration_unit_options)
        )
        binding.loanSimulatorDurationUnitAutoCompleteTextView.setAdapter(adapter)
        // Workaround to show all items in the dropdown list after a rotation
        binding.loanSimulatorDurationUnitAutoCompleteTextView.threshold = (Integer.MAX_VALUE)

        binding.loanSimulatorAmountTextInputEditText.transformationMethod = null
        binding.loanSimulatorDownPaymentTextInputEditText.transformationMethod = null
        binding.loanSimulatorDurationTextInputEditText.transformationMethod = null

        binding.loanSimulatorInterestTextInputEditText.filters = arrayOf(PointBeforeNumberFilter())
        binding.loanSimulatorInterestTextInputEditText.doAfterTextChanged {
            val interestRate = it?.toString() ?: ""
            viewModel.onInterestRateChanged(interestRate)
        }

        binding.loanSimulatorInsuranceTextInputEditText.filters = arrayOf(PointBeforeNumberFilter())
        binding.loanSimulatorInsuranceTextInputEditText.doAfterTextChanged {
            val insuranceRate = it?.toString() ?: ""
            viewModel.onInsuranceRateChanged(insuranceRate)
        }

        binding.loanSimulatorDurationTextInputEditText.doAfterTextChanged {
            val duration = it?.toString() ?: ""
            viewModel.onDurationChanged(duration)
        }
        binding.loanSimulatorDurationUnitAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            viewModel.onDurationUnitChanged(DurationUnit.values()[position])
        }

        selectAllTextOnFocus(binding.loanSimulatorAmountTextInputEditText)
        selectAllTextOnFocus(binding.loanSimulatorDownPaymentTextInputEditText)
        selectAllTextOnFocus(binding.loanSimulatorInterestTextInputEditText)
        selectAllTextOnFocus(binding.loanSimulatorInsuranceTextInputEditText)
        selectAllTextOnFocus(binding.loanSimulatorDurationTextInputEditText)

        binding.loanSimulatorResetButton.setOnClickListener {
            viewModel.onResetButtonClicked()
            binding.loanSimulatorAmountTextInputEditText.setText("")
            binding.loanSimulatorDownPaymentTextInputEditText.setText("")
            binding.loanSimulatorInterestTextInputEditText.setText("")
            binding.loanSimulatorInsuranceTextInputEditText.setText("")
            binding.loanSimulatorDurationTextInputEditText.setText("")
        }

        binding.loanSimulatorCalculateButton.setOnClickListener {
            viewModel.onCalculateButtonClicked()
            hideKeyboard()
        }

        setExpandButtonOnClickListener(binding.loanSimulatorMonthlyResultsExpandButton)
        setExpandButtonOnClickListener(binding.loanSimulatorYearlyResultsExpandButton)
        setExpandButtonOnClickListener(binding.loanSimulatorTotalResultsExpandButton)

        viewModel.viewStateLiveData.observe(viewLifecycleOwner) { viewState ->
            binding.loanSimulatorAmountTextInputLayout.startIconDrawable = getDrawable(viewState)

            if (currentCurrency != viewState.currencyFormat) {
                addTextChangedListenerForAmount(
                    binding.loanSimulatorAmountTextInputEditText,
                    viewState,
                    viewModel::onAmountChanged
                )
                addTextChangedListenerForAmount(
                    binding.loanSimulatorDownPaymentTextInputEditText,
                    viewState,
                    viewModel::onDownPaymentChanged
                )
            }
            currentCurrency = viewState.currencyFormat

            binding.loanSimulatorDownPaymentTextInputLayout.startIconDrawable = getDrawable(viewState)

            binding.loanSimulatorAmountTextInputLayout.error = viewState.amountError?.toCharSequence(requireContext())
            binding.loanSimulatorDownPaymentTextInputLayout.error = viewState.downPaymentError?.toCharSequence(requireContext())
            binding.loanSimulatorInterestTextInputLayout.error = viewState.interestRateError?.toCharSequence(requireContext())
            binding.loanSimulatorInsuranceTextInputLayout.error = viewState.insuranceRateError?.toCharSequence(requireContext())
            binding.loanSimulatorDurationTextInputLayout.error = viewState.durationError?.toCharSequence(requireContext())

            binding.loanSimulatorMonthlyPaymentResult.text = viewState.monthlyPayment
            binding.loanSimulatorMonthlyInterestResult.text = viewState.monthlyInterest
            binding.loanSimulatorMonthlyInsuranceResult.text = viewState.monthlyInsurance
            binding.loanSimulatorYearlyPaymentResult.text = viewState.yearlyPayment
            binding.loanSimulatorYearlyInterestResult.text = viewState.yearlyInterest
            binding.loanSimulatorYearlyInsuranceResult.text = viewState.yearlyInsurance
            binding.loanSimulatorTotalPaymentResult.text = viewState.totalPayment
            binding.loanSimulatorTotalInterestResult.text = viewState.totalInterest
            binding.loanSimulatorTotalInsuranceResult.text = viewState.totalInsurance

            setOnClickListenerCopyToClipboard(binding.loanSimulatorMonthlyPaymentResult, "monthlyPayment", viewState.monthlyPayment)
            setOnClickListenerCopyToClipboard(binding.loanSimulatorMonthlyInterestResult, "monthlyInterest", viewState.monthlyInterest)
            setOnClickListenerCopyToClipboard(binding.loanSimulatorMonthlyInsuranceResult, "monthlyInsurance", viewState.monthlyInsurance)
            setOnClickListenerCopyToClipboard(binding.loanSimulatorYearlyPaymentResult, "yearlyPayment", viewState.yearlyPayment)
            setOnClickListenerCopyToClipboard(binding.loanSimulatorYearlyInterestResult, "yearlyInterest", viewState.yearlyInterest)
            setOnClickListenerCopyToClipboard(binding.loanSimulatorYearlyInsuranceResult, "yearlyInsurance", viewState.yearlyInsurance)
            setOnClickListenerCopyToClipboard(binding.loanSimulatorTotalPaymentResult, "totalPayment", viewState.totalPayment)
            setOnClickListenerCopyToClipboard(binding.loanSimulatorTotalInterestResult, "totalInterest", viewState.totalInterest)
            setOnClickListenerCopyToClipboard(binding.loanSimulatorTotalInsuranceResult, "totalInsurance", viewState.totalInsurance)
        }

        viewModel.viewActionLiveData.observeEvent(viewLifecycleOwner) {
            when (it) {
                is LoanSimulatorViewAction.CloseDialog -> dismiss()
                is LoanSimulatorViewAction.ShowToast -> it.message.showAsToast(requireContext())
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("isMonthlyResultsCardExpanded", isMonthlyResultsCardExpanded)
        outState.putBoolean("isYearlyResultsCardExpanded", isYearlyResultsCardExpanded)
        outState.putBoolean("isTotalResultsCardExpanded", isTotalResultsCardExpanded)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        isMonthlyResultsCardExpanded = savedInstanceState?.getBoolean("isMonthlyResultsCardExpanded") ?: false
        isYearlyResultsCardExpanded = savedInstanceState?.getBoolean("isYearlyResultsCardExpanded") ?: false
        isTotalResultsCardExpanded = savedInstanceState?.getBoolean("isTotalResultsCardExpanded") ?: false

        setCardViewDetailsVisibility(binding.loanSimulatorMonthlyPaymentDetailsConstraintLayout)
        setCardViewDetailsVisibility(binding.loanSimulatorYearlyPaymentDetailsConstraintLayout)
        setCardViewDetailsVisibility(binding.loanSimulatorTotalPaymentDetailsConstraintLayout)
    }

    private fun setCardViewDetailsVisibility(details: ConstraintLayout) {
        when (details) {
            binding.loanSimulatorMonthlyPaymentDetailsConstraintLayout -> {
                if (isMonthlyResultsCardExpanded) {
                    binding.loanSimulatorMonthlyResultsExpandButton.setImageResource(R.drawable.baseline_expand_less_24)
                    details.visibility = View.VISIBLE
                } else {
                    binding.loanSimulatorMonthlyResultsExpandButton.setImageResource(R.drawable.baseline_expand_more_24)
                    details.visibility = View.GONE
                }
            }
            binding.loanSimulatorYearlyPaymentDetailsConstraintLayout -> {
                if (isYearlyResultsCardExpanded) {
                    binding.loanSimulatorYearlyResultsExpandButton.setImageResource(R.drawable.baseline_expand_less_24)
                    details.visibility = View.VISIBLE
                } else {
                    binding.loanSimulatorYearlyResultsExpandButton.setImageResource(R.drawable.baseline_expand_more_24)
                    details.visibility = View.GONE
                }
            }
            binding.loanSimulatorTotalPaymentDetailsConstraintLayout -> {
                if (isTotalResultsCardExpanded) {
                    binding.loanSimulatorTotalResultsExpandButton.setImageResource(R.drawable.baseline_expand_less_24)
                    details.visibility = View.VISIBLE
                } else {
                    binding.loanSimulatorTotalResultsExpandButton.setImageResource(R.drawable.baseline_expand_more_24)
                    details.visibility = View.GONE
                }
            }
        }
    }

    private fun setExpandButtonOnClickListener(buttonView: ImageButton) {
        val viewToAnimate = when (buttonView) {
            binding.loanSimulatorMonthlyResultsExpandButton -> binding.loanSimulatorMonthlyResultsCardView
            binding.loanSimulatorYearlyResultsExpandButton -> binding.loanSimulatorYearlyResultsCardView
            binding.loanSimulatorTotalResultsExpandButton -> binding.loanSimulatorTotalResultsCardView
            else -> throw IllegalArgumentException("Unknown buttonView")
        }
        val viewToShow = when (buttonView) {
            binding.loanSimulatorMonthlyResultsExpandButton -> binding.loanSimulatorMonthlyPaymentDetailsConstraintLayout
            binding.loanSimulatorYearlyResultsExpandButton -> binding.loanSimulatorYearlyPaymentDetailsConstraintLayout
            binding.loanSimulatorTotalResultsExpandButton -> binding.loanSimulatorTotalPaymentDetailsConstraintLayout
            else -> throw IllegalArgumentException("Unknown buttonView")
        }

        buttonView.setOnClickListener {
            val isExpanding: Boolean
            val visibility = if (viewToShow.visibility == View.GONE) {
                    isExpanding = true
                    when (buttonView) {
                        binding.loanSimulatorMonthlyResultsExpandButton -> isMonthlyResultsCardExpanded = true
                        binding.loanSimulatorYearlyResultsExpandButton -> isYearlyResultsCardExpanded = true
                        binding.loanSimulatorTotalResultsExpandButton -> isTotalResultsCardExpanded = true
                    }
                    buttonView.setImageResource(R.drawable.baseline_expand_less_24)
                    View.VISIBLE
                } else {
                    isExpanding = false
                    when (buttonView) {
                        binding.loanSimulatorMonthlyResultsExpandButton -> isMonthlyResultsCardExpanded = false
                        binding.loanSimulatorYearlyResultsExpandButton -> isYearlyResultsCardExpanded = false
                        binding.loanSimulatorTotalResultsExpandButton -> isTotalResultsCardExpanded = false
                    }
                    buttonView.setImageResource(R.drawable.baseline_expand_more_24)
                    View.GONE
                }
            beginDelayedTransition(isExpanding, viewToAnimate)
            viewToShow.visibility = visibility
        }
    }

    private fun beginDelayedTransition(isExpanding: Boolean, view: View) {
        val transition = AutoTransition().addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionEnd(transition: Transition) {
                if (isExpanding) {
                    binding.root.smoothScrollTo(0, view.bottom)
                }
            }

            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
        })
        TransitionManager.beginDelayedTransition(binding.root, transition)
    }

    private fun addTextChangedListenerForAmount(
        textInputEditText: TextInputEditText,
        viewState: LoanSimulatorViewState,
        updateValue: (value: String) -> Unit
    ) {
        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let { amount ->
                    val originalText = amount.toString()
                    updateValue(originalText)

                    if (originalText.isNotEmpty()) {
                        try {
                            val parsed = viewState.currencyFormat.parse(originalText)
                            val formatted = viewState.currencyFormat.format(parsed)
                            if (formatted != originalText) {
                                textInputEditText.removeTextChangedListener(this)
                                textInputEditText.setText(formatted)
                                textInputEditText.setSelection(formatted.length)
                                textInputEditText.addTextChangedListener(this)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
    }

    private fun selectAllTextOnFocus(textInputEditText: TextInputEditText) {
        textInputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Detects when layout is ready to be drawn
                binding.root.viewTreeObserver.addOnPreDrawListener(object :
                    ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        // Removes listener to prevent repeated calls
                        binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                        // Selects all text
                        textInputEditText.selectAll()
                        return true
                    }
                })
            }
        }
    }

    private fun setOnClickListenerCopyToClipboard(textView: TextView, label: String, data: String) {
        textView.setOnClickListener {
            if (data.isNotEmpty()) {
                val clipData = ClipData.newPlainText(label, data)
                clipboardManager.setPrimaryClip(clipData)
                viewModel.onResultClicked()
            }
        }
    }

    private fun getDrawable(it: LoanSimulatorViewState): Drawable? {
        return ResourcesCompat.getDrawable(resources, it.currencyIcon, null)
    }
}

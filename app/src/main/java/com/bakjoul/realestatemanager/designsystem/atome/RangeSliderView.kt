package com.bakjoul.realestatemanager.designsystem.atome

import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.widget.doAfterTextChanged
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ViewRangeSliderBinding
import com.bakjoul.realestatemanager.designsystem.utils.CustomViewUtils.hideKeyboard
import com.bakjoul.realestatemanager.ui.utils.restoreChildViewStates
import com.bakjoul.realestatemanager.ui.utils.saveChildViewStates
import java.math.BigDecimal
import java.util.Locale
import java.util.concurrent.CopyOnWriteArraySet

class RangeSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewRangeSliderBinding.inflate(LayoutInflater.from(context), this, true)

    private val listeners = CopyOnWriteArraySet<(Pair<BigDecimal, BigDecimal>) -> Unit>()

    private var range: Pair<BigDecimal, BigDecimal> = Pair(BigDecimal.ZERO, BigDecimal.ZERO)
        set(value) {
            field = value
            publishNewValueToListeners(value)
        }
    private var isInitializingValues = true
    private var updatingFromSlider = false
    private var updatingFromEditText = false

    init {
        context.withStyledAttributes(attrs, R.styleable.RangeSliderView) {
            // Title
            getString(R.styleable.RangeSliderView_rangeSliderTitle)?.let {
                binding.rangeSliderTitle.text = it
            }

            // Minimum value TextInputLayout hint
            getString(R.styleable.RangeSliderView_rangeSliderMinValueHint)?.let {
                binding.rangeSliderMinValueTextInputLayout.hint = it
            }

            // Maximum value TextInputLayout hint
            getString(R.styleable.RangeSliderView_rangeSliderMaxValueHint)?.let {
                binding.rangeSliderMaxValueTextInputLayout.hint = it
            }

            // Minimum value EditText imeOptions
            binding.rangeSliderMinValueTextInputEditText.imeOptions = EditorInfo.IME_ACTION_NEXT

            // Maximum value EditText imeOptions
            val imeOptions = getInt(R.styleable.RangeSliderView_rangeSliderMaxValueTextImeOptions, 0)
            if (imeOptions == 1) {
                binding.rangeSliderMaxValueTextInputEditText.imeOptions = EditorInfo.IME_ACTION_DONE
                binding.rangeSliderMaxValueTextInputEditText.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        binding.rangeSliderMaxValueTextInputEditText.clearFocus()
                        hideKeyboard(binding.rangeSliderMaxValueTextInputEditText)
                        true
                    } else {
                        false
                    }
                }
            }
        }

        binding.rangeSlider.stepSize = 1f

        binding.rangeSlider.addOnChangeListener { rangeSlider, _, _ ->
            if (!updatingFromEditText) {
                if (!isInitializingValues) {
                    updatingFromSlider = true
                }

                range = Pair(
                    BigDecimal(rangeSlider.values[0].toDouble()),
                    BigDecimal(rangeSlider.values[1].toDouble())
                )

                val minValue = BigDecimal.valueOf(rangeSlider.values[0].toDouble())
                val maxValue = BigDecimal.valueOf(rangeSlider.values[1].toDouble())

                val minValueString = String.format(Locale.getDefault(), "%.0f", minValue)
                val maxValueString = String.format(Locale.getDefault(), "%.0f", maxValue)

                // Updates EditTexts only if the value is different
                if (binding.rangeSliderMinValueTextInputEditText.text.toString() != minValueString) {
                    binding.rangeSliderMinValueTextInputEditText.setText(minValueString)
                    binding.rangeSliderMinValueTextInputLayout.error = null
                }
                if (binding.rangeSliderMaxValueTextInputEditText.text.toString() != maxValueString) {
                    binding.rangeSliderMaxValueTextInputEditText.setText(maxValueString)
                    binding.rangeSliderMaxValueTextInputLayout.error = null
                }
            }
            updatingFromEditText = false
        }

        // Selects all text in EditText when it gains focus
        binding.rangeSliderMinValueTextInputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Detects when the view is ready to be drawn
                binding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        // Removes the listener to avoid multiple calls
                        binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                        // Selects all text in the EditText
                        binding.rangeSliderMinValueTextInputEditText.selectAll()
                        return true
                    }
                })
            } else {
                if (binding.rangeSliderMinValueTextInputEditText.text.toString().isEmpty()) {
                    binding.rangeSliderMinValueTextInputEditText.text = SpannableStringBuilder(binding.rangeSlider.values[0].toInt().toString())
                }
            }
        }

        binding.rangeSliderMinValueTextInputEditText.doAfterTextChanged {
            if (isInitializingValues) {
                return@doAfterTextChanged
            }

            if (!updatingFromSlider) {
                updatingFromEditText = true

                val textValue = it.toString().toBigDecimalOrNull()
                if (textValue != null) {
                    if (textValue >= binding.rangeSlider.valueFrom.toBigDecimal() &&
                        textValue <= binding.rangeSlider.values[1].toBigDecimal()
                    ) {
                        binding.rangeSlider.values = listOf(textValue.toFloat(), binding.rangeSlider.values[1])
                        binding.rangeSliderMinValueTextInputLayout.error = null
                    } else {
                        binding.rangeSliderMinValueTextInputLayout.error = context.getString(R.string.invalid_value)
                    }
                }
            }
            updatingFromSlider = false
        }

        // Selects all text in EditText when it gains focus
        binding.rangeSliderMaxValueTextInputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Detects when the view is ready to be drawn
                binding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        // Removes the listener to avoid multiple calls
                        binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                        // Selects all text in the EditText
                        binding.rangeSliderMaxValueTextInputEditText.selectAll()
                        return true
                    }
                })
            } else {
                if (binding.rangeSliderMaxValueTextInputEditText.text.toString().isEmpty()) {
                    binding.rangeSliderMaxValueTextInputEditText.text = SpannableStringBuilder(binding.rangeSlider.values[1].toInt().toString())
                }
            }
        }

        binding.rangeSliderMaxValueTextInputEditText.doAfterTextChanged {
            if (isInitializingValues) {
                return@doAfterTextChanged
            }

            if (!updatingFromSlider) {
                updatingFromEditText = true

                val textValue = it.toString().toBigDecimalOrNull()
                if (textValue != null) {
                    if (textValue <= binding.rangeSlider.valueTo.toBigDecimal() &&
                        textValue >= binding.rangeSlider.values[0].toBigDecimal()
                    ) {
                        binding.rangeSlider.values = listOf(binding.rangeSlider.values[0], textValue.toFloat())
                        binding.rangeSliderMaxValueTextInputLayout.error = null
                    } else {
                        binding.rangeSliderMaxValueTextInputLayout.error = context.getString(R.string.invalid_value)
                    }
                }
            }
            updatingFromSlider = false
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).apply {
            childrenStates = saveChildViewStates()
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                state.childrenStates?.let { restoreChildViewStates(it) }
            }

            else -> super.onRestoreInstanceState(state)
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>?) {
        dispatchThawSelfOnly(container)
    }

    private fun publishNewValueToListeners(newValue: Pair<BigDecimal, BigDecimal>) {
        listeners.forEach { listener ->
            listener.invoke(newValue)
        }
    }

    fun setTitle(title: String) {
        binding.rangeSliderTitle.text = title
    }

    fun setRangeValues(valueFrom: Float, valueTo: Float, minValue: Float?, maxValue: Float?) {
        binding.rangeSlider.valueFrom = valueFrom
        binding.rangeSlider.valueTo = valueTo
        if (minValue != null && maxValue != null) {
            binding.rangeSlider.values = listOf(minValue, maxValue)
        } else {
            binding.rangeSlider.values = listOf(valueFrom, valueTo)
        }
        isInitializingValues = false
    }

    fun setLabelFormatter(formatter: (Float) -> String) {
        binding.rangeSlider.setLabelFormatter(formatter)
    }

    fun setMinValueHelperText(helperText: String) {
        binding.rangeSliderMinValueTextInputLayout.helperText = helperText
    }

    fun setMaxValueHelperText(helperText: String) {
        binding.rangeSliderMaxValueTextInputLayout.helperText = helperText
    }

    fun addOnRangeChangedListener(listener: (Pair<BigDecimal, BigDecimal>) -> Unit) {
        listeners.add(listener)
    }

    fun removeOnRangeChangedListener(listener: (Pair<BigDecimal, BigDecimal>) -> Unit) {
        listeners.remove(listener)
    }

    internal class SavedState : BaseSavedState {

        internal var childrenStates: SparseArray<Parcelable>? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            @Suppress("DEPRECATION")
            childrenStates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                source.readSparseArray(javaClass.classLoader, SavedState::class.java)
            } else {
                source.readSparseArray(javaClass.classLoader)
            }
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeSparseArray(childrenStates as SparseArray<Parcelable>)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel) = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }
}

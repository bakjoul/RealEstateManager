package com.bakjoul.realestatemanager.designsystem.atome

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.widget.doAfterTextChanged
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ViewPlusMinusBinding
import java.math.BigDecimal

class PlusMinusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding = ViewPlusMinusBinding.inflate(LayoutInflater.from(context), this, true)

    private var isBigDecimal = false
    private var value : Number? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.PlusMinusView) {
            // Label drawable start
            getDrawable(R.styleable.PlusMinusView_plusMinusImageSrc)?.let {
                setDrawableStart(it)
            }
            // Label text
            getString(R.styleable.PlusMinusView_plusMinusLabel)?.let {
                binding.viewPlusMinusLabelText.text = it
            }
            // Value
            getString(R.styleable.PlusMinusView_plusMinusValue)?.let {
                binding.viewPlusMinusValueEditText.text = SpannableStringBuilder(it)
            }
            // Value type
            isBigDecimal = getBoolean(R.styleable.PlusMinusView_plusMinusIsBigDecimal, false)
            // Label drawable padding
            getDimensionPixelSize(R.styleable.PlusMinusView_plusMinusDrawablePadding, 2).let {
                binding.viewPlusMinusLabelText.compoundDrawablePadding = it
            }
            // Value EditText width
            getDimensionPixelSize(R.styleable.PlusMinusView_plusMinusEditTextWidth, 0).takeIf { it != 0 }?.let { editTextWidth ->
                val layoutParams = binding.viewPlusMinusValueEditText.layoutParams
                layoutParams.width = editTextWidth
                binding.viewPlusMinusValueEditText.layoutParams = layoutParams
            }
        }

        // Sets and displays initial value
        value = if (isBigDecimal) BigDecimal.ZERO else 0
        binding.viewPlusMinusValueEditText.text = SpannableStringBuilder(value.toString())

        // Disables decrement button by default
        binding.viewPlusMinusDecrementButton.isEnabled = false
        binding.viewPlusMinusDecrementButton.alpha = 0.5f

        // Disables decrement button when value is 0 and updates value when EditText text changes
        binding.viewPlusMinusValueEditText.doAfterTextChanged { editable ->
            val editTextString = editable?.toString()

            if (isBigDecimal) {
                val editTextValue = editTextString?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                binding.viewPlusMinusDecrementButton.isEnabled = editTextValue != BigDecimal.ZERO
                binding.viewPlusMinusDecrementButton.alpha = if (editTextValue == BigDecimal.ZERO) 0.5f else 1f
                value = editTextValue
            } else {
                val editTextValue = editTextString?.toIntOrNull() ?: 0
                binding.viewPlusMinusDecrementButton.isEnabled = editTextValue != 0
                binding.viewPlusMinusDecrementButton.alpha = if (editTextValue == 0) 0.5f else 1f
                value = editTextValue
            }
        }

        // Decrements value when decrement button is clicked
        binding.viewPlusMinusDecrementButton.setOnClickListener {
            // Updates value
            value = if (isBigDecimal) {
                (value as? BigDecimal)?.minus(BigDecimal.ONE)
            } else {
                (value as? Int)?.minus(1)
            }

            // Displays new value
            binding.viewPlusMinusValueEditText.text = SpannableStringBuilder(value.toString())
        }

        // Increments value when increment button is clicked
        binding.viewPlusMinusIncrementButton.setOnClickListener {
            // Updates value
            value = if (isBigDecimal) {
                (value as? BigDecimal)?.plus(BigDecimal.ONE)
            } else {
                (value as? Int)?.plus(1)
            }

            // Displays new value
            binding.viewPlusMinusValueEditText.text = SpannableStringBuilder(value.toString())
        }

        // Selects all text when EditText gains focus
        binding.viewPlusMinusValueEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Detects when layout is ready to be drawn
                binding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        // Removes listener to prevent repeated calls
                        binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                        // Selects all text
                        binding.viewPlusMinusValueEditText.selectAll()
                        return true
                    }
                })
            } else {
                if (binding.viewPlusMinusValueEditText.text.toString().isEmpty()) {
                    binding.viewPlusMinusValueEditText.text = SpannableStringBuilder(value.toString())
                }
            }
        }
    }

    private fun setDrawableStart(drawable: Drawable) {
        val compoundDrawables = binding.viewPlusMinusLabelText.compoundDrawables
        binding.viewPlusMinusLabelText.setCompoundDrawablesWithIntrinsicBounds(
            drawable,
            compoundDrawables[1],
            compoundDrawables[2],
            compoundDrawables[3]
        )
    }

    fun setLabel(label: String) {
        binding.viewPlusMinusLabelText.text = label
    }

    fun getValue() : Number? {
        return value
    }
}

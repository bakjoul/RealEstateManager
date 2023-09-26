package com.bakjoul.realestatemanager.designsystem.atome

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ViewPlusMinusBinding
import java.math.BigDecimal

class PlusMinusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewPlusMinusBinding.inflate(LayoutInflater.from(context), this, true)

    private var count : Int? = null // 3

    init {
        context.withStyledAttributes(attrs, R.styleable.PlusMinusView) {
            val drawableStart = getDrawable(R.styleable.PlusMinusView_plusMinusImageSrc)
            if (drawableStart != null) {
                setDrawableStart(drawableStart)
            }
            getString(R.styleable.PlusMinusView_plusMinusLabel)?.let {
                binding.viewPlusMinusLabelText.text = it
            }
            getString(R.styleable.PlusMinusView_plusMinusValue)?.let {
                val editableText = SpannableStringBuilder(it)
                binding.viewPlusMinusValueEditText.text = editableText
            }
            getDimensionPixelSize(R.styleable.PlusMinusView_plusMinusDrawablePadding, 2).let {
                binding.viewPlusMinusLabelText.compoundDrawablePadding = it
            }

            val editTextWidth = getDimensionPixelSize(R.styleable.PlusMinusView_plusMinusEditTextWidth, 0)
            if (editTextWidth != 0) {
                val layoutParams = binding.viewPlusMinusValueEditText.layoutParams
                layoutParams.width = editTextWidth
                binding.viewPlusMinusValueEditText.layoutParams = layoutParams
            }
        }

        // Disables the decrement button by default
        binding.viewPlusMinusDecrementButton.isEnabled = false
        binding.viewPlusMinusDecrementButton.alpha = 0.5f
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

    fun getValueEditText(): EditText {
        return binding.viewPlusMinusValueEditText
    }

    fun setValueEditText(value: String) {
        val editableText = SpannableStringBuilder(value)
        binding.viewPlusMinusValueEditText.text = editableText
    }

    fun getIntValue(): Int {
        return binding.viewPlusMinusValueEditText.text.toString().toInt()
    }

    fun getBigDecimalValue(): BigDecimal {
        return BigDecimal(binding.viewPlusMinusValueEditText.text.toString())
    }

    fun getFormattedIntValue(): String {
        val inputText = binding.viewPlusMinusValueEditText.text.toString()
        return if (inputText.isNotBlank()) {
            val value = inputText.toInt()
            value.toString()
        } else {
            // If input is blank
            "0"
        }
    }

    fun addOnValueChangedListener(listener : (Int) -> Unit) {

    }

    fun getFormattedBigDecimalValue(): String {
        val inputText = binding.viewPlusMinusValueEditText.text.toString()
        return if (inputText.isNotBlank()) {
            val value = BigDecimal(inputText)
            if (value == value.toInt().toBigDecimal()) {
                value.toInt().toString()
            } else {
                value.toString()
            }
        } else {
            // If input is blank
            "0"
        }
    }

    fun decrementButton(): ImageButton = binding.viewPlusMinusDecrementButton

    fun incrementButton(): ImageButton = binding.viewPlusMinusIncrementButton

    override fun onSaveInstanceState(): Parcelable? {
        return count
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        count = state.
    }
}

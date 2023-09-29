package com.bakjoul.realestatemanager.designsystem.plus_minus

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ViewPlusMinusBinding
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils

class PlusMinusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewPlusMinusBinding.inflate(LayoutInflater.from(context), this, true)
    private val viewModelKey = ViewModelUtils.generateViewModelKey()
    private val viewModel by lazy { ViewModelProvider(findViewTreeViewModelStoreOwner()!!)[viewModelKey, PlusMinusViewModel::class.java] }

    private var isBigDecimal = false

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
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        viewModel.setBigDecimal(isBigDecimal)

        // Disables decrement button when value is 0 and updates value when EditText text changes
        binding.viewPlusMinusValueEditText.doAfterTextChanged { editable ->
            editable?.toString()?.let { viewModel.setValue(it) }
        }
        binding.viewPlusMinusDecrementButton.setOnClickListener { viewModel.decrementValue() }
        binding.viewPlusMinusIncrementButton.setOnClickListener { viewModel.incrementValue() }

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
                    //binding.viewPlusMinusValueEditText.text = SpannableStringBuilder(value.toString())
                    viewModel.setValue("0")
                }
            }
        }

        viewModel.getValue().observe(findViewTreeLifecycleOwner()!!) {
            binding.viewPlusMinusValueEditText.setText(it.toString())
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
}

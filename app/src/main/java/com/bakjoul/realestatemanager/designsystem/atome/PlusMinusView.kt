package com.bakjoul.realestatemanager.designsystem.atome

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.widget.doAfterTextChanged
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ViewPlusMinusBinding
import com.bakjoul.realestatemanager.ui.utils.restoreChildViewStates
import com.bakjoul.realestatemanager.ui.utils.saveChildViewStates
import java.math.BigDecimal
import java.util.concurrent.CopyOnWriteArraySet

class PlusMinusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewPlusMinusBinding.inflate(LayoutInflater.from(context), this, true)

    private val listeners = CopyOnWriteArraySet<(Number) -> Unit>()

    // FIXME When Context.withStyledAttributes is updated to guarantee variable value
    private var isBigDecimal: Boolean = false
    private var count: BigDecimal = BigDecimal.ZERO
        set(value) {
            field = value
            publishNewValueToListeners(value)
        }

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
            // EditText width
            getDimensionPixelSize(R.styleable.PlusMinusView_plusMinusEditTextWidth, 0).takeIf { it != 0 }?.let { editTextWidth ->
                val layoutParams = binding.viewPlusMinusValueEditText.layoutParams
                layoutParams.width = editTextWidth
                binding.viewPlusMinusValueEditText.layoutParams = layoutParams
            }
            // EditText imeOptions
            val imeOptions = getInt(R.styleable.PlusMinusView_plusMinusImeOptions, 0)
            if (imeOptions == 1) {
                binding.viewPlusMinusValueEditText.imeOptions = EditorInfo.IME_ACTION_DONE
                binding.viewPlusMinusValueEditText.setOnEditorActionListener { _, actionId, _ ->
                    // Clears focus and hides keyboard when done button is clicked
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        binding.viewPlusMinusValueEditText.clearFocus()
                        hideKeyboard(binding.viewPlusMinusValueEditText)
                        true
                    } else {
                        false
                    }
                }
            }
        }

        // Sets and displays initial value
        binding.viewPlusMinusValueEditText.text = SpannableStringBuilder(count.toString())

        // Disables decrement button by default
        binding.viewPlusMinusDecrementButton.isEnabled = false
        binding.viewPlusMinusDecrementButton.alpha = 0.5f

        // Disables decrement button when value is 0 and updates value when EditText text changes
        binding.viewPlusMinusValueEditText.doAfterTextChanged { editable ->
            val editTextValue = editable?.toString()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            binding.viewPlusMinusDecrementButton.isEnabled = editTextValue != BigDecimal.ZERO
            binding.viewPlusMinusDecrementButton.alpha = if (editTextValue == BigDecimal.ZERO) 0.5f else 1f
            count = editTextValue
        }

        // Decrements value when decrement button is clicked
        binding.viewPlusMinusDecrementButton.setOnClickListener {
            // Updates value
            count = count.minus(BigDecimal.ONE)

            // Displays new value
            binding.viewPlusMinusValueEditText.text = SpannableStringBuilder(count.toString())
        }

        // Increments value when increment button is clicked
        binding.viewPlusMinusIncrementButton.setOnClickListener {
            // Updates value
            count = count.plus(BigDecimal.ONE)

            // Displays new value
            binding.viewPlusMinusValueEditText.text = SpannableStringBuilder(count.toString())
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
                    binding.viewPlusMinusValueEditText.text =
                        SpannableStringBuilder(count.toString())
                }
            }
        }
    }

    private fun publishNewValueToListeners(newValue: Number) {
        listeners.forEach { listener ->
            listener.invoke(newValue)
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

    private fun setDrawableStart(drawable: Drawable) {
        val compoundDrawables = binding.viewPlusMinusLabelText.compoundDrawables
        binding.viewPlusMinusLabelText.setCompoundDrawablesWithIntrinsicBounds(
            drawable,
            compoundDrawables[1],
            compoundDrawables[2],
            compoundDrawables[3]
        )
    }

    private fun hideKeyboard(view: View) {
        val context = view.context
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun setLabel(label: String) {
        binding.viewPlusMinusLabelText.text = label
    }

    fun addOnValueChangedListener(listener: (Number) -> Unit) {
        listeners.add(listener)
    }

    fun removeOnValueChangeListener(listener: (Number) -> Unit) {
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

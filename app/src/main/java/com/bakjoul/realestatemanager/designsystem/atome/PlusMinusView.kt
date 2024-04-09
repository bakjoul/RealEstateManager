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
import androidx.core.view.isVisible
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

    private val listeners = CopyOnWriteArraySet<(BigDecimal) -> Unit>()

    // FIXME When Context.withStyledAttributes is updated to guarantee variable value
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
                binding.plusMinusLabelText.text = it
            }
            // Error text
            getString(R.styleable.PlusMinusView_plusMinusErrorText)?.let {
                binding.plusMinusErrorTextView.text = it
            }
            // Label drawable padding
            getDimensionPixelSize(R.styleable.PlusMinusView_plusMinusDrawablePadding, 2).let {
                binding.plusMinusLabelText.compoundDrawablePadding = it
            }
            // EditText width
            getDimensionPixelSize(R.styleable.PlusMinusView_plusMinusEditTextWidth, 0).takeIf { it != 0 }?.let { editTextWidth ->
                val layoutParams = binding.plusMinusValueEditText.layoutParams
                layoutParams.width = editTextWidth
                binding.plusMinusValueEditText.layoutParams = layoutParams
            }
            // EditText imeOptions
            val imeOptions = getInt(R.styleable.PlusMinusView_plusMinusImeOptions, 0)
            if (imeOptions == 1) {
                binding.plusMinusValueEditText.imeOptions = EditorInfo.IME_ACTION_DONE
                binding.plusMinusValueEditText.setOnEditorActionListener { _, actionId, _ ->
                    // Clears focus and hides keyboard when done button is clicked
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        binding.plusMinusValueEditText.clearFocus()
                        hideKeyboard(binding.plusMinusValueEditText)
                        true
                    } else {
                        false
                    }
                }
            }
        }

        // Sets and displays initial value
        binding.plusMinusValueEditText.text = SpannableStringBuilder(count.toString())

        // Disables decrement button by default
        binding.plusMinusDecrementButton.isEnabled = false
        binding.plusMinusDecrementButton.alpha = 0.5f

        // Disables decrement button when value is 0 and updates value when EditText text changes
        binding.plusMinusValueEditText.doAfterTextChanged { editable ->
            val editTextValue = editable?.toString()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            binding.plusMinusDecrementButton.isEnabled = editTextValue != BigDecimal.ZERO
            binding.plusMinusDecrementButton.alpha = if (editTextValue == BigDecimal.ZERO) 0.5f else 1f

            if (editTextValue != count) {
                count = editTextValue
            }
        }

        // Decrements value when decrement button is clicked
        binding.plusMinusDecrementButton.setOnClickListener {
            // Updates value
            count = count.minus(BigDecimal.ONE)

            // Displays new value
            binding.plusMinusValueEditText.text = SpannableStringBuilder(count.toString())
        }

        // Increments value when increment button is clicked
        binding.plusMinusIncrementButton.setOnClickListener {
            // Updates value
            count = count.plus(BigDecimal.ONE)

            // Displays new value
            binding.plusMinusValueEditText.text = SpannableStringBuilder(count.toString())
        }

        // Selects all text when EditText gains focus
        binding.plusMinusValueEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Detects when layout is ready to be drawn
                binding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        // Removes listener to prevent repeated calls
                        binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                        // Selects all text
                        binding.plusMinusValueEditText.selectAll()
                        return true
                    }
                })
            } else {
                if (binding.plusMinusValueEditText.text.toString().isEmpty()) {
                    binding.plusMinusValueEditText.text = SpannableStringBuilder(count.toString())
                }
            }
        }
    }

    private fun publishNewValueToListeners(newValue: BigDecimal) {
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
        val compoundDrawables = binding.plusMinusLabelText.compoundDrawables
        binding.plusMinusLabelText.setCompoundDrawablesWithIntrinsicBounds(
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
        binding.plusMinusLabelText.text = label
    }

    fun setInitialValue(value: BigDecimal) {
        count = value
        binding.plusMinusValueEditText.text = SpannableStringBuilder(count.toString())
    }

    fun addOnValueChangedListener(listener: (BigDecimal) -> Unit) {
        listeners.add(listener)
    }

    fun removeOnValueChangeListener(listener: (BigDecimal) -> Unit) {
        listeners.remove(listener)
    }

    fun isErrorVisible(isErrorVisible: Boolean) {
        binding.plusMinusErrorTextView.isVisible = isErrorVisible
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

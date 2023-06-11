package com.bakjoul.realestatemanager.designsystem.atome

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ViewDetailsFieldBinding

class DetailsFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewDetailsFieldBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.withStyledAttributes(attrs, R.styleable.DetailsFieldView) {
            binding.detailsFieldIcon.setImageResource(getResourceId(R.styleable.DetailsFieldView_imageSrc, 0))
            getString(R.styleable.DetailsFieldView_label)?.let { binding.detailsFieldLabel.text = it }
            getString(R.styleable.DetailsFieldView_textPreview)?.let { binding.detailsFieldText.text = it }
        }
    }

    fun setText(text: String) {
        binding.detailsFieldText.text = text
    }
}

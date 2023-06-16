package com.bakjoul.realestatemanager.designsystem.atome

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ViewSettingsSpinnerBinding

class SettingsSpinnerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewSettingsSpinnerBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.withStyledAttributes(attrs, R.styleable.SettingsSpinnerView) {
            binding.settingsSpinnerIcon.setImageResource(getResourceId(R.styleable.SettingsSpinnerView_spinnerImageSrc, 0))
            getString(R.styleable.SettingsSpinnerView_spinnerLabel)?.let { binding.settingsSpinnerLabel.text = it }
            getString(R.styleable.SettingsSpinnerView_spinnerDescription)?.let { binding.settingsSpinnerDesc.text = it }

            val entriesResId = getResourceId(R.styleable.SettingsSpinnerView_spinnerEntries, 0)
            if (entriesResId != 0) {
                val entries = resources.getStringArray(entriesResId)
                val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, entries)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.settingsSpinner.adapter = adapter
            }
        }
    }

    fun getSpinner(): Spinner {
        return binding.settingsSpinner
    }

    fun getEntries(): Array<String> {
        return resources.getStringArray(R.array.currency_options)
    }
}

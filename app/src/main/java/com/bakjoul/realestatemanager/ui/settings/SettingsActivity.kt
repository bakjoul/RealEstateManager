package com.bakjoul.realestatemanager.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivitySettingsBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivitySettingsBinding.inflate(it) }
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setToolbar()

        val currencySpinner = binding.settingsCurrencySpinner.getSpinner()
        val currencyOptions = binding.settingsCurrencySpinner.getEntries()

        var isFirstSelection = true
        viewModel.getCurrencyLiveData().observe(this) { initialCurrency ->
            currencySpinner.setSelection(currencyOptions.indexOf(initialCurrency.nameWithSymbol))
        }

        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                viewModel.onCurrencySelected(currencyOptions[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setToolbar() {
        val toolbar = binding.settingsToolbar
        toolbar.setTitle(R.string.settings)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}
package com.bakjoul.realestatemanager.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivitySettingsBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivitySettingsBinding.inflate(it) }
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setToolbar()

        val currencySpinner = binding.settingsCurrencyDropdown
        val currencyOptions = resources.getStringArray(R.array.currency_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter

        var isFirstSelection = true
        lifecycleScope.launch {
            val initialCurrency = viewModel.getCurrentCurrencyFlow().first()
            currencySpinner.setSelection(currencyOptions.indexOf(initialCurrency))
        }

        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                val selectedCurrency = currencyOptions[position]

                lifecycleScope.launch {
                    viewModel.onCurrencySelected(selectedCurrency)
                }
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

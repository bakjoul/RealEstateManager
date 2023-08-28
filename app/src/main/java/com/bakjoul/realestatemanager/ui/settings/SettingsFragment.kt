package com.bakjoul.realestatemanager.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentSettingsBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding by viewBinding { FragmentSettingsBinding.bind(it) }
    private val viewModel by viewModels<SettingsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()

        val currencySpinner = binding.settingsCurrencySpinner.getSpinner()
        val currencyOptions = binding.settingsCurrencySpinner.getEntries()
        var isCurrencyFirstSelection = true

        viewModel.getCurrencyLiveData().observe(viewLifecycleOwner) { initialCurrency ->
            currencySpinner.setSelection(currencyOptions.indexOf(initialCurrency.nameWithSymbol))
        }

        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isCurrencyFirstSelection) {
                    isCurrencyFirstSelection = false
                    return
                }

                viewModel.onCurrencySelected(currencyOptions[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val surfaceUnitSpinner = binding.settingsUnitSpinner.getSpinner()
        val surfaceUnitOptions = binding.settingsUnitSpinner.getEntries()
        var isSurfaceUnitFirstSelection = true

        viewModel.getSurfaceUnitLiveData().observe(viewLifecycleOwner) { initialSurfaceUnit ->
            surfaceUnitSpinner.setSelection(surfaceUnitOptions.indexOf(initialSurfaceUnit.nameWithUnit))
        }

        surfaceUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isSurfaceUnitFirstSelection) {
                    isSurfaceUnitFirstSelection = false
                    return
                }

                viewModel.onSurfaceUnitSelected(surfaceUnitOptions[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setToolbar() {
        val toolbar = binding.settingsToolbar
        toolbar?.setTitle(R.string.settings)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar?.setNavigationOnClickListener { viewModel.onCloseButtonClicked() }
        binding.settingsCustomToolbarCloseButton?.setOnClickListener { viewModel.onCloseButtonClicked() }
    }
}

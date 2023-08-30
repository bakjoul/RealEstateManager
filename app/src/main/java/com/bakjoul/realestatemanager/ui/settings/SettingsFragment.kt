package com.bakjoul.realestatemanager.ui.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
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
        if (resources.getBoolean(R.bool.isTablet)) {
            setMenu()
        }

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
        toolbar.setTitle(R.string.settings)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener { viewModel.onCloseButtonClicked() }
    }

    private fun setMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.findItem(R.id.main_menu_button)?.isVisible = false
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.settings_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.settings_menu_close -> {
                    viewModel.onCloseButtonClicked()
                    true
                }

                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}

package com.bakjoul.realestatemanager.ui.settings

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentSettingsBinding
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : DialogFragment(R.layout.fragment_settings) {

    private companion object {
        private const val DIALOG_WINDOW_WIDTH = 0.5
    }

    private val binding by viewBinding { FragmentSettingsBinding.bind(it) }
    private val viewModel by viewModels<SettingsViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val params = dialog.window?.attributes
        params?.gravity = Gravity.END
        dialog.window?.attributes = params

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()

        if (resources.getBoolean(R.bool.isTablet)) {
            val width = (resources.displayMetrics.widthPixels * DIALOG_WINDOW_WIDTH).toInt()
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog?.window?.setLayout(width, height)
        }

        dialog?.window?.setWindowAnimations(R.style.DialogAnimation)
    }

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

        viewModel.viewActionLiveData.observeEvent(viewLifecycleOwner) {
            if (it is SettingsViewAction.CloseSettings) {
                dismiss()
            }
        }
    }

    private fun setToolbar() {
        val toolbar = binding.settingsToolbar
        toolbar?.setTitle(R.string.settings)

        toolbar?.setNavigationOnClickListener { viewModel.onCloseButtonClicked() }
        binding.settingsAppbarCloseButton?.setOnClickListener { viewModel.onCloseButtonClicked() }
    }
}

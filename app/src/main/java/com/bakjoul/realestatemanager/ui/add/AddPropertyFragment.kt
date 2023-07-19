package com.bakjoul.realestatemanager.ui.add

import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentAddPropertyBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPropertyFragment : Fragment(R.layout.fragment_add_property) {

    private val binding by viewBinding { FragmentAddPropertyBinding.bind(it) }
    private val viewModel by viewModels<AddPropertyViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addPropertyTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onPropertyTypeChanged(checkedId)
        }

        binding.addPropertyTypeSaleStatusToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onSaleStatusChanged(isChecked)
        }

        binding.addPropertyRoomsPlusMinusView.getValueEditText()
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: android.text.Editable?) {
                    // TODO viewmodel method
                }
            })

        binding.addPropertyRoomsPlusMinusView.decrementButton().setOnClickListener {
            viewModel.decrementRooms()
        }

        binding.addPropertyRoomsPlusMinusView.incrementButton().setOnClickListener {
            viewModel.incrementRooms()
        }
    }
}
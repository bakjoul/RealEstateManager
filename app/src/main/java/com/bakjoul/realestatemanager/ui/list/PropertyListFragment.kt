package com.bakjoul.realestatemanager.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentListBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PropertyListFragment : Fragment(R.layout.fragment_list) {

    private val binding by viewBinding { FragmentListBinding.bind(it) }
    private val viewModel by viewModels<PropertyListViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.listFilterButton.setOnClickListener { viewModel.onFilterButtonClicked() }
        binding.listAddPropertyFab.setOnClickListener { viewModel.onAddPropertyButtonClicked() }

        val adapter = PropertyAdapter()
        binding.listRecyclerView.adapter = adapter

        viewModel.propertiesLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}

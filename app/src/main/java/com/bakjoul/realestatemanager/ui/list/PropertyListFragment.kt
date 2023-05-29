package com.bakjoul.realestatemanager.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PropertyListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PropertyListViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PropertyAdapter()
        binding.listRecyclerView.adapter = adapter
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.rv_divider)!!)
        binding.listRecyclerView.addItemDecoration(divider)

        viewModel.propertiesLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}

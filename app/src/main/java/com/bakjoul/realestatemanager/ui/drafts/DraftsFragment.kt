package com.bakjoul.realestatemanager.ui.drafts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentDraftsBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DraftsFragment : DialogFragment(R.layout.fragment_drafts) {

    private val binding by viewBinding { FragmentDraftsBinding.bind(it) }
    private val viewModel by viewModels<DraftsViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DraftsAdapter()
        binding.draftsRecyclerView.adapter = adapter

        viewModel.draftsLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}

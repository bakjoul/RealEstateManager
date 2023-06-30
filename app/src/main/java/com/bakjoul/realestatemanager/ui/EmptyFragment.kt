package com.bakjoul.realestatemanager.ui

import androidx.fragment.app.Fragment
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentEmptyBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding

class EmptyFragment : Fragment(R.layout.fragment_empty) {
    private val binding by viewBinding { FragmentEmptyBinding.bind(it) }
}

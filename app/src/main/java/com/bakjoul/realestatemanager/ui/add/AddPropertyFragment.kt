package com.bakjoul.realestatemanager.ui.add

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentAddPropertyBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding

class AddPropertyFragment : Fragment(R.layout.fragment_add_property) {

    private val binding by viewBinding { FragmentAddPropertyBinding.bind(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
package com.bakjoul.realestatemanager.ui.drafts

import android.app.Dialog
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentDraftsBinding
import com.bakjoul.realestatemanager.ui.utils.CustomThemeDialog
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.hideKeyboard
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DraftsFragment : DialogFragment(R.layout.fragment_drafts) {

    private companion object {
        private const val TABLET_DIALOG_WINDOW_WIDTH = 0.5
        private const val TABLET_DIALOG_WINDOW_HEIGHT = 0.9
    }

    private val binding by viewBinding { FragmentDraftsBinding.bind(it) }
    private val viewModel by viewModels<DraftsViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : CustomThemeDialog(requireContext(), R.style.FullScreenDialog) {
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                if (currentFocus != null && !currentFocus!!.hasFocus()) {
                    hideKeyboard()
                    currentFocus!!.clearFocus()
                }
                return super.dispatchTouchEvent(ev)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (resources.getBoolean(R.bool.isTablet)) {
            val width = (resources.displayMetrics.widthPixels * TABLET_DIALOG_WINDOW_WIDTH).toInt()
            val height = (resources.displayMetrics.heightPixels * TABLET_DIALOG_WINDOW_HEIGHT).toInt()
            dialog?.window?.setLayout(width, height)
            dialog?.window?.setWindowAnimations(R.style.SlideInBottomAnimation)
        } else {
            dialog?.window?.setWindowAnimations(R.style.SlideInRightAnimation)
        }

        setToolbar()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DraftsAdapter()
        binding.draftsRecyclerView.adapter = adapter

        viewModel.draftsLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.viewActionLiveData.observeEvent(viewLifecycleOwner) {
            when (it) {
                DraftsViewAction.ShowProgressBar ->  binding.draftsProgressBar.visibility = View.VISIBLE
                DraftsViewAction.CloseDialog -> dismiss()
            }
        }
    }

    private fun setToolbar() {
        val toolbar = binding.draftsToolbar
        toolbar.setTitle(R.string.add_property_title)

        toolbar.setNavigationOnClickListener { viewModel.closeDialog() }
    }
}

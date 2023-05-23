package com.bakjoul.realestatemanager.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MainViewModel : ViewModel() {

    private var isTablet: Boolean = false

    fun onResume(isTablet: Boolean) {
        this.isTablet = isTablet
    }
}
